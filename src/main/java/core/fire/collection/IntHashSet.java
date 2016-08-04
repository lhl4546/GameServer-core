/**
 * 
 */
package core.fire.collection;

import java.util.Iterator;

/**
 * IntHashSet which elements are primitive int
 * 
 * @author lhl
 *
 *         2016年5月24日 下午3:47:48
 */
public class IntHashSet implements Iterable<Integer>
{
    private transient IntHashMap<Boolean> map;
    private static final Boolean PRESENT = Boolean.TRUE;

    public IntHashSet() {
        map = new IntHashMap<>();
    }

    /**
     * Returns the number of elements in this hashset.
     * 
     * @return
     */
    public int size() {
        return map.size();
    }

    /**
     * Tests if this hashset is empty
     * 
     * @return
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Tests if an specified element is in this hashset
     * 
     * @param e
     * @return
     */
    public boolean contains(int e) {
        return map.containsKey(e);
    }

    /**
     * add an element to this hashset
     * 
     * @param e
     * @return
     */
    public boolean add(int e) {
        return map.put(e, PRESENT) == null;
    }

    /**
     * remove an element from this hashset
     * 
     * @param e
     * @return
     */
    public boolean remove(int e) {
        return map.remove(e) == PRESENT;
    }

    /**
     * remove all elements of this hashset
     */
    public void clear() {
        map.clear();
    }

    @Override
    public Iterator<Integer> iterator() {
        return map.keySet().iterator();
    }
}
