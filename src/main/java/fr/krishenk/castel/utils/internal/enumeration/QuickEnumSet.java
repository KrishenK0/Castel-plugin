package fr.krishenk.castel.utils.internal.enumeration;

import java.lang.reflect.Array;
import java.util.*;

public class QuickEnumSet<E extends Enum<E>>
        extends AbstractSet<E> {
    private final transient E[] universe;
    private final transient boolean[] elements;
    private transient int size;
    private transient int modCount;

    public QuickEnumSet(E[] universe) {
        this.universe = universe;
        this.elements = new boolean[universe.length];
    }

    public static <E extends Enum<E>> QuickEnumSet<E> allOf(E[] universe) {
        QuickEnumSet set = new QuickEnumSet(universe);
        set.size = universe.length;
        for (int i = 0; i < universe.length; ++i) {
            set.elements[i] = true;
        }
        return set;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean contains(Object o) {
        return this.elements[((Enum)o).ordinal()];
    }

    @Override
    public Iterator<E> iterator() {
        return new EnumSetIterator();
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[this.size];
        int index = 0;
        for (Enum element : this) {
            array[index++] = element;
        }
        return array;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        T[] array = a.length >= this.size ? a : (T[]) Array.newInstance(a.getClass().getComponentType(), this.size);
        int index = 0;
        for (Enum element : this) {
            array[index++] = (T) element;
        }
        return array;
    }

    @Override
    public boolean add(E e) {
        boolean contained = this.elements[((Enum)e).ordinal()];
        if (!contained) {
            this.elements[e.ordinal()] = true;
            ++this.modCount;
            ++this.size;
        }
        return contained;
    }

    @Override
    public boolean remove(Object o) {
        int ordinal = ((Enum)o).ordinal();
        boolean contained = this.elements[ordinal];
        if (contained) {
            this.elements[ordinal] = false;
            ++this.modCount;
            --this.size;
        }
        return contained;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Collection<?> e = c;
        for (Object a : e) {
            Enum b = (Enum) a;
            if (this.elements[b.ordinal()]) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        ++this.modCount;
        for (Enum e : c) {
            boolean contained = this.elements[e.ordinal()];
            if (contained) continue;
            this.elements[e.ordinal()] = true;
            ++this.size;
        }
        return true;
    }

    @SafeVarargs
    public final QuickEnumSet<E> addAll(E ... enums) {
        for (E e : enums) {
            this.elements[e.ordinal()] = true;
        }
        return this;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        this.modCount++;
        Collection<?> ce = c;
        for (Object obj : ce) {
            Enum<?> e = (Enum<?>) obj;
            boolean contained = this.elements[e.ordinal()];
            if (contained) {
                this.elements[e.ordinal()] = false;
                this.size--;
            }
        }
        return true;
    }

    @Override
    public void clear() {
        Arrays.fill(this.elements, false);
        ++this.modCount;
        this.size = 0;
    }

    private final class EnumSetIterator
            implements Iterator<E> {
        private int cursor;
        private int iterModCount;

        private EnumSetIterator() {
            this.iterModCount = QuickEnumSet.this.modCount;
        }

        @Override
        public boolean hasNext() {
            this.checkModCount();
            while (this.cursor != QuickEnumSet.this.elements.length) {
                if (QuickEnumSet.this.elements[this.cursor]) {
                    return true;
                }
                ++this.cursor;
            }
            return false;
        }

        @Override
        public E next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException("Size=" + QuickEnumSet.this.size);
            }
            return QuickEnumSet.this.universe[this.cursor++];
        }

        void checkModCount() {
            if (this.iterModCount != QuickEnumSet.this.modCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public void remove() {
            this.checkModCount();
            if (QuickEnumSet.this.elements[this.cursor - 1]) {
                ((QuickEnumSet)QuickEnumSet.this).elements[this.cursor - 1] = false;
                this.iterModCount = ++QuickEnumSet.this.modCount;
                QuickEnumSet.this.size--;
            }
        }
    }
}

