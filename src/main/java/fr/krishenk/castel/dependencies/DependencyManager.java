package fr.krishenk.castel.dependencies;

import fr.krishenk.castel.dependencies.classpath.BootstrapProvider;
import fr.krishenk.castel.dependencies.classpath.IsolatedClassLoader;
import fr.krishenk.castel.dependencies.relocation.RelocationHandler;
import fr.krishenk.castel.dependencies.relocation.SimpleRelocation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DependencyManager {
    private final BootstrapProvider plugin;
    private final Path cacheDirectory;
    private final EnumMap<Dependency, Path> loaded = new EnumMap(Dependency.class);
    private final Map<Set<Dependency>, IsolatedClassLoader> loaders = new HashMap<Set<Dependency>, IsolatedClassLoader>();
    private RelocationHandler relocationHandler;
    //public static final List<Dependency> REQUIRED_DEPENDENCIES = Arrays.asList(new Dependency[]{Dependency.KOTLIN_STDLIB, Dependency.CAFFEINE});

    public DependencyManager(BootstrapProvider plugin) {
        this.plugin = plugin;
        this.cacheDirectory = DependencyManager.setupCacheDirectory(plugin.getLibsFolder());
    }

    public EnumMap<Dependency, Path> getLoaded() {
        return this.loaded;
    }

    private synchronized RelocationHandler getRelocationHandler() {
        if (this.relocationHandler == null) {
            this.relocationHandler = new RelocationHandler(this);
        }
        return this.relocationHandler;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public IsolatedClassLoader obtainClassLoaderWith(Set<Dependency> dependencies) {
        EnumSet<Dependency> set = EnumSet.noneOf(Dependency.class);
        set.addAll(dependencies);
        for (Dependency dependency : dependencies) {
            if (this.loaded.containsKey((Object)dependency)) continue;
            throw new IllegalStateException("Dependency " + (Object)((Object)dependency) + " is not loaded.");
        }
        Map<Set<Dependency>, IsolatedClassLoader> map = this.loaders;
        synchronized (map) {
            IsolatedClassLoader classLoader = this.loaders.get(set);
            if (classLoader != null) {
                return classLoader;
            }
            URL[] urls = (URL[])set.stream().map(this.loaded::get).map(file -> {
                try {
                    return file.toUri().toURL();
                }
                catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
            }).toArray(URL[]::new);
            classLoader = new IsolatedClassLoader(urls);
            this.loaders.put(set, classLoader);
            return classLoader;
        }
    }

    public void tryLoadDependency(CountDownLatch latch, Dependency dependency) {
        int retries = 0;
        do {
            boolean count = true;
            try {
                this.loadDependency(dependency, retries == 0);
                return;
            }
            catch (Throwable ex) {
                if (ex instanceof DependencyDownloadException && ex.getCause() instanceof SocketException && ex.getCause().getMessage().equalsIgnoreCase("Connection reset")) {
                    this.plugin.getLogger().warning("[" + retries + "] Failed to download dependency '" + (Object)((Object)dependency) + "': " + ex.getMessage() + ". Retrying...");
                    count = false;
                    continue;
                }
                throw new IllegalStateException("Unable to load dependency: " + dependency.name(), ex);
            }
            finally {
                if (count) {
                    latch.countDown();
                }
            }
        } while (++retries <= 5);
    }

    public void loadDependencies(Collection<Dependency> dependencies) {
        Objects.requireNonNull(dependencies);
        CountDownLatch latch = new CountDownLatch(dependencies.size());
        for (Dependency dependency : dependencies) {
            this.tryLoadDependency(latch, dependency);
        }
        try {
            latch.await();
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static boolean shouldAutoLoad(Dependency dependency) {
        switch (dependency) {
            default:
        }
        return true;
    }

    private void loadDependency(Dependency dependency, boolean notifyDownload) throws Exception {
        if (this.loaded.containsKey((Object)dependency)) {
            return;
        }
        Path file = this.remapDependency(dependency, this.downloadDependency(dependency, notifyDownload));
        this.loaded.put(dependency, file);
        if (DependencyManager.shouldAutoLoad(dependency)) {
            this.plugin.getClassPathAppender().addJarToClasspath(file);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Path downloadDependency(Dependency dependency, boolean notifyDownload) throws DependencyDownloadException {
        Path file = this.cacheDirectory.resolve(dependency.getFileName(null));
        if (Files.exists(file, new LinkOption[0])) {
            return file;
        }
        DependencyDownloadException lastError = null;
        AtomicBoolean done = new AtomicBoolean(false);
        if (notifyDownload) {
            this.plugin.getLogger().info("Downloading " + (Object)((Object)dependency) + "...");
            this.plugin.runAsyncLater(() -> {
                if (!done.get()) {
                    //String url = DependencyRepository.MAVEN_CENTRAL.getUrl() + dependency.getMavenRepoPath();
                    //this.plugin.getLogger().warning("It looks like downloading " + dependency.name() + " is taking longer than expected. In case you have have any connection issues on your side, here's the direct download link: " + url + "\nAfter downloading it, put it inside '" + this.plugin.getLibsFolder().toAbsolutePath() + "' folder then restart the server.");
                }
            }, 10L, TimeUnit.SECONDS);
        }
        this.plugin.runAsyncLater(() -> {
            if (!done.get()) {
                this.plugin.getLogger().warning("It appears that the server is stuck trying to download " + (Object)((Object)dependency) + ". Please try restarting the server.");
            }
        }, 5L, TimeUnit.MINUTES);
        /*
        for (DependencyRepository repo : DependencyRepository.values()) {
            try {
                repo.download(dependency, file);
                Path path = file;
                return path;
            }
            catch (DependencyDownloadException e) {
                lastError = e;
            }
            finally {
                done.set(true);
            }
        */
        throw (DependencyDownloadException)Objects.requireNonNull(lastError);
    }

    private Path remapDependency(Dependency dependency, Path normalFile) throws Exception {
        ArrayList<SimpleRelocation> rules = new ArrayList<SimpleRelocation>(dependency.getRelocations());
        if (rules.isEmpty()) {
            return normalFile;
        }
        Path remappedFile = this.cacheDirectory.resolve(dependency.getFileName("remapped"));
        if (Files.exists(remappedFile, new LinkOption[0])) {
            return remappedFile;
        }
        this.getRelocationHandler().remap(normalFile, remappedFile, rules);
        return remappedFile;
    }

    private static Path setupCacheDirectory(Path path) {
        try {
            Files.createDirectories(path, new FileAttribute[0]);
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to create libs directory", e);
        }
        return path;
    }
}
