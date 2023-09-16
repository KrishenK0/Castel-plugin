package fr.krishenk.castel.utils;

import fr.krishenk.castel.utils.internal.arrays.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

public class FSUtil {
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    public static int countEntriesOf(Path folder) throws IOException {
        int n;
        block9: {
            if (!Files.isDirectory(folder, new LinkOption[0])) {
                throw new IllegalArgumentException("Path is not a folder: " + folder.toAbsolutePath());
            }
            DirectoryStream<Path> fs = null;
            try {
                fs = Files.newDirectoryStream(folder);
                n = ArrayUtils.sizeOfIterator(fs.iterator());
                if (fs == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (fs != null) {
                        try {
                            fs.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            fs.close();
        }
        return n;
    }

    public static int countEntriesOf(Path folder, Predicate<Path> filter) throws IOException {
        int n;
        block9: {
            if (!Files.isDirectory(folder, new LinkOption[0])) {
                throw new IllegalArgumentException("Path is not a folder: " + folder.toAbsolutePath());
            }
            DirectoryStream<Path> fs = null;
            try {
                fs = Files.newDirectoryStream(folder);
                n = (int) StreamSupport.stream(fs.spliterator(), false).filter(filter).count();
                if (fs == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (fs != null) {
                        try {
                            fs.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            fs.close();
        }
        return n;
    }

    public static boolean isFolderEmpty(Path folder) {
        try {
            return FSUtil.countEntriesOf(folder) == 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void deleteFolder(Path folder) {
        if (!Files.exists(folder, new LinkOption[0])) {
            return;
        }
        try {
            AtomicBoolean errored = new AtomicBoolean();
            Files.walk(folder, new FileVisitOption[0]).forEach(path -> {
                try {
                    if (folder.equals(path)) {
                        return;
                    }
                    if (Files.isDirectory(path, new LinkOption[0])) {
                        FSUtil.deleteFolder(path);
                    } else {
                        Files.delete(path);
                    }
                }
                catch (IOException ex) {
                    errored.set(true);
                    ex.printStackTrace();
                }
            });
            if (!errored.get()) {
                Files.delete(folder);
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void deleteAllFileTypes(Path folder, String type) {
        try {
            Files.list(folder).forEach(path -> {
                try {
                    if (folder.equals(path)) {
                        return;
                    }
                    if (!path.toString().endsWith(type)) {
                        return;
                    }
                    if (Files.isDirectory(path, new LinkOption[0])) {
                        return;
                    }
                    Files.delete(path);
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void transfer(InputStream in, OutputStream out) throws IOException {
        int read;
        Objects.requireNonNull(in, "in");
        Objects.requireNonNull(out, "out");
        byte[] buffer = new byte[8192];
        while ((read = in.read(buffer, 0, 8192)) >= 0) {
            out.write(buffer, 0, read);
        }
    }
}


