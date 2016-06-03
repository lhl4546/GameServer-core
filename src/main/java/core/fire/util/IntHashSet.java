/**
 * 
 */
package core.fire.util;

import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * 基于IntHashMap实现的IntHashSet
 * <p>
 * 继承{@linkplain AbstractCollection}是为了支持序列化与反序列化，顺便也利用了它的{@code toString}功能
 * 
 * @author lhl
 *
 *         2016年5月24日 下午3:47:48
 */
public class IntHashSet extends AbstractCollection<Integer>
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

    /**
     * 为支持反序列化必须重写{@linkplain AbstractCollection#add(Object)}
     * 方法，使用者请勿直接调用该方法，请使用重载方法{@linkplain #add(int)}添加元素
     */
    public boolean add(Integer e) {
        return map.put(e, PRESENT) == null;
    }

    public boolean remove(int e) {
        return map.remove(e) == PRESENT;
    }

    public void clear() {
        map.clear();
    }

    @Override
    public Iterator<Integer> iterator() {
        return map.keySet().iterator();
    }
}
