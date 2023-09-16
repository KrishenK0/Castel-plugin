package fr.krishenk.castel.dependencies.classpath;


import java.net.MalformedURLException;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class ReflectionClassPathAppender implements ClassPathAppender{
    private final URLClassLoaderAccess classLoaderAccess;

    public ReflectionClassPathAppender(ClassLoader classLoader) {
        if (!(classLoader instanceof URLClassLoader))
            throw new IllegalStateException("ClassLoader is not instance of URLClassLoader");
        this.classLoaderAccess = URLClassLoaderAccess.create((URLClassLoader) classLoader);
    }

    public ReflectionClassPathAppender(Object bootstrap) {
        this(bootstrap.getClass().getClassLoader());
    }

    @Override
    public void addJarToClasspath(Path file) {
        try {
            this.classLoaderAccess.addURL(file.toUri().toURL());
        } catch (MalformedURLException e) {
            throw  new RuntimeException(e);
        }
    }
}
