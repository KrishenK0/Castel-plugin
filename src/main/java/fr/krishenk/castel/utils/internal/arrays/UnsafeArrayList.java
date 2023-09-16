package fr.krishenk.castel.utils.internal.arrays;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.function.Consumer;

public class UnsafeArrayList<E> extends AbstractCollection<E> implements List<E>, RandomAccess {
    private static final int DEFAULT_CAPACITY = 10;
    private transient E[] array;
    public int size;

    private UnsafeArrayList() {
    }

    private UnsafeArrayList(E[] array) {
        this.array = array;
        this.size = array.length;
    }

    public static <E> UnsafeArrayList<E> withSize(E[] array) {
        UnsafeArrayList<E> list2 = new UnsafeArrayList<E>();
        list2.array = array;
        return list2;
    }

    @SafeVarargs
    public static <E> UnsafeArrayList<E> of(E ... elements) {
        return new UnsafeArrayList<E>(elements);
    }

    @SafeVarargs
    public static <E> UnsafeArrayList<E> copyOf(E ... elements) {
        return new UnsafeArrayList<E>(Arrays.copyOf(elements, elements.length));
    }

    @Override
    public String toString() {
        if (this.isEmpty()) {
            return "UnsafeArrayList:[]";
        }
        int iMax = this.size - 1;
        StringBuilder builder = new StringBuilder(20 + this.size * 5);
        builder.append("UnsafeArrayList:[");
        int i = 0;
        while (true) {
            builder.append(this.array[i]);
            if (i == iMax) {
                return builder.append(']').toString();
            }
            builder.append(", ");
            ++i;
        }
    }

    public void grow() {
        this.grow(this.size + 1);
    }

    @Override
    public int size() {
        return this.size;
    }

    public void resetPointer() {
        this.size = 0;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return this.indexOf(o) != -1;
    }

    @Override
    public @NonNull Iterator<E> iterator() {
        return new Itr(0);
    }

    @Override
    public int indexOf(Object object) {
        return this.indexOfRange(object, 0, this.size);
    }

    public int indexOfRange(Object object, int start, int end) {
        E[] array = this.array;
        while (start < end) {
            if (object.equals(array[start])) {
                return start;
            }
            ++start;
        }
        return -1;
    }

    public void setArray(E[] elements) {
        this.array = elements;
        this.size = elements.length;
    }

    public E[] getArray() {
        return this.array;
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.lastIndexOfRange(o, 0, this.size);
    }

    @Override
    public @NonNull ListIterator<E> listIterator() {
        return this.listIterator(0);
    }

    @Override
    public @NonNull ListIterator<E> listIterator(int index) {
        return new Itr(index);
    }

    @Override
    public @NonNull List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    public int lastIndexOfRange(Object object, int start, int end) {
        E[] array = this.array;
        while (--end >= start) {
            if (!object.equals(array[end])) continue;
            return end;
        }
        return -1;
    }

    @Override
    public E[] toArray() {
        return Arrays.copyOf(this.array, this.size);
    }

    @Override
    public <T> @NonNull T[] toArray(@NonNull T[] a) {
        if (a.length < this.size) {
            return (T[]) Arrays.copyOf(this.array, this.size, a.getClass());
        }
        System.arraycopy(this.array, 0, a, 0, this.size);
        if (a.length > this.size) {
            a[this.size] = null;
        }
        return a;
    }

    @Override
    public E get(int index) {
        return this.array[index];
    }

    @Override
    public E set(int index, E element) {
        E oldValue = this.array[index];
        this.array[index] = Objects.requireNonNull(element, "Cannot add null object");
        return oldValue;
    }

    @Override
    public void add(int index, E element) {
        if (this.size == this.array.length) {
            this.grow();
        }
        System.arraycopy(this.array, index, this.array, index + 1, this.size - index);
        this.array[index] = Objects.requireNonNull(element, "Cannot add null object");
        ++this.size;
    }

    @Override
    public E remove(int index) {
        E oldValue = this.array[index];
        this.fastRemove(index);
        return oldValue;
    }

    @Override
    public boolean add(E element) {
        if (this.size == this.array.length) {
            this.grow();
        }
        this.array[this.size++] = Objects.requireNonNull(element, "Cannot add null object");
        return true;
    }

