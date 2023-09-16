package fr.krishenk.castel;

import fr.krishenk.castel.dependencies.classpath.BootstrapProvider;
import fr.krishenk.castel.dependencies.classpath.ClassPathAppender;
import fr.krishenk.castel.dependencies.classpath.ReflectionClassPathAppender;

import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CastelBootstrapProvider implements BootstrapProvider {
    private final CastelPlugin loader;
    private final ClassPathAppender classPathAppender;

    CastelBootstrapProvider(CastelPlugin plugin) {
        this.loader = plugin;
        this.classPathAppender = new ReflectionClassPathAppender(this.getClass().getClassLoader());
    }

    @Override
    public void onLoad() {
        this.loader.onLoad();
    }

    @Override
    public void onEnable() {
        this.loader.onEnable();
    }

    @Override
    public void onDisable() {
        this.loader.onDisable();
    }

    @Override
    public void runAsyncLater(Runnable runnable, long time, TimeUnit unit) {
        CastelPlugin.taskScheduler.asyncLater(Duration.ofMillis(unit.toMillis(time)), runnable);
    }

    @Override
    public ClassPathAppender getClassPathAppender() {
        return this.classPathAppender;
    }

    @Override
    public Path getLibsFolder() {
        return CastelPlugin.getFolder().resolve("libs");
    }

    @Override
    public Logger getLogger() {
        return this.loader.getLogger();
    }
}
