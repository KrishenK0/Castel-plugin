package fr.krishenk.castel.utils.internal.nonnull;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class NonNullSet<V> implements Set<V> {
    @NotNull
    private final Set<V> set;
    private final int size;

    public NonNullSet(@NotNull Set<V> set) {
        
        this.set = set;
        this.size = this.set.size();
    }

    @NotNull
    public final Set<V> getSet() {
        return this.set;
    }

    @Override
    public void clear() {
        this.set.clear();
    }

    @Override
    public boolean isEmpty() {
        return this.set.isEmpty();
    }

    @Override
    @NotNull
    public Iterator<V> iterator() {
        return this.set.iterator();
    }

    public int getSize() {
        return this.size;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends V> elements) {
        
        return this.set.addAll(NullabilityUtils.assertNonNullElements(elements));
    }

    @Override
    public boolean add(V element) {
        return this.set.add(NullabilityUtils.assertNonNull(this, element));
    }

    @Override
    public boolean remove(Object element) {
        return this.set.remove(NullabilityUtils.assertNonNull(this, element));
    }

    @Override
    public boolean contains(Object element) {
        return this.set.contains(NullabilityUtils.assertNonNull(this, element));
    }

    @Override
    public boolean containsAll(@NotNull Collection<? extends Object> elements) {
        return this.set.containsAll(NullabilityUtils.assertNonNullElements(elements));
    }

    @Override
    public boolean retainAll(@NotNull Collection<? extends Object> elements) {
        Set<Object> set = new HashSet<>(this.set);
        set.retainAll(NullabilityUtils.assertNonNullElements(elements));
        return this.set.retainAll(set);
    }

    @Override
    public boolean removeAll(@NotNull Collection<? extends Object> elements) {
        Set<Object> set = new HashSet<>(this.set);
        set.retainAll(NullabilityUtils.assertNonNullElements(elements));
        return this.set.removeAll(set);
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return this.set.toArray(array);
    }

    @Override
    public Object[] toArray() {
        return this.set.toArray();
    }
}

