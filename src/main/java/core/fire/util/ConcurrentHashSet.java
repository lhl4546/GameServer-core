/**
 * 
 */
package core.fire.util;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 基于{@link java.util.concurrent.ConcurrentHashMap}的HashSet实现
 * 
 * @author lhl
 *
 *         2015年10月20日 下午3:45:01
 */
public final class ConcurrentHashSet<E> extends AbstractSet<E> implements Serializable
{
    private static final long serialVersionUID = -7768423633362716202L;

    private final ConcurrentMap<E, Boolean> map;

    public ConcurrentHashSet() {
        map = new ConcurrentHashMap<>();
    }

    public ConcurrentHashSet(Collection<? extends E> c) {
        map = new ConcurrentHashMap<>();
        addAll(c);
    }

    @Override
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean add(E o) {
        return map.putIfAbsent(o, Boolean.TRUE) == null;
    }

    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }
}