    public boolean batchRemove(Collection<?> c, boolean complement, int from, int end) {
        Object[] array = this.array;
        while (true) {
            if (from == end) {
                return false;
            }
            if (c.contains(array[from]) != complement) break;
            ++from;
        }
        int w = from++;
        try {
            while (from < end) {
                E e = (E) array[from];
                if (c.contains(e) == complement) {
                    array[w++] = e;
                }
                ++from;
            }
        }
        catch (Throwable ex) {
            System.arraycopy(array, from, array, w, end - from);
            w += end - from;
            throw ex;
        }
        finally {
            this.shiftTailOverGap(array, w, end);
        }
        return true;
    }

    public void shiftTailOverGap(Object[] es, int lo, int hi) {
        System.arraycopy(es, hi, es, lo, this.size - hi);
        int to = this.size;
        for (int i = this.size -= hi - lo; i < to; ++i) {
            es[i] = null;
        }
    }

    @Override
    public int hashCode() {
        E[] array;
        int hashCode = 1;
        for (E element : array = this.array) {
            hashCode = 31 * hashCode + element.hashCode();
        }
        return hashCode;
    }

    @Override
    public boolean remove(Object obj) {
        E[] array = this.array;
        for (int i = 0; i < this.size; ++i) {
            if (!obj.equals(array[i])) continue;
            this.fastRemove(i);
            return true;
        }
        return false;
    }

    public void fastRemove(int i) {
        if (--this.size > i) {
            System.arraycopy(this.array, i + 1, this.array, i, this.size - i);
        }
        this.array[this.size] = null;
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> collection) {
        for (Object e : collection) {
            if (this.contains(e)) continue;
            return false;
        }
        return true;
    }

    @Override
    public void clear() {
        Arrays.fill(this.array, null);
        this.size = 0;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.addAll((E[]) c.toArray(new Object[0]));
    }

    public boolean addAll(E[] e) {
        int len = e.length;
        if (len == 0) {
            return false;
        }
        if (this.size + len > this.array.length) {
            this.grow(this.size + len);
        }
        System.arraycopy(e, 0, this.array, this.size, len);
        this.size += len;
        return true;
    }

    public void grow(int minCapacity) {
        this.array = Arrays.copyOf(this.array, this.newCapacity(minCapacity));
    }

    public int newCapacity(int minCapacity) {
        int currentCapacity = this.array.length;
        int newCapacity = currentCapacity + (currentCapacity >> 1);
        if (newCapacity - minCapacity <= 0) {
            if (currentCapacity == 0) {
                return Math.max(10, minCapacity);
            }
            if (minCapacity < 0) {
                throw new OutOfMemoryError();
            }
            return minCapacity;
        }
        return newCapacity;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        int numMoved;
        Object[] a = c.toArray();
        int numNew = a.length;
        if (numNew == 0) {
            return false;
        }
        if (numNew > this.array.length - this.size) {
            this.grow(this.size + numNew);
        }
        if ((numMoved = this.size - index) > 0) {
            System.arraycopy(this.array, index, this.array, index + numNew, numMoved);
        }
        System.arraycopy(a, 0, this.array, index, numNew);
        this.size += numNew;
        return true;
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return this.batchRemove(c, false, 0, this.size);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        return this.batchRemove(c, true, 0, this.size);
    }

    @Override
    public void forEach(@NonNull Consumer<? super E> action) {
        for (E element : this.array) {
            action.accept(element);
        }
    }

    private class Itr
            implements ListIterator<E> {
        public int cursor;

        Itr(int cursor) {
            this.cursor = cursor;
        }

        @Override
        public boolean hasNext() {
            return this.cursor != UnsafeArrayList.this.size;
        }

        @Override
        public E next() {
            return UnsafeArrayList.this.array[this.cursor++];
        }

        @Override
        public void remove() {
            UnsafeArrayList.this.remove(this.cursor - 1);
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            int size = UnsafeArrayList.this.size;
            if (this.cursor < size) {
                while (this.cursor < size) {
                    action.accept(UnsafeArrayList.this.array[this.cursor++]);
                }
            }
        }

        @Override
        public boolean hasPrevious() {
            return this.cursor != 0;
        }

        @Override
        public int nextIndex() {
            return this.cursor;
        }

        @Override
        public int previousIndex() {
            return this.cursor - 1;
        }

        @Override
        public E previous() {
            return UnsafeArrayList.this.array[--this.cursor];
        }

        @Override
        public void set(E e) {
            UnsafeArrayList.this.set(this.cursor - 1, e);
        }

        @Override
        public void add(E e) {
            UnsafeArrayList.this.add(this.cursor++, e);
        }
    }
}


