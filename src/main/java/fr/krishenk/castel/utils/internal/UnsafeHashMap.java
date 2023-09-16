package fr.krishenk.castel.utils.internal;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class UnsafeHashMap<K, V> implements Map<K, V>, Cloneable {
    public static final int DEFAULT_INITIAL_CAPACITY = 16;
    public static final int MAXIMUM_CAPACITY = 0x40000000;
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;
    public static final int TREEIFY_THRESHOLD = 8;
    public static final int UNTREEIFY_THRESHOLD = 6;
    public static final int MIN_TREEIFY_CAPACITY = 64;
    public final float loadFactor;
    public transient Set<K> keySet;
    public transient Collection<V> values;
    public transient Node<K, V>[] table;
    public transient Set<Map.Entry<K, V>> entrySet;
    private transient int size;
    private transient int modCount;
    private int threshold;

    public UnsafeHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
        }
        if (initialCapacity > 0x40000000) {
            initialCapacity = 0x40000000;
        }
        if (loadFactor <= 0.0f || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
        }
        this.loadFactor = loadFactor;
        this.threshold = UnsafeHashMap.tableSizeFor(initialCapacity);
    }

    public UnsafeHashMap(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }

    public UnsafeHashMap() {
        this.loadFactor = 0.75f;
    }

    public UnsafeHashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = 0.75f;
        this.putMapEntries(m, false);
    }

    @SafeVarargs
    public static <K, V> UnsafeHashMap<K, V> of(Map.Entry<? extends K, ? extends V>... entries) {
        UnsafeHashMap<K, V> map = new UnsafeHashMap<>();
        map.putEntries(false, entries);
        return map;
    }

    public static int hash(Object key) {
        if (key == null) {
            throw new NullPointerException("Cannot hash null key to map");
        }
        int h = key.hashCode();
        return h ^ h >>> 16;
    }

    public static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {
            Class<?> c = x.getClass();
            if (c == String.class) {
                return c;
            }
            Type[] ts = c.getGenericInterfaces();
            if (ts != null) {
                for (Type type : ts) {
                    Type[] as;
                    ParameterizedType p;
                    Type t = type;
                    if (!(t instanceof ParameterizedType) || (p = (ParameterizedType)t).getRawType() != Comparable.class || (as = p.getActualTypeArguments()) == null || as.length != 1 || as[0] != c) continue;
                    return c;
                }
            }
        }
        return null;
    }

    public static int compareComparables(Class<?> kc, Object k, Object x) {
        return x == null || x.getClass() != kc ? 0 : ((Comparable)k).compareTo(x);
    }

    public static int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        return (n |= n >>> 16) < 0 ? 1 : (n >= 0x40000000 ? 0x40000000 : n + 1);
    }

    public final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        this.putMapEntries(m, false, evict);
    }

    public final void putMapEntries(Map<? extends K, ? extends V> m, boolean ifAbsent, boolean evict) {
        int s = m.size();
        if (s > 0) {
            if (this.table == null) {
                int t;
                float ft = (float)s / this.loadFactor + 1.0f;
                int n = t = ft < 1.07374182E9f ? (int)ft : 0x40000000;
                if (t > this.threshold) {
                    this.threshold = UnsafeHashMap.tableSizeFor(t);
                }
            } else if (s > this.threshold) {
                this.resize();
            }
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                this.putVal(UnsafeHashMap.hash(key), key, value, ifAbsent, evict);
            }
        }
    }

    @SafeVarargs
    public final void putEntries(boolean evict, Map.Entry<? extends K, ? extends V>... entries) {
        int s = entries.length;
        if (s > 0) {
            if (this.table == null) {
                int t;
                float ft = (float) s / this.loadFactor + 1.0f;
                int n = t = ft < 1.07374182E9f ? (int) ft : 0x40000000;
                if (t > this.threshold) {
                    this.threshold = UnsafeHashMap.tableSizeFor(t);
                }
            } else if (s > this.threshold) {
                this.resize();
            }
            for (Map.Entry<? extends K, ? extends V> entry : entries) {
                K key = entry.getKey();
                V value = entry.getValue();
                this.putVal(UnsafeHashMap.hash(key), key, value, false, evict);
            }
        }
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
    public V get(Object key) {
        Node<K, V> e = this.getNode(UnsafeHashMap.hash(key), key);
        return e == null ? null : (V)e.value;
    }

    public @Nullable Node<K, V> getNode(int hash, Object key) {
        Object k;
        if (this.table == null || this.table.length == 0) {
            return null;
        }
        Node<K, V> first = this.table[this.table.length - 1 & hash];
        if (first == null) {
            return null;
        }
        if (first.hash == hash && ((k = first.key) == key || key.equals(k))) {
            return first;
        }
        Node e = first.next;
        if (e == null) {
            return null;
        }
        if (first instanceof TreeNode) {
            return ((TreeNode)first).getTreeNode(hash, key);
        }
        do {
            if (e.hash != hash || (k = e.key) != key && !key.equals(k)) continue;
            return e;
        } while ((e = e.next) != null);
        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        return this.getNode(UnsafeHashMap.hash(key), key) != null;
    }

    @Override
    public @Nullable V put(K key, @Nullable V value) {
        return this.putVal(UnsafeHashMap.hash(key), key, value, false, true);
    }

    public final V putVal(int hash, K key, @Nullable V value, boolean onlyIfAbsent, boolean evict) {
        int i;
        Node<K, V> p;
        int n;
        Node<K, V>[] tab = this.table;
        if (tab == null || (n = tab.length) == 0) {
            tab = this.resize();
            n = tab.length;
        }
        if ((p = tab[i = n - 1 & hash]) == null) {
            tab[i] = this.newNode(hash, key, value, null);
        } else {
            Node<K, V> e;
            Object k;
            if (p.hash == hash && ((k = p.key) == key || key.equals(k))) {
                e = p;
            } else if (p instanceof TreeNode) {
                e = ((TreeNode)p).putTreeVal(this, tab, hash, key, value);
            } else {
                int binCount = 0;
                while (true) {
                    if ((e = p.next) == null) {
                        p.next = this.newNode(hash, key, value, null);
                        if (binCount < 7) break;
                        this.treeifyBin(tab, hash);
                        break;
                    }
                    if (e.hash == hash && ((k = e.key) == key || key.equals(k))) break;
                    p = e;
                    ++binCount;
                }
            }
            if (e != null) {
                Object oldValue = e.value;
                if (!onlyIfAbsent || oldValue == null) {
                    e.value = value;
                }
                this.afterNodeAccess(e);
                return (V) oldValue;
            }
        }
        ++this.modCount;
        if (++this.size > this.threshold) {
            this.resize();
        }
        this.afterNodeInsertion(evict);
        return null;
    }

    public Node<K, V>[] resize() {
        int newCap;
        Node<K, V>[] oldTab = this.table;
        int oldCap = oldTab == null ? 0 : oldTab.length;
        int oldThr = this.threshold;
        int newThr = 0;
        if (oldCap > 0) {
            if (oldCap >= 0x40000000) {
                this.threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            newCap = oldCap << 1;
            if (newCap < 0x40000000 && oldCap >= 16) {
                newThr = oldThr << 1;
            }
        } else if (oldThr > 0) {
            newCap = oldThr;
        } else {
            newCap = 16;
            newThr = 12;
        }
        if (newThr == 0) {
            float ft = (float)newCap * this.loadFactor;
            newThr = newCap < 0x40000000 && ft < 1.07374182E9f ? (int)ft : Integer.MAX_VALUE;
        }
        this.threshold = newThr;
        Node[] newTab = new Node[newCap];
        this.table = newTab;
        if (oldTab != null) {
            for (int j = 0; j < oldCap; ++j) {
                Node next;
                Node<K, V> e = oldTab[j];
                if (e == null) continue;
                oldTab[j] = null;
                if (e.next == null) {
                    newTab[e.hash & newCap - 1] = e;
                    continue;
                }
                if (e instanceof TreeNode) {
                    ((TreeNode)e).split(this, newTab, j, oldCap);
                    continue;
                }
                Node<K, V> loHead = null;
                Node<K, V> loTail = null;
                Node<K, V> hiHead = null;
                Node<K, V> hiTail = null;
                do {
                    next = e.next;
                    if ((e.hash & oldCap) == 0) {
                        if (loTail == null) {
                            loHead = e;
                        } else {
                            loTail.next = e;
                        }
                        loTail = e;
                        continue;
                    }
                    if (hiTail == null) {
                        hiHead = e;
                    } else {
                        hiTail.next = e;
                    }
                    hiTail = e;
                } while ((e = next) != null);
                if (loTail != null) {
                    loTail.next = null;
                    newTab[j] = loHead;
                }
                if (hiTail == null) continue;
                hiTail.next = null;
                newTab[j + oldCap] = hiHead;
            }
        }
        return newTab;
    }

    public void treeifyBin(Node<K, V>[] tab, int hash) {
        int n;
        if (tab == null || (n = tab.length) < 64) {
            this.resize();
        } else {
            int index = n - 1 & hash;
            Node<K, V> e = tab[index];
            if (e != null) {
                TreeNode<K, V> hd = null;
                TreeNode<K, V> tl = null;
                do {
                    TreeNode<K, V> p = this.replacementTreeNode(e, null);
                    if (tl == null) {
                        hd = p;
                    } else {
                        p.prev = tl;
                        tl.next = p;
                    }
                    tl = p;
                } while ((e = e.next) != null);
                tab[index] = hd;
                if (tab[index] != null) {
                    hd.treeify(tab);
                }
            }
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        this.putMapEntries(m, true);
    }

    public void putAllIfAbsent(Map<? extends K, ? extends V> m) {
        this.putMapEntries(m, true, true);
    }

    @Override
    public V remove(Object key) {
        Node<K, V> e = this.removeNode(UnsafeHashMap.hash(key), key, null, false, true);
        return e == null ? null : (V)e.value;
    }

    public final @Nullable Node<K, V> removeNode(int hash, Object key, @Nullable Object value, boolean matchValue, boolean movable) {
        Object v;
        Object k;
        if (this.table == null || this.table.length == 0) {
            return null;
        }
        int index = this.table.length - 1 & hash;
        Node<K, V> p = this.table[index];
        if (p == null) {
            return null;
        }
        Node<K, V>[] tab = this.table;
        Node<K, V> node = null;
        if (p.hash == hash && ((k = p.key) == key || key.equals(k))) {
            node = p;
        } else {
            Node e = p.next;
            if (e != null) {
                if (p instanceof TreeNode) {
                    node = ((TreeNode)p).getTreeNode(hash, key);
                } else {
                    do {
                        if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                            node = e;
                            break;
                        }
                        p = e;
                    } while ((e = e.next) != null);
                }
            }
        }
        if (node != null && (!matchValue || (v = node.value) == value || value != null && value.equals(v))) {
            if (node instanceof TreeNode) {
                ((TreeNode)node).removeTreeNode(this, tab, movable);
            } else if (node == p) {
                tab[index] = node.next;
            } else {
                p.next = node.next;
            }
            ++this.modCount;
            --this.size;
            this.afterNodeRemoval(node);
            return node;
        }
        return null;
    }

    @Override
    public void clear() {
        ++this.modCount;
        if (this.table != null && this.size > 0) {
            Node<K, V>[] tab = this.table;
            this.size = 0;
            for (int i = 0; i < tab.length; ++i) {
                tab[i] = null;
            }
        }
    }

    @Override
    public boolean containsValue(Object value) {
        if (this.table != null && this.size > 0) {
            Node<K, V>[] tab = this.table;
            for (Node<K, V> e : this.table) {
                while (e != null) {
                    Object v = e.value;
                    if (v == value || value.equals(v)) {
                        return true;
                    }
                    e = e.next;
                }
            }
        }
        return false;
    }

    @Override
    public Set<K> keySet() {
        return this.keySet == null ? (this.keySet = new KeySet()) : this.keySet;
    }

    @Override
    public Collection<V> values() {
        return this.values == null ? (this.values = new Values()) : this.values;
    }

    public void initValues() {
        this.values = new Values();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return this.entrySet == null ? (this.entrySet = new EntrySet()) : this.entrySet;
    }

    @Override
    public @Nullable V getOrDefault(Object key, @Nullable V defaultValue) {
        Node<K, V> node = this.getNode(UnsafeHashMap.hash(key), key);
        return node == null ? defaultValue : node.value;
    }

    @Override
    public @Nullable V putIfAbsent(K key, @Nullable V value) {
        return this.putVal(UnsafeHashMap.hash(key), key, value, true, true);
    }

    @Override
    public boolean remove(Object key, @Nullable Object value) {
        return this.removeNode(UnsafeHashMap.hash(key), key, value, true, true) != null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Node<K, V> node = this.getNode(UnsafeHashMap.hash(key), key);
        if (node == null) {
            return false;
        }
        if (Objects.equals(node.value, oldValue)) {
            node.value = newValue;
            this.afterNodeAccess(node);
            return true;
        }
        return false;
    }

    @Override
    public @Nullable V replace(K key, @Nullable V value) {
        Node<K, V> node = this.getNode(UnsafeHashMap.hash(key), key);
        if (node == null) {
            return null;
        }
        V oldValue = node.value;
        node.value = value;
        this.afterNodeAccess(node);
        return oldValue;
    }

    @Override
    public @Nullable V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        V v;
        int i;
        Node<K, V> first;
        int n;
        Node<K, V>[] tab;
        Node<K, V> old;
        TreeNode t;
        int binCount;
        int hash;
        block16: {
            block15: {
                if (mappingFunction == null) {
                    throw new NullPointerException();
                }
                hash = UnsafeHashMap.hash(key);
                binCount = 0;
                t = null;
                old = null;
                if (this.size > this.threshold) break block15;
                tab = this.table;
                if (this.table != null && (n = tab.length) != 0) break block16;
            }
            tab = this.resize();
            n = tab.length;
        }
        if ((first = tab[i = n - 1 & hash]) != null) {
            V oldValue;
            if (first instanceof TreeNode) {
                t = (TreeNode)first;
                old = t.getTreeNode(hash, key);
            } else {
                Node<K, V> e = first;
                do {
                    Object k;
                    if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
            if (old != null && (oldValue = old.value) != null) {
                this.afterNodeAccess(old);
                return oldValue;
            }
        }
        if ((v = mappingFunction.apply(key)) == null) {
            return null;
        }
        if (old != null) {
            old.value = v;
            this.afterNodeAccess(old);
            return v;
        }
        if (t != null) {
            t.putTreeVal(this, tab, hash, key, v);
        } else {
            tab[i] = this.newNode(hash, key, v, first);
            if (binCount >= 7) {
                this.treeifyBin(tab, hash);
            }
        }
        ++this.modCount;
        ++this.size;
        this.afterNodeInsertion(true);
        return v;
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null) {
            throw new NullPointerException();
        }
        int hash = UnsafeHashMap.hash(key);
        Node<K, V> e = this.getNode(hash, key);
        if (e == null) {
            return null;
        }
        V v = e.value;
        if (v == null) {
            return null;
        }
        if ((v = remappingFunction.apply(key, v)) != null) {
            e.value = v;
            this.afterNodeAccess(e);
            return v;
        }
        this.removeNode(hash, key, null, false, true);
        return null;
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Node<K, V>[] tab;
        int n;
        if (remappingFunction == null) {
            throw new NullPointerException();
        }
        int hash = UnsafeHashMap.hash(key);
        if (this.size > this.threshold || this.table == null || (n = this.table.length) == 0) {
            tab = this.resize();
            n = tab.length;
        } else {
            tab = this.table;
        }
        TreeNode t = null;
        Node old = null;
        int binCount = 0;
        int i = n - 1 & hash;
        Node<K, V> first = this.table[i];
        if (first != null) {
            if (first instanceof TreeNode) {
                t = (TreeNode)first;
                old = t.getTreeNode(hash, key);
            } else {
                Node<K, V> e = first;
                do {
                    Object k;
                    if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        V oldValue = old == null ? null : (V) old.value;
        V v = remappingFunction.apply(key, oldValue);
        if (old != null) {
            if (v != null) {
                old.value = v;
                this.afterNodeAccess(old);
            } else {
                this.removeNode(hash, key, null, false, true);
            }
        } else if (v != null) {
            if (t != null) {
                t.putTreeVal(this, tab, hash, key, v);
            } else {
                tab[i] = this.newNode(hash, key, v, first);
                if (binCount >= 7) {
                    this.treeifyBin(tab, hash);
                }
            }
            ++this.modCount;
            ++this.size;
            this.afterNodeInsertion(true);
        }
        return v;
    }

    @Override
    public V merge(K key, @Nullable V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        int i;
        Node<K, V> first;
        int n;
        Node<K, V>[] tab;
        Node<K, V> old;
        TreeNode t;
        int binCount;
        int hash;
        block17: {
            block16: {
                if (value == null) {
                    throw new NullPointerException();
                }
                if (remappingFunction == null) {
                    throw new NullPointerException();
                }
                hash = UnsafeHashMap.hash(key);
                binCount = 0;
                t = null;
                old = null;
                if (this.size > this.threshold) break block16;
                tab = this.table;
                if (this.table != null && (n = tab.length) != 0) break block17;
            }
            tab = this.resize();
            n = tab.length;
        }
        if ((first = tab[i = n - 1 & hash]) != null) {
            if (first instanceof TreeNode) {
                t = (TreeNode)first;
                old = t.getTreeNode(hash, key);
            } else {
                Node<K, V> e = first;
                do {
                    Object k;
                    if (e.hash == hash && ((k = e.key) == key || key.equals(k))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        if (old != null) {
            V v = old.value != null ? remappingFunction.apply(old.value, value) : value;
            if (v != null) {
                old.value = v;
                this.afterNodeAccess(old);
            } else {
                this.removeNode(hash, key, null, false, true);
            }
            return v;
        }
        if (t != null) {
            t.putTreeVal(this, tab, hash, key, value);
        } else {
            tab[i] = this.newNode(hash, key, value, first);
            if (binCount >= 7) {
                this.treeifyBin(tab, hash);
            }
        }
        ++this.modCount;
        ++this.size;
        this.afterNodeInsertion(true);
        return value;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        if (action == null) {
            throw new NullPointerException();
        }
        if (this.size > 0 && this.table != null) {
            Node<K, V>[] tab = this.table;
            int mc = this.modCount;
            Node<K, V>[] arrnode = tab;
            int n = arrnode.length;
            for (int i = 0; i < n; ++i) {
                Node<K, V> kvNode;
                Node<K, V> e = kvNode = arrnode[i];
                while (e != null) {
                    action.accept(e.key, e.value);
                    e = e.next;
                }
            }
            if (this.modCount != mc) {
                throw new ConcurrentModificationException();
            }
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        if (function == null) {
            throw new NullPointerException();
        }
        if (this.size > 0 && this.table != null) {
            Node<K, V>[] tab = this.table;
            int mc = this.modCount;
            Node<K, V>[] arrnode = tab;
            int n = arrnode.length;
            for (int i = 0; i < n; ++i) {
                Node<K, V> kvNode;
                Node<K, V> e = kvNode = arrnode[i];
                while (e != null) {
                    e.value = function.apply(e.key, e.value);
                    e = e.next;
                }
            }
            if (this.modCount != mc) {
                throw new ConcurrentModificationException();
            }
        }
    }

    public Object clone() {
        UnsafeHashMap result;
        try {
            result = (UnsafeHashMap)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
        result.reinitialize();
        result.putMapEntries(this, false);
        return result;
    }

    public final int capacity() {
        return this.table != null ? this.table.length : (this.threshold > 0 ? this.threshold : 16);
    }

    public Node<K, V> newNode(int hash, K key, V value, Node<K, V> next) {
        return new Node<K, V>(hash, key, value, next);
    }

    public Node<K, V> replacementNode(Node<K, V> p, Node<K, V> next) {
        return new Node(p.hash, p.key, p.value, next);
    }

    public TreeNode<K, V> newTreeNode(int hash, K key, V value, Node<K, V> next) {
        return new TreeNode<K, V>(hash, key, value, next);
    }

    public TreeNode<K, V> replacementTreeNode(Node<K, V> p, Node<K, V> next) {
        return new TreeNode(p.hash, p.key, p.value, next);
    }

    public void reinitialize() {
        this.table = null;
        this.entrySet = null;
        this.keySet = null;
        this.values = null;
        this.modCount = 0;
        this.threshold = 0;
        this.size = 0;
    }

    void afterNodeAccess(Node<K, V> p) {
    }

    void afterNodeInsertion(boolean evict) {
    }

    void afterNodeRemoval(Node<K, V> p) {
    }

    public static class Node<K, V>
            implements Map.Entry<K, V> {
        public final int hash;
        public final K key;
        public V value;
        public Node<K, V> next;

        public Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        @Override
        public K getKey() {
            return this.key;
        }

        @Override
        public V getValue() {
            return this.value;
        }

        public String toString() {
            return this.key + "=" + this.value;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(this.key) ^ Objects.hashCode(this.value);
        }

        @Override
        public V setValue(V newValue) {
            V oldValue = this.value;
            this.value = newValue;
            return oldValue;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof Map.Entry) {
                Map.Entry e = (Map.Entry)o;
                return Objects.equals(this.key, e.getKey()) && Objects.equals(this.value, e.getValue());
            }
            return false;
        }
    }

    public static final class TreeNode<K, V>
            extends Entry<K, V> {
        TreeNode<K, V> parent;
        TreeNode<K, V> left;
        TreeNode<K, V> right;
        TreeNode<K, V> prev;
        boolean red;

        TreeNode(int hash, K key, V val, Node<K, V> next) {
            super(hash, key, val, next);
        }

        public static <K, V> void moveRootToFront(Node<K, V>[] tab, TreeNode<K, V> root) {
            int n;
            if (root != null && tab != null && (n = tab.length) > 0) {
                int index = n - 1 & root.hash;
                TreeNode first = (TreeNode)tab[index];
                if (root != first) {
                    tab[index] = root;
                    TreeNode<K, V> rp = root.prev;
                    Node rn = root.next;
                    if (rn != null) {
                        ((TreeNode)rn).prev = rp;
                    }
                    if (rp != null) {
                        rp.next = rn;
                    }
                    if (first != null) {
                        first.prev = root;
                    }
                    root.next = first;
                    root.prev = null;
                }
                assert (TreeNode.checkInvariants(root));
            }
        }

        public static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null || (d = a.getClass().getName().compareTo(b.getClass().getName())) == 0) {
                d = System.identityHashCode(a) <= System.identityHashCode(b) ? -1 : 1;
            }
            return d;
        }

        public static <K, V> TreeNode<K, V> rotateLeft(TreeNode<K, V> root, TreeNode<K, V> p) {
            TreeNode<K, V> r;
            if (p != null && (r = p.right) != null) {
                p.right = r.left;
                TreeNode<K, V> rl = p.right;
                if (p.right != null) {
                    rl.parent = p;
                }
                TreeNode<K, V> pp = r.parent = p.parent;
                if (r.parent == null) {
                    root = r;
                    r.red = false;
                } else if (pp.left == p) {
                    pp.left = r;
                } else {
                    pp.right = r;
                }
                r.left = p;
                p.parent = r;
            }
            return root;
        }

        public static <K, V> TreeNode<K, V> rotateRight(TreeNode<K, V> root, TreeNode<K, V> p) {
            TreeNode<K, V> l;
            if (p != null && (l = p.left) != null) {
                p.left = l.right;
                TreeNode<K, V> lr = p.left;
                if (p.left != null) {
                    lr.parent = p;
                }
                TreeNode<K, V> pp = l.parent = p.parent;
                if (l.parent == null) {
                    root = l;
                    l.red = false;
                } else if (pp.right == p) {
                    pp.right = l;
                } else {
                    pp.left = l;
                }
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        public static <K, V> TreeNode<K, V> balanceInsertion(TreeNode<K, V> root, TreeNode<K, V> x) {
            x.red = true;
            while (true) {
                TreeNode<K, V> xpp;
                TreeNode<K, V> xp;
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                if (!xp.red || (xpp = xp.parent) == null) {
                    return root;
                }
                TreeNode<K, V> xppl = xpp.left;
                if (xp == xppl) {
                    TreeNode<K, V> xppr = xpp.right;
                    if (xppr != null && xppr.red) {
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                        continue;
                    }
                    if (x == xp.right) {
                        x = xp;
                        root = TreeNode.rotateLeft(root, x);
                        xp = x.parent;
                        TreeNode<K, V> treeNode = xpp = xp == null ? null : xp.parent;
                    }
                    if (xp == null) continue;
                    xp.red = false;
                    if (xpp == null) continue;
                    xpp.red = true;
                    root = TreeNode.rotateRight(root, xpp);
                    continue;
                }
                if (xppl != null && xppl.red) {
                    xppl.red = false;
                    xp.red = false;
                    xpp.red = true;
                    x = xpp;
                    continue;
                }
                if (x == xp.left) {
                    x = xp;
                    root = TreeNode.rotateRight(root, x);
                    xp = x.parent;
                    TreeNode<K, V> treeNode = xpp = xp == null ? null : xp.parent;
                }
                if (xp == null) continue;
                xp.red = false;
                if (xpp == null) continue;
                xpp.red = true;
                root = TreeNode.rotateLeft(root, xpp);
            }
        }

        public static <K, V> TreeNode<K, V> balanceDeletion(TreeNode<K, V> root, TreeNode<K, V> x) {
            while (x != null && x != root) {
                TreeNode<K, V> sr;
                TreeNode<K, V> sl;
                TreeNode<K, V> xp = x.parent;
                if (xp == null) {
                    x.red = false;
                    return x;
                }
                if (x.red) {
                    x.red = false;
                    return root;
                }
                TreeNode<K, V> xpl = xp.left;
                if (xpl == x) {
                    TreeNode<K, V> xpr = xp.right;
                    if (xpr != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = TreeNode.rotateLeft(root, xp);
                        xp = x.parent;
                        TreeNode<K, V> treeNode = xpr = xp == null ? null : xp.right;
                    }
                    if (xpr == null) {
                        x = xp;
                        continue;
                    }
                    sl = xpr.left;
                    sr = xpr.right;
                    if (!(sr != null && sr.red || sl != null && sl.red)) {
                        xpr.red = true;
                        x = xp;
                        continue;
                    }
                    if (sr == null || !sr.red) {
                        if (sl != null) {
                            sl.red = false;
                        }
                        xpr.red = true;
                        root = TreeNode.rotateRight(root, xpr);
                        xp = x.parent;
                        TreeNode<K, V> treeNode = xpr = xp == null ? null : xp.right;
                    }
                    if (xpr != null) {
                        xpr.red = xp != null && xp.red;
                        sr = xpr.right;
                        if (sr != null) {
                            sr.red = false;
                        }
                    }
                    if (xp != null) {
                        xp.red = false;
                        root = TreeNode.rotateLeft(root, xp);
                    }
                    x = root;
                    continue;
                }
                if (xpl != null && xpl.red) {
                    xpl.red = false;
                    xp.red = true;
                    root = TreeNode.rotateRight(root, xp);
                    xp = x.parent;
                    TreeNode<K, V> treeNode = xpl = xp == null ? null : xp.left;
                }
                if (xpl == null) {
                    x = xp;
                    continue;
                }
                sl = xpl.left;
                sr = xpl.right;
                if (!(sl != null && sl.red || sr != null && sr.red)) {
                    xpl.red = true;
                    x = xp;
                    continue;
                }
                if (sl == null || !sl.red) {
                    if (sr != null) {
                        sr.red = false;
                    }
                    xpl.red = true;
                    root = TreeNode.rotateLeft(root, xpl);
                    xp = x.parent;
                    TreeNode<K, V> treeNode = xpl = xp == null ? null : xp.left;
                }
                if (xpl != null) {
                    xpl.red = xp != null && xp.red;
                    sl = xpl.left;
                    if (sl != null) {
                        sl.red = false;
                    }
                }
                if (xp != null) {
                    xp.red = false;
                    root = TreeNode.rotateRight(root, xp);
                }
                x = root;
            }
            return root;
        }

        public static <K, V> boolean checkInvariants(TreeNode<K, V> t) {
            TreeNode<K, V> tb = t.prev;
            if (tb != null && tb.next != t) {
                return false;
            }
            TreeNode tn = (TreeNode)t.next;
            if (tn != null && tn.prev != t) {
                return false;
            }
            TreeNode<K, V> tp = t.parent;
            if (tp != null && t != tp.left && t != tp.right) {
                return false;
            }
            TreeNode<K, V> tl = t.left;
            if (tl != null && (tl.parent != t || tl.hash > t.hash)) {
                return false;
            }
            TreeNode<K, V> tr = t.right;
            if (tr != null && (tr.parent != t || tr.hash < t.hash)) {
                return false;
            }
            if (t.red && tl != null && tl.red && tr != null && tr.red) {
                return false;
            }
            if (tl != null && !TreeNode.checkInvariants(tl)) {
                return false;
            }
            return tr == null || TreeNode.checkInvariants(tr);
        }

        public final TreeNode<K, V> root() {
            TreeNode<K, V> r = this;
            TreeNode<K, V> p;
            while ((p = r.parent) != null) {
                r = p;
            }
            return r;
        }

        public final TreeNode<K, V> find(int h, Object k, Class<?> kc) {
            TreeNode<K, V> p = this;
            do {
                int dir;
                TreeNode<K, V> pl = p.left;
                TreeNode<K, V> pr = p.right;
                int ph = p.hash;
                if (ph > h) {
                    p = pl;
                    continue;
                }
                if (ph < h) {
                    p = pr;
                    continue;
                }
                Object pk = p.key;
                if (pk == k || k != null && k.equals(pk)) {
                    return p;
                }
                if (pl == null) {
                    p = pr;
                    continue;
                }
                if (pr == null) {
                    p = pl;
                    continue;
                }
                if ((kc != null || (kc = UnsafeHashMap.comparableClassFor(k)) != null) && (dir = UnsafeHashMap.compareComparables(kc, k, pk)) != 0) {
                    p = dir < 0 ? pl : pr;
                    continue;
                }
                TreeNode<K, V> q = pr.find(h, k, kc);
                if (q != null) {
                    return q;
                }
                p = pl;
            } while (p != null);
            return null;
        }

        public final TreeNode<K, V> getTreeNode(int h, Object k) {
            return (this.parent != null ? this.root() : this).find(h, k, null);
        }

        public final void treeify(Node<K, V>[] tab) {
            TreeNode<K, V> root = null;
            TreeNode<K, V> x = this;
            while (x != null) {
                TreeNode next = (TreeNode)x.next;
                x.right = null;
                x.left = null;
                if (root == null) {
                    x.parent = null;
                    x.red = false;
                    root = x;
                } else {
                    TreeNode<K, V> xp;
                    int dir;
                    Object k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    TreeNode<K, V> p = root;
                    do {
                        Object pk = p.key;
                        int ph = p.hash;
                        if (ph > h) {
                            dir = -1;
                        } else if (ph < h) {
                            dir = 1;
                        } else if (kc == null && (kc = UnsafeHashMap.comparableClassFor(k)) == null || (dir = UnsafeHashMap.compareComparables(kc, k, pk)) == 0) {
                            dir = TreeNode.tieBreakOrder(k, pk);
                        }
                        xp = p;
                    } while ((p = dir <= 0 ? p.left : p.right) != null);
                    x.parent = xp;
                    if (dir <= 0) {
                        xp.left = x;
                    } else {
                        xp.right = x;
                    }
                    root = TreeNode.balanceInsertion(root, x);
                }
                x = next;
            }
            TreeNode.moveRootToFront(tab, root);
        }

        public final Node<K, V> untreeify(UnsafeHashMap<K, V> map) {
            Node<K, V> hd = null;
            Node<K, V> tl = null;
            Node q = this;
            while (q != null) {
                Node<K, V> p = map.replacementNode(q, null);
                if (tl == null) {
                    hd = p;
                } else {
                    tl.next = p;
                }
                tl = p;
                q = q.next;
            }
            return hd;
        }

        public final @Nullable TreeNode<K, V> putTreeVal(UnsafeHashMap<K, V> map, Node<K, V>[] tab, int h, K k, @Nullable V v) {
            TreeNode<K, V> xp;
            int dir;
            TreeNode<K, V> root;
            Class<?> kc = null;
            boolean searched = false;
            TreeNode<K, V> p = root = this.parent != null ? this.root() : this;
            do {
                int ph;
                if ((ph = p.hash) > h) {
                    dir = -1;
                } else if (ph < h) {
                    dir = 1;
                } else {
                    Object pk = p.key;
                    if (pk == k || k.equals(pk)) {
                        return p;
                    }
                    if (kc == null && (kc = UnsafeHashMap.comparableClassFor(k)) == null || (dir = UnsafeHashMap.compareComparables(kc, k, pk)) == 0) {
                        if (!searched) {
                            TreeNode<K, V> q;
                            searched = true;
                            TreeNode<K, V> ch = p.left;
                            if (ch != null && (q = ch.find(h, k, kc)) != null || (ch = p.right) != null && (q = ch.find(h, k, kc)) != null) {
                                return q;
                            }
                        }
                        dir = TreeNode.tieBreakOrder(k, pk);
                    }
                }
                xp = p;
            } while ((p = dir <= 0 ? p.left : p.right) != null);
            Node xpn = xp.next;
            TreeNode<K, V> x = map.newTreeNode(h, k, v, xpn);
            if (dir <= 0) {
                xp.left = x;
            } else {
                xp.right = x;
            }
            xp.next = x;
            x.prev = xp;
            x.parent = x.prev;
            if (xpn != null) {
                ((TreeNode)xpn).prev = x;
            }
            TreeNode.moveRootToFront(tab, TreeNode.balanceInsertion(root, x));
            return null;
        }

        public final void removeTreeNode(UnsafeHashMap<K, V> map, Node<K, V>[] tab, boolean movable) {
            TreeNode<K, V> r;
            TreeNode<K, V> replacement;
            TreeNode<K, V> rl;
            TreeNode<K, V> first;
            if (tab == null || tab.length == 0) {
                return;
            }
            int index = tab.length - 1 & this.hash;
            TreeNode<K, V> root = first = (TreeNode<K, V>)tab[index];
            TreeNode succ = (TreeNode)this.next;
            TreeNode<K, V> pred = this.prev;
            if (pred == null) {
                first = succ;
                tab[index] = first;
            } else {
                pred.next = succ;
            }
            if (succ != null) {
                succ.prev = pred;
            }
            if (first == null) {
                return;
            }
            if (root.parent != null) {
                root = root.root();
            }
            if (root == null || root.right == null || (rl = root.left) == null || rl.left == null) {
                tab[index] = first.untreeify(map);
                return;
            }
            TreeNode<K, V> p = this;
            TreeNode<K, V> pl = this.left;
            TreeNode<K, V> pr = this.right;
            if (pl != null && pr != null) {
                TreeNode<K, V> sl;
                TreeNode<K, V> s = pr;
                while ((sl = s.left) != null) {
                    s = sl;
                }
                boolean c = s.red;
                s.red = p.red;
                p.red = c;
                TreeNode<K, V> sr = s.right;
                TreeNode<K, V> pp = p.parent;
                if (s == pr) {
                    p.parent = s;
                    s.right = p;
                } else {
                    TreeNode<K, V> sp = s.parent;
                    p.parent = sp;
                    if (p.parent != null) {
                        if (s == sp.left) {
                            sp.left = p;
                        } else {
                            sp.right = p;
                        }
                    }
                    if ((s.right = pr) != null) {
                        pr.parent = s;
                    }
                }
                p.left = null;
                p.right = sr;
                if (p.right != null) {
                    sr.parent = p;
                }
                if ((s.left = pl) != null) {
                    pl.parent = s;
                }
                if ((s.parent = pp) == null) {
                    root = s;
                } else if (p == pp.left) {
                    pp.left = s;
                } else {
                    pp.right = s;
                }
                replacement = sr != null ? sr : p;
            } else {
                replacement = pl != null ? pl : (pr != null ? pr : p);
            }
            if (replacement != p) {
                replacement.parent = p.parent;
                TreeNode<K, V> pp = replacement.parent;
                if (pp == null) {
                    root = replacement;
                } else if (p == pp.left) {
                    pp.left = replacement;
                } else {
                    pp.right = replacement;
                }
                p.parent = null;
                p.right = null;
                p.left = null;
            }
            TreeNode<K, V> treeNode = r = p.red ? root : TreeNode.balanceDeletion(root, replacement);
            if (replacement == p) {
                TreeNode<K, V> pp = p.parent;
                p.parent = null;
                if (pp != null) {
                    if (p == pp.left) {
                        pp.left = null;
                    } else if (p == pp.right) {
                        pp.right = null;
                    }
                }
            }
            if (movable) {
                TreeNode.moveRootToFront(tab, r);
            }
        }

        public final void split(UnsafeHashMap<K, V> map, Node<K, V>[] tab, int index, int bit) {
            TreeNode b = this;
            TreeNode loHead = null;
            TreeNode loTail = null;
            TreeNode hiHead = null;
            TreeNode hiTail = null;
            int lc = 0;
            int hc = 0;
            TreeNode e = b;
            while (e != null) {
                TreeNode next = (TreeNode)e.next;
                e.next = null;
                if ((e.hash & bit) == 0) {
                    e.prev = loTail;
                    if (e.prev == null) {
                        loHead = e;
                    } else {
                        loTail.next = e;
                    }
                    loTail = e;
                    ++lc;
                } else {
                    e.prev = hiTail;
                    if (e.prev == null) {
                        hiHead = e;
                    } else {
                        hiTail.next = e;
                    }
                    hiTail = e;
                    ++hc;
                }
                e = next;
            }
            if (loHead != null) {
                if (lc <= 6) {
                    tab[index] = loHead.untreeify(map);
                } else {
                    tab[index] = loHead;
                    if (hiHead != null) {
                        loHead.treeify(tab);
                    }
                }
            }
            if (hiHead != null) {
                if (hc <= 6) {
                    tab[index + bit] = hiHead.untreeify(map);
                } else {
                    tab[index + bit] = hiHead;
                    if (loHead != null) {
                        hiHead.treeify(tab);
                    }
                }
            }
        }
    }

    public final class KeySet
            extends AbstractSet<K> {
        @Override
        public final int size() {
            return UnsafeHashMap.this.size;
        }

        @Override
        public final void clear() {
            UnsafeHashMap.this.clear();
        }

        @Override
        public final Iterator<K> iterator() {
            return new KeyIterator();
        }

        @Override
        public final boolean contains(Object o) {
            return UnsafeHashMap.this.containsKey(o);
        }

        @Override
        public final boolean remove(Object key) {
            return UnsafeHashMap.this.removeNode(UnsafeHashMap.hash(key), key, null, false, true) != null;
        }

        @Override
        public final Spliterator<K> spliterator() {
            return new KeySpliterator(UnsafeHashMap.this, 0, -1, 0, 0);
        }

        @Override
        public final void forEach(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (UnsafeHashMap.this.size > 0 && UnsafeHashMap.this.table != null) {
                Node<K, V>[] tab = UnsafeHashMap.this.table;
                int mc = UnsafeHashMap.this.modCount;
                for (Node<K, V> e : tab) {
                    while (e != null) {
                        action.accept(e.key);
                        e = e.next;
                    }
                }
                if (UnsafeHashMap.this.modCount != mc) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    private final class Values
            extends AbstractCollection<V> {
        private Values() {
        }

        @Override
        public final int size() {
            return UnsafeHashMap.this.size;
        }

        @Override
        public final void clear() {
            UnsafeHashMap.this.clear();
        }

        @Override
        public final Iterator<V> iterator() {
            return new ValueIterator();
        }

        @Override
        public final boolean contains(Object o) {
            return UnsafeHashMap.this.containsValue(o);
        }

        @Override
        public final Spliterator<V> spliterator() {
            return new ValueSpliterator(UnsafeHashMap.this, 0, -1, 0, 0);
        }

        @Override
        public final void forEach(Consumer<? super V> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (UnsafeHashMap.this.size > 0 && UnsafeHashMap.this.table != null) {
                Node<K, V>[] tab = UnsafeHashMap.this.table;
                int mc = UnsafeHashMap.this.modCount;
                for (Node<K, V> e : tab) {
                    while (e != null) {
                        action.accept(e.value);
                        e = e.next;
                    }
                }
                if (UnsafeHashMap.this.modCount != mc) {
                    throw new ConcurrentModificationException();
                }
            }
        }
    }

    private final class EntrySet
            extends AbstractSet<Map.Entry<K, V>> {
        private EntrySet() {
        }

        @Override
        public final int size() {
            return UnsafeHashMap.this.size;
        }

        @Override
        public final void clear() {
            UnsafeHashMap.this.clear();
        }

        @Override
        public final Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            Object key = e.getKey();
            Node candidate = UnsafeHashMap.this.getNode(UnsafeHashMap.hash(key), key);
            return e.equals(candidate);
        }

        @Override
        public final boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry e = (Map.Entry)o;
                Object key = e.getKey();
                Object value = e.getValue();
                return UnsafeHashMap.this.removeNode(UnsafeHashMap.hash(key), key, value, true, true) != null;
            }
            return false;
        }

        @Override
        public final Spliterator<Map.Entry<K, V>> spliterator() {
            return new EntrySpliterator(UnsafeHashMap.this, 0, -1, 0, 0);
        }

        @Override
        public final void forEach(Consumer<? super Map.Entry<K, V>> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (UnsafeHashMap.this.size > 0 && UnsafeHashMap.this.table != null) {
                Node<K, V>[] tab = UnsafeHashMap.this.table;
                int mc = UnsafeHashMap.this.modCount;
                for (Node<K, V> e : tab) {
                    while (e != null) {
                        action.accept(e);
                        e = e.next;
                    }
                }
                if (UnsafeHashMap.this.modCount != mc) {
                    throw new ConcurrentModificationException();
                }
            }
        }

    }

    public final class EntryIterator
            extends HashIterator
            implements Iterator<Map.Entry<K, V>> {
        @Override
        public final Map.Entry<K, V> next() {
            return this.nextNode();
        }
    }

    public final class ValueIterator
            extends HashIterator
            implements Iterator<V> {
        @Override
        public final V next() {
            return this.nextNode().value;
        }
    }

    public final class KeyIterator
            extends HashIterator
            implements Iterator<K> {
        @Override
        public final K next() {
            return this.nextNode().key;
        }
    }

    public abstract class HashIterator {
        public Node<K, V> next;
        public Node<K, V> current;
        public int expectedModCount;
        public int index;

        public HashIterator() {
            this.expectedModCount = UnsafeHashMap.this.modCount;
            this.next = null;
            this.current = null;
            this.index = 0;
            if (UnsafeHashMap.this.table != null && UnsafeHashMap.this.size > 0) {
                Node<K, V>[] tab = UnsafeHashMap.this.table;
                while (this.index < tab.length && (this.next = tab[this.index++]) == null) {
                }
            }
        }

        public final boolean hasNext() {
            return this.next != null;
        }

        public final Node<K, V> nextNode() {
            if (UnsafeHashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            if (UnsafeHashMap.this.table == null) {
                return this.next;
            }
            Node e = this.next;
            this.current = e;
            this.next = this.current.next;
            if (this.next == null) {
                Node<K, V>[] tab = UnsafeHashMap.this.table;
                while (this.index < tab.length && (this.next = tab[this.index++]) == null) {
                }
            }
            return e;
        }

        public final void remove() {
            if (UnsafeHashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (this.current == null) {
                throw new IllegalStateException();
            }
            UnsafeHashMap.this.removeNode(this.current.hash, this.current.key, null, false, false);
            this.expectedModCount = UnsafeHashMap.this.modCount;
            this.current = null;
        }
    }

    public static class Entry<K, V>
            extends Node<K, V> {
        Entry(int hash, K key, V value, Node<K, V> next) {
            super(hash, key, value, next);
        }
    }

    public static final class EntrySpliterator<K, V>
            extends HashMapSpliterator<K, V>
            implements Spliterator<Map.Entry<K, V>> {
        EntrySpliterator(UnsafeHashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K, V> trySplit() {
            EntrySpliterator<K, V> entrySpliterator;
            int lo = this.index;
            int hi = this.getFence();
            int mid = lo + hi >>> 1;
            if (lo >= mid || this.current != null) {
                entrySpliterator = null;
            } else {
                this.index = this.index;
                this.est = this.est;
                entrySpliterator = new EntrySpliterator<K, V>(this.map, lo, mid, this.est >>> 1, this.expectedModCount);
            }
            return entrySpliterator;
        }

        @Override
        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            int i;
            int mc;
            if (action == null) {
                throw new NullPointerException();
            }
            UnsafeHashMap m = this.map;
            Node<K, V>[] tab = m.table;
            int hi = this.fence;
            if (hi < 0) {
                mc = this.expectedModCount = m.modCount;
                this.fence = tab == null ? 0 : tab.length;
                hi = this.fence;
            } else {
                mc = this.expectedModCount;
            }
            if (tab != null && tab.length >= hi && (i = this.index) >= 0 && (i < (this.index = hi) || this.current != null)) {
                Node<K, V> p = this.current;
                this.current = null;
                do {
                    if (p == null) {
                        p = tab[i++];
                        continue;
                    }
                    action.accept(p);
                    p = p.next;
                } while (p != null || i < hi);
                if (m.modCount != mc) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
            int hi;
            if (action == null) {
                throw new NullPointerException();
            }
            Node<K, V>[] tab = this.map.table;
            if (tab != null && tab.length >= (hi = this.getFence()) && this.index >= 0) {
                while (this.current != null || this.index < hi) {
                    if (this.current == null) {
                        this.current = tab[this.index++];
                        continue;
                    }
                    Node<K, V> e = this.current;
                    this.current = this.current.next;
                    action.accept(e);
                    if (this.map.modCount != this.expectedModCount) {
                        throw new ConcurrentModificationException();
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public int characteristics() {
            return (this.fence < 0 || this.est == this.map.size ? 64 : 0) | 1;
        }
    }

    public static final class ValueSpliterator<K, V>
            extends HashMapSpliterator<K, V>
            implements Spliterator<V> {
        ValueSpliterator(UnsafeHashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K, V> trySplit() {
            ValueSpliterator<K, V> valueSpliterator;
            int lo = this.index;
            int hi = this.getFence();
            int mid = lo + hi >>> 1;
            if (lo >= mid || this.current != null) {
                valueSpliterator = null;
            } else {
                this.index = this.index;
                this.est = this.est;
                valueSpliterator = new ValueSpliterator<K, V>(this.map, lo, mid, this.est >>> 1, this.expectedModCount);
            }
            return valueSpliterator;
        }

        @Override
        public void forEachRemaining(Consumer<? super V> action) {
            int i;
            int mc;
            if (action == null) {
                throw new NullPointerException();
            }
            UnsafeHashMap m = this.map;
            Node<K, V>[] tab = m.table;
            int hi = this.fence;
            if (hi < 0) {
                mc = this.expectedModCount = m.modCount;
                this.fence = tab == null ? 0 : tab.length;
                hi = this.fence;
            } else {
                mc = this.expectedModCount;
            }
            if (tab != null && tab.length >= hi && (i = this.index) >= 0 && (i < (this.index = hi) || this.current != null)) {
                Node<K, V> p = this.current;
                this.current = null;
                do {
                    if (p == null) {
                        p = tab[i++];
                        continue;
                    }
                    action.accept(p.value);
                    p = p.next;
                } while (p != null || i < hi);
                if (m.modCount != mc) {
                    throw new ConcurrentModificationException();
                }
            }
        }

        @Override
        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null) {
                throw new NullPointerException();
            }
            Node<K, V>[] tab = this.map.table;
            if (tab != null && tab.length >= (hi = this.getFence()) && this.index >= 0) {
                while (this.current != null || this.index < hi) {
                    if (this.current == null) {
                        this.current = tab[this.index++];
                        continue;
                    }
                    V v = this.current.value;
                    this.current = this.current.next;
                    action.accept(v);
                    if (this.map.modCount != this.expectedModCount) {
                        throw new ConcurrentModificationException();
                    }
                    return true;
                }
            }
            return false;
        }


        @Override
        public int characteristics() {
            return this.fence < 0 || this.est == this.map.size ? 64 : 0;
        }
    }

    public static final class KeySpliterator<K, V>
            extends HashMapSpliterator<K, V>
            implements Spliterator<K> {
        KeySpliterator(UnsafeHashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K, V> trySplit() {
            KeySpliterator<K, V> keySpliterator;
            int hi = this.getFence();
            int mid = this.index + hi >>> 1;
            if (this.index >= mid || this.current != null) {
                keySpliterator = null;
            } else {
                this.index = this.index;
                this.est = this.est;
                keySpliterator = new KeySpliterator<K, V>(this.map, this.index, mid, this.est >>> 1, this.expectedModCount);
            }
            return keySpliterator;
        }

        @Override
        public void forEachRemaining(Consumer<? super K> action) {
            int i;
            int mc;
            if (action == null) {
                throw new NullPointerException();
            }
            UnsafeHashMap<K, V> m = this.map;
            Node<K, V>[] tab = m.table;
            int hi = this.fence;
            if (hi < 0) {
                mc = this.expectedModCount = m.modCount;
                this.fence = tab == null ? 0 : tab.length;
                hi = this.fence;
            } else {
                mc = this.expectedModCount;
            }
            if (tab != null && tab.length >= hi && (i = this.index) >= 0 && (i < (this.index = hi) || this.current != null)) {
                Node<K, V> p = this.current;
                this.current = null;
                do {
                    if (p == null) {
                        p = tab[i++];
                        continue;
                    }
                    action.accept(p.key);
                    p = p.next;
                } while (p != null || i < hi);
                if (m.modCount != mc) {
                    throw new ConcurrentModificationException();
                }
            }
        }


        @Override
        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null) {
                throw new NullPointerException();
            }
            Node<K, V>[] tab = this.map.table;
            if (tab != null && tab.length >= (hi = this.getFence()) && this.index >= 0) {
                while (this.current != null || this.index < hi) {
                    if (this.current == null) {
                        this.current = tab[this.index++];
                        continue;
                    }
                    K k = this.current.key;
                    this.current = this.current.next;
                    action.accept(k);
                    if (this.map.modCount != this.expectedModCount) {
                        throw new ConcurrentModificationException();
                    }
                    return true;
                }
            }
            return false;
        }


        @Override
        public int characteristics() {
            return (this.fence < 0 || this.est == this.map.size ? 64 : 0) | 1;
        }
    }

    public static class HashMapSpliterator<K, V> {
        public final UnsafeHashMap<K, V> map;
        public Node<K, V> current;
        public int index;
        public int fence;
        public int est;
        public int expectedModCount;

        public HashMapSpliterator(UnsafeHashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        public final int getFence() {
            if (this.fence >= 0) {
                return this.fence;
            }
            this.est = ((UnsafeHashMap)this.map).size;
            this.expectedModCount = ((UnsafeHashMap)this.map).modCount;
            Node<K, V>[] tab = this.map.table;
            this.fence = tab == null ? 0 : tab.length;
            return this.fence;
        }

        public final long estimateSize() {
            this.getFence();
            return this.est;
        }
    }
}

