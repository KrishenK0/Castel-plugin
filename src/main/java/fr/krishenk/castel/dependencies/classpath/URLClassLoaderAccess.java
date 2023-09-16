package fr.krishenk.castel.dependencies.classpath;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;

public abstract class URLClassLoaderAccess {
    private final URLClassLoader classLoader;

    public static URLClassLoaderAccess create(URLClassLoader classLoader) {
        if (Reflection.isSupported()) {
            return new Reflection(classLoader);
        }
        if (Unsafe.isSupported()) {
            return new Unsafe(classLoader);
        }
        return Noop.INSTANCE;
    }

    protected URLClassLoaderAccess(URLClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public abstract void addURL(URL var1);

    private static void throwError(Throwable cause) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Unable to inject into the plugin URLClassLoader.\nYou may be able to fix this problem by adding the following command-line argument directly after the 'java' command in your start script: \n'--add-opens java.base/java.lang=ALL-UNNAMED'", cause);
    }

    private static class Reflection
            extends URLClassLoaderAccess {
        private static final Method ADD_URL_METHOD;

        private static boolean isSupported() {
            return ADD_URL_METHOD != null;
        }

        Reflection(URLClassLoader classLoader) {
            super(classLoader);
        }

        @Override
        public void addURL(URL url) {
            try {
                ADD_URL_METHOD.invoke(((URLClassLoaderAccess)this).classLoader, url);
            }
            catch (ReflectiveOperationException e) {
                URLClassLoaderAccess.throwError(e);
            }
        }

        static {
            Method addUrlMethod;
            try {
                addUrlMethod = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addUrlMethod.setAccessible(true);
            }
            catch (Exception e) {
                addUrlMethod = null;
            }
            ADD_URL_METHOD = addUrlMethod;
        }
    }

    private static class Unsafe
            extends URLClassLoaderAccess {
        private static final Object UNSAFE;
        private final Collection<URL> unopenedURLs;
        private final Collection<URL> pathURLs;

        private static boolean isSupported() {
            return UNSAFE != null;
        }

        Unsafe(URLClassLoader classLoader) {
            super(classLoader);
            Collection pathURLs;
            Collection unopenedURLs;
            try {
                Object ucp = Unsafe.fetchField(URLClassLoader.class, classLoader, "ucp");
                unopenedURLs = (Collection)Unsafe.fetchField(ucp.getClass(), ucp, "unopenedUrls");
                pathURLs = (Collection)Unsafe.fetchField(ucp.getClass(), ucp, "path");
            }
            catch (Throwable e) {
                unopenedURLs = null;
                pathURLs = null;
            }
            this.unopenedURLs = unopenedURLs;
            this.pathURLs = pathURLs;
        }

        private static Object fetchField(Class<?> clazz, Object object, String name) throws NoSuchFieldException {
            Field field = clazz.getDeclaredField(name);
            try {
                long offset = (Long)UNSAFE.getClass().getDeclaredMethod("objectFieldOffset", Field.class).invoke(UNSAFE, field);
                return UNSAFE.getClass().getDeclaredMethod("getObject", Object.class, Long.TYPE).invoke(UNSAFE, object, offset);
            }
            catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void addURL(URL url) {
            if (this.unopenedURLs == null || this.pathURLs == null) {
                URLClassLoaderAccess.throwError(new NullPointerException("unopenedURLs or pathURLs"));
            }
            this.unopenedURLs.add(url);
            this.pathURLs.add(url);
        }

        static {
            Object unsafe;
            try {
                Field unsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
                unsafeField.setAccessible(true);
                unsafe = unsafeField.get(null);
            }
            catch (Throwable t) {
                unsafe = null;
            }
            UNSAFE = unsafe;
        }
    }

    private static class Noop
            extends URLClassLoaderAccess {
        private static final Noop INSTANCE = new Noop();

        private Noop() {
            super(null);
        }

        @Override
        public void addURL(URL url) {
            URLClassLoaderAccess.throwError(null);
        }
    }
}

