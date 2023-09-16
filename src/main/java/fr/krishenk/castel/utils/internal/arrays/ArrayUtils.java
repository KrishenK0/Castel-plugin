package fr.krishenk.castel.utils.internal.arrays;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.common.value.qual.IntRange;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ArrayUtils {
    public static <T> void reverse(T[] array) {
        int i = 0;
        for (int j = array.length - 1; j > i; ++i, --j) {
            T tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
        }
    }

    public static int sizeOfIterator(Iterator<?> iterator) {
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            ++count;
        }
        return count;
    }

    public static <T> T getLast(List<T> list2) {
        return list2.isEmpty() ? null : (T)list2.get(list2.size() - 1);
    }

    public static String[] mergeObjects(Object ... objects) {
        ArrayList<String> list2 = new ArrayList<String>(objects.length * 2);
        for (Object object : objects) {
            if (object instanceof Object[]) {
                for (Object o : (Object[])object) {
                    list2.add(o.toString());
                }
                continue;
            }
            list2.add(object.toString());
        }
        return list2.toArray(new String[0]);
    }

    public static String[] merge(String[] array1, String[] array2) {
        String[] joinedArray = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        return joinedArray;
    }

    public static Object[] shift(Object[] array) {
        Object[] result = new Object[array.length - 1];
        System.arraycopy(array, 1, result, 0, result.length);
        return result;
    }

    public static <T> ConditionalBuilder<T> when(boolean cond, T item) {
        return new ConditionalBuilder<T>(new ArrayList()).when(cond, item);
    }

    public static <T> T[] malloc(@NonNull T[] initial, @IntRange(from=1L, to=0x7FFFFFFFL) int length) {
        if (length <= initial.length) {
            throw new IllegalArgumentException("Cannot allocate array with the same or smaller size than the initial array " + length + " <= " + initial.length);
        }
        Object[] arr = (Object[]) Array.newInstance(initial.getClass().getComponentType(), length);
        System.arraycopy(initial, 0, arr, 0, initial.length);
        return (T[]) arr;
    }

    public static final class ConditionalBuilder<T> {
        protected final Collection<T> collection;

        private ConditionalBuilder(Collection<T> collection) {
            this.collection = collection;
        }

        public ConditionalBuilder<T> when(boolean cond, T item) {
            if (cond) {
                this.collection.add(item);
            }
            return this;
        }

        public Collection<T> build() {
            return this.collection;
        }

        public T[] toArray(T[] a) {
            return this.collection.toArray(a);
        }
    }
}
