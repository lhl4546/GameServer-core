/**
 * 
 */
package core.fire.util;

/**
 * 基于IntHashMap实现的IntHashSet
 * 
 * @author lhl
 *
 *         2016年5月24日 下午3:47:48
 */
public class IntHashSet
{
    private transient IntHashMap<Boolean> map;
    private static final Boolean PRESENT = Boolean.TRUE;

    public IntHashSet() {
        map = new IntHashMap<>();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean contains(int e) {
        return map.containsKey(e);
    }

    public boolean add(int e) {
        return map.put(e, PRESENT) == null;
    }

    public boolean remove(int e) {
        return map.remove(e) == PRESENT;
    }

    public void clear() {
        map.clear();
    }
}
