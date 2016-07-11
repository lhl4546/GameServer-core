/**
 * 
 */
package core.fire.collection;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * An implementation of hashmap whose key is primitive int. Null value is not
 * allowed. Notice that its not thread safe.
 * 
 * <p>
 * <b> Do not use this in concurrent scenario </b>
 * 
 * @author lhl
 *
 *         2016年4月7日 下午2:39:32
 * @see org.apache.commons.lang.IntHashMap
 */
public class IntHashMap<V>
{
    private transient Entry<V> table[];
    private transient int size;
    private int threshold;
    private final float loadFactor;

    transient volatile Set<Integer> keySet;
    transient volatile Collection<V> values;

    static final int DEFAULT_CAPACITY = 16;
    static final float DEFAULT_LOADFACTOR = 0.75f;
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * Constructs an empty <tt>IntHashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75f).
     */
    public IntHashMap() {
        this(DEFAULT_CAPACITY, DEFAULT_LOADFACTOR);
    }

    /**
     * Constructs an empty <tt>IntHashMap</tt> with the specified initial
     * capacity and the default load factor (0.75f).
     * 
     * @param initialCapacity
     */
    public IntHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOADFACTOR);
    }

    /**
     * Constructs an empty <tt>IntHashMap</tt> with the specitied initial
     * capacity and load factor.
     * 
     * @param initialCapacity
     * @param loadFactor
     */
    public IntHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);

        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;

        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " + loadFactor);

        this.loadFactor = loadFactor;
        threshold = tableSizeFor(initialCapacity);
    }

    /**
     * Returns a power of two size for the given target capacity.
     */
    static final int tableSizeFor(int cap) {
        int n = cap - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
    }

    /**
     * Returns the number of keys in this hashtable.
     *
     * @return the number of keys in this hashtable.
     */
    public int size() {
        return size;
    }

    /**
     * Tests if this hashtable maps no keys to values.
     *
     * @return <code>true</code> if this hashtable maps no keys to values;
     *         <code>false</code> otherwise.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * <p>
     * Returns <code>true</code> if this HashMap maps one or more keys to this
     * value.
     * </p>
     *
     * <p>
     * Note that this method is identical in functionality to contains (which
     * predates the Map interface).
     * </p>
     *
     * @param value value whose presence in this HashMap is to be tested.
     * @return boolean <code>true</code> if the value is contained
     */
    public boolean containsValue(Object value) {
        Objects.requireNonNull(value);

        Entry<V>[] tab = table;
        if (tab != null && size > 0) {
            V v;
            for (int i = 0; i < tab.length; i++) {
                for (Entry<V> e = tab[i]; e != null; e = e.next) {
                    if ((v = e.value) == value || v.equals(value)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * <p>
     * Tests if the specified object is a key in this hashtable.
     * </p>
     *
     * @param key possible key.
     * @return <code>true</code> if and only if the specified object is a key in
     *         this hashtable, as determined by the <tt>equals</tt> method;
     *         <code>false</code> otherwise.
     */
    public boolean containsKey(int key) {
        return get(key) != null;
    }

    /**
     * Returns the value to which the specified key is mapped in this map.
     *
     * @param key a key in the hashtable.
     * @return the value to which the key is mapped in this hashtable;
     *         <code>null</code> if the key is not mapped to any value in this
     *         hashtable.
     */
    public V get(int key) {
        Entry<V>[] tab = table;
        int n;
        if (tab != null && (n = tab.length) > 0) {
            int hash = key;
            for (Entry<V> e = tab[(n - 1) & hash]; e != null; e = e.next) {
                if (e.hash == hash) {
                    return e.value;
                }
            }
        }
        return null;
    }

    /**
     * <p>
     * Increases the capacity of and internally reorganizes this hashtable, in
     * order to accommodate and access its entries more efficiently.
     * </p>
     *
     * <p>
     * This method is called automatically when the number of keys in the
     * hashtable exceeds this hashtable's capacity and load factor.
     * </p>
     */
    private void rehash() {
        Entry<V>[] oldTable = table;
        int oldCap = (oldTable == null) ? 0 : oldTable.length;
        int oldThr = threshold;
        int newCap = 0, newThr = 0;
        if (oldCap > 0) {
            if (oldCap >= MAXIMUM_CAPACITY)
                threshold = Integer.MAX_VALUE;
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_CAPACITY) {
                newThr = oldThr << 1;
            }
        } else if (oldThr > 0) {
            newCap = oldThr;
        } else {
            newCap = DEFAULT_CAPACITY;
            newThr = (int) (DEFAULT_CAPACITY * DEFAULT_LOADFACTOR);
        }
        if (newThr == 0) {
            float ft = (float) newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float) MAXIMUM_CAPACITY ? (int) ft : Integer.MAX_VALUE);
        }
        threshold = newThr;

        @SuppressWarnings("unchecked")
        Entry<V>[] newTable = (Entry<V>[]) new Entry[newCap];
        table = newTable;

        if (oldTable != null) {
            for (int i = 0; i < oldCap; i++) {
                for (Entry<V> old = oldTable[i]; old != null;) {
                    Entry<V> e = old;
                    old = old.next;

                    int index = (newCap - 1) & e.hash;
                    e.next = newTable[index];
                    newTable[index] = e;
                }
            }
        }
    }

    /**
     * <p>
     * Maps the specified <code>key</code> to the specified <code>value</code>
     * in this hashtable. The key cannot be <code>null</code>.
     * </p>
     *
     * <p>
     * The value can be retrieved by calling the <code>get</code> method with a
     * key that is equal to the original key.
     * </p>
     *
     * @param key the hashtable key.
     * @param value the value.
     * @return the previous value of the specified key in this hashtable, or
     *         <code>null</code> if it did not have one.
     */
    public V put(int key, V value) {
        Entry<V>[] tab = table;
        if (tab == null || tab.length == 0) {
            rehash();
            tab = table;
        }

        int hash = key;
        int index = (tab.length - 1) & hash;
        for (Entry<V> e = tab[index]; e != null; e = e.next) {
            if (e.hash == hash) {
                V old = e.value;
                e.value = value;
                return old;
            }
        }

        tab[index] = new Entry<V>(hash, value, tab[index]);

        if (++size >= threshold)
            rehash();

        return null;
    }

    /**
     * Removes the key from this hashmap
     * 
     * @param key
     * @return the value to which the key had been mapped in this hashmap or
     *         null if the key does not have a mapping
     */
    public V remove(int key) {
        Entry<V>[] tab = table;
        if (tab != null && tab.length > 0) {
            int hash = key;
            int index = (tab.length - 1) & hash;
            for (Entry<V> e = tab[index], prev = null; e != null; prev = e, e = e.next) {
                if (e.hash == hash) {
                    if (prev != null) {
                        prev.next = e.next;
                    } else {
                        tab[index] = e.next;
                    }
                    --size;
                    V oldValue = e.value;
                    e.value = null;
                    return oldValue;
                }
            }
        }
        return null;
    }

    /**
     * Clears this hashmap
     */
    public void clear() {
        Entry<V>[] tab = table;
        if (tab != null && size > 0) {
            size = 0;
            for (int index = tab.length; --index >= 0;) {
                tab[index] = null;
            }
        }
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are reflected
     * in the collection, and vice-versa. If the map is modified while an
     * iteration over the collection is in progress (except through the
     * iterator's own <tt>remove</tt> operation), the results of the iteration
     * are undefined. The collection supports element removal, which removes the
     * corresponding mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations. It does not support the <tt>add</tt> or
     * <tt>addAll</tt> operations.
     *
     * @return a view of the values contained in this map
     */
    public Collection<V> values() {
        Collection<V> vs;
        return (vs = values) == null ? (values = new Values()) : vs;
    }

    final class Values extends AbstractCollection<V>
    {
        public final int size() {
            return size;
        }

        public final void clear() {
            IntHashMap.this.clear();
        }

        public final Iterator<V> iterator() {
            return new ValueIterator();
        }

        public final boolean contains(Object o) {
            return containsValue(o);
        }

        public final Spliterator<V> spliterator() {
            return new ValueSpliterator<>(IntHashMap.this, 0, -1, 0);
        }

        public final void forEach(Consumer<? super V> action) {
            Entry<V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                for (int i = 0; i < tab.length; ++i) {
                    for (Entry<V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.value);
                }
            }
        }
    }

    static final class ValueSpliterator<V> extends IntHashMapSpliterator<V> implements Spliterator<V>
    {
        ValueSpliterator(IntHashMap<V> m, int origin, int fence, int est) {
            super(m, origin, fence, est);
        }

        public ValueSpliterator<V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null : new ValueSpliterator<>(map, lo, index = mid, est >>>= 1);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            int i, hi;
            if (action == null)
                throw new NullPointerException();
            IntHashMap<V> m = map;
            Entry<V>[] tab = m.table;
            if ((hi = fence) < 0) {
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            if (tab != null && tab.length >= hi && (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Entry<V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.value);
                        p = p.next;
                    }
                } while (p != null || i < hi);
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Entry<V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        V v = current.value;
                        current = current.next;
                        action.accept(v);
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0);
        }
    }

    static class IntHashMapSpliterator<V>
    {
        final IntHashMap<V> map;
        Entry<V> current; // current node
        int index; // current index, modified on advance/split
        int fence; // one past last index
        int est; // size estimate

        IntHashMapSpliterator(IntHashMap<V> m, int origin, int fence, int est) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
        }

        final int getFence() { // initialize fence and size on first use
            int hi;
            if ((hi = fence) < 0) {
                IntHashMap<V> m = map;
                est = m.size;
                Entry<V>[] tab = m.table;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // force init
            return (long) est;
        }
    }

    final class ValueIterator extends HashIterator implements Iterator<V>
    {
        public final V next() {
            return nextNode().value;
        }
    }

    abstract class HashIterator
    {
        Entry<V> next; // next entry to return
        Entry<V> current; // current entry
        int index; // current slot

        HashIterator() {
            Entry<V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // advance to first entry
                do {
                } while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Entry<V> nextNode() {
            Entry<V>[] t;
            Entry<V> e = next;
            if (e == null)
                throw new NoSuchElementException();
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {
                } while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        public final void remove() {
            Entry<V> p = current;
            if (p == null)
                throw new IllegalStateException();
            current = null;
            IntHashMap.this.remove(p.hash);
        }
    }

    final class KeyIterator extends HashIterator implements Iterator<Integer>
    {
        public final Integer next() {
            return nextNode().hash;
        }
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map. The set is
     * backed by the map, so changes to the map are reflected in the set, and
     * vice-versa. If the map is modified while an iteration over the set is in
     * progress (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined. The set supports element
     * removal, which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt>, and <tt>clear</tt> operations. It does not support
     * the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the keys contained in this map
     */
    public Set<Integer> keySet() {
        Set<Integer> ks;
        return (ks = keySet) == null ? (keySet = new KeySet()) : ks;
    }

    final class KeySet extends AbstractSet<Integer>
    {
        public final int size() {
            return size;
        }

        public final void clear() {
            IntHashMap.this.clear();
        }

        public final Iterator<Integer> iterator() {
            return new KeyIterator();
        }

        public final boolean contains(int o) {
            return containsKey(o);
        }

        public final boolean remove(int key) {
            return IntHashMap.this.remove(key) != null;
        }

        public final Spliterator<Integer> spliterator() {
            throw new UnsupportedOperationException();
        }

        @Override
        public final void forEach(Consumer<? super Integer> action) {
            Entry<V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                for (int i = 0; i < tab.length; ++i) {
                    for (Entry<V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.hash);
                }
            }
        }
    }

    /**
     * For primitive int key, the hash is the int value of the key.
     * 
     * @author lhl
     *
     *         2016年4月7日 下午4:29:30
     */
    static class Entry<V>
    {
        final int hash;
        V value;
        Entry<V> next;

        Entry(int hash, V value, Entry<V> next) {
            this.hash = hash;
            this.value = value;
            this.next = next;
        }
    }
}
