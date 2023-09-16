package fr.krishenk.castel.utils.internal.nonnull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;

public class NullabilityUtils {
    @NotNull
    public static final NullabilityUtils INSTANCE = new NullabilityUtils();

    private NullabilityUtils() {
    }

    
    @NotNull
    public static final <T> Collection<T> assertNonNullElements(@NotNull Collection<? extends T> collection) {
        boolean bl;
        block4: {
            if (collection.isEmpty()) {
                bl = false;
            } else {
                Iterator iterator = collection.iterator();
                while (iterator.hasNext()) {
                    if (!(iterator.next() == null)) continue;
                    bl = true;
                    break block4;
                }
                bl = false;
            }
        }
        if (bl) {
            throw new IllegalArgumentException(collection.getClass().getSimpleName() + " contains null");
        }
        return (Collection<T>) collection;
    }

    
    public static final <T> T assertNonNull(@NotNull Collection<? extends T> $this$assertNonNull, @Nullable T obj) {
        T t = obj;
        if (t == null) {
            throw new IllegalArgumentException($this$assertNonNull.getClass().getSimpleName() + " cannot contain null values");
        }
        return t;
    }
}


