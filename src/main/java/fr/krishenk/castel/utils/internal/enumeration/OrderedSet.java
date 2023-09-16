package fr.krishenk.castel.utils.internal.enumeration;

import java.util.*;

public class OrderedSet<E> extends AbstractSet<E> implements Set<E> {
    private Object[] elements;
    private int size;
    private int modCount;

    public OrderedSet(int size) {
        this.elements = new Object[size];
    }

    public int getSize() {
        return this.size;
    }

    public void setSize(int n) {
        this.size = n;
    }

    public OrderedSet(Collection<? extends E> collection) {
        this(collection.size());
        this.addAll(collection);
    }

    @Override
    public boolean add(E element) {
        E e = element;
        int hash = e != null ? e.hashCode() : 0;
        OrderedSet this_$iv = this;
        boolean $i$f$internalContains = false;
        if (hash < this_$iv.elements.length && this_$iv.elements[hash] != null) {
            return true;
        }
        int n = this.modCount;
        this.modCount = n + 1;
        this.ensureCapacity(hash);
        this.elements[hash] = element;
        n = this.size();
        this.setSize(n + 1);
        return false;
    }

    public final void ensureCapacity(int elementHash) {
        if (elementHash < this.elements.length) {
            return;
        }
        Object[] newElements = new Object[elementHash + 1];
        System.arraycopy(this.elements, 0, newElements, 0, this.elements.length);
        this.elements = newElements;
    }

    @Override
    public boolean addAll(Collection<? extends E> elements) {
        
        this.ensureCapacity(elements.size());
        int n = this.modCount;
        this.modCount = n + 1;
        for (E element : elements) {
            this.add(element);
        }
        return true;
    }

    @Override
    public void clear() {
        int n = this.modCount;
        this.modCount = n + 1;
        Arrays.fill(this.elements, 0, 6, null);
        this.setSize(0);
    }

    @Override
    public java.util.Iterator<E> iterator() {
        return new Iterator();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean remove(Object element) {
        int hash = element != null ? element.hashCode() : 0;
        boolean contained = hash < this.elements.length && this.elements[hash] != null;
        if (contained) {
            int n = this.modCount;
            this.modCount = n + 1;
            this.elements[hash] = null;
            n = this.size();
            this.setSize(n + -1);
        }
        return contained;
    }

    @Override
    public boolean removeAll(Collection<? extends Object> elements) {
        
        int n = this.modCount;
        this.modCount = n + 1;
        for (Object object : elements) {
            this.remove(object);
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<? extends Object> elements) {
        
        java.util.Iterator<E> iter = this.iterator();
        int n = this.modCount;
        this.modCount = n + 1;
        while (iter.hasNext()) {
            E next = iter.next();
            if (elements.contains(next)) continue;
            iter.remove();
        }
        return true;
    }

    /*
     * WARNING - void declaration
     */
    @Override
    public boolean contains(Object element) {
        OrderedSet orderedSet = this;
        int hash = element != null ? element.hashCode() : 0;
        return hash < orderedSet.elements.length && orderedSet.elements[hash] != null;
    }


    private final boolean internalContains(int hash) {
        return hash < this.elements.length && this.elements[hash] != null;
    }

    @Override
    public boolean containsAll(Collection<? extends Object> elements) {
        for (Object object : elements) {
            if (this.contains(object)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    public final class Iterator<E>
            implements java.util.Iterator<E>{
        private int cursor;
        private int iterModCount;

        public Iterator() {
            this.iterModCount = OrderedSet.this.modCount;
        }

        public int getCursor() {
            return this.cursor;
        }

        public void setCursor(int n) {
            this.cursor = n;
        }

        @Override
        public boolean hasNext() {
            this.checkModCount();
            while (this.cursor != OrderedSet.this.elements.length) {
                Object element = OrderedSet.this.elements[this.cursor];
                if (element != null) {
                    return true;
                }
                int n = this.cursor;
                this.cursor = n + 1;
            }
            return false;
        }

        @Override
        public E next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException("Size=" + OrderedSet.this.size());
            }
            int n = this.cursor;
            this.cursor = n + 1;
            return (E) OrderedSet.this.elements[n];
        }

        private void checkModCount() {
            if (this.iterModCount != OrderedSet.this.modCount) {
                throw new ConcurrentModificationException();
            }
        }

        @Override
        public void remove() {
            this.checkModCount();
            if (OrderedSet.this.elements[this.cursor] == null) {
                throw new IllegalStateException("Element already removed, next() not called");
            }
            OrderedSet.this.elements[this.cursor] = null;
            OrderedSet<E> orderedSet = (OrderedSet<E>) OrderedSet.this;
            orderedSet.modCount = orderedSet.modCount + 1;
            this.iterModCount = orderedSet.modCount;
            orderedSet = (OrderedSet<E>) OrderedSet.this;
            int n = orderedSet.size();
            orderedSet.setSize(n + -1);
        }
    }
}
