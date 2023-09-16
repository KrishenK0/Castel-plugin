package fr.krishenk.castel.dependencies.classpath;

import java.nio.file.Path;

public interface ClassPathAppender extends AutoCloseable {
    public void addJarToClasspath(Path var1);

    @Override
    default public void close() {
    }
}
