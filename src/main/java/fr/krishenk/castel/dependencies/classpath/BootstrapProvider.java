package fr.krishenk.castel.dependencies.classpath;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public interface BootstrapProvider {
    public void onLoad();

    public void onEnable();

    public void onDisable();

    public void runAsyncLater(Runnable var1, long var2, TimeUnit var4);

    public ClassPathAppender getClassPathAppender();

    public Path getLibsFolder();

    public Logger getLogger();
}
