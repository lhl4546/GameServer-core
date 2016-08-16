package core.fire.scene.aoi;

import static java.lang.Math.abs;

import java.util.Iterator;

import core.fire.collection.IntHashMap;
import core.fire.scene.SceneException;
import core.fire.scene.aoi.LinkedList.Node;

/**
 * 场景，实现了基于十字链的AOI算法。拥有对象进入场景、场景内部移动、离开场景等功能。
 * 
 * @author lhl
 *
 */
public class Scene implements AOI
{
    /** x轴对象列表，first为距原点最近的对象，last为距原点最远的对象 */
    private LinkedList<AoiObject> xObjList = new LinkedList<>();
    /** y轴对象列表,first为距原点最近的对象，last为距原点最远的对象 */
    private LinkedList<AoiObject> yObjList = new LinkedList<>();
    /** 场景内所有对象，key为对象id */
    private IntHashMap<AoiObject> objMap = new IntHashMap<>();

    /** 对象进入场景通知列表，key为对象id */
    private IntHashMap<AoiObject> enterList = new IntHashMap<>();
    /** 对象场景内部移动通知列表，key为对象id */
    private IntHashMap<AoiObject> moveList = new IntHashMap<>();
    /** 对象离开场景通知列表，key为对象id */
    private IntHashMap<AoiObject> leaveList = new IntHashMap<>();

    /**
     * 更新。分别遍历进入集合、移动集合、离开集合，逐个通知，然后清空这些集合
     * 
     * @param aoiObj
     */
    private void update(AoiObject aoiObj) {
        Iterator<AoiObject> iterator = enterList.values().iterator();
        while (iterator.hasNext()) {
            AoiObject e = iterator.next();
            // 进入视野是相互的，所以需要发送2条通知
            System.out.println(String.format("send [%d %d:%d] ENTER msg to [%d %d:%d]", aoiObj.getId(), aoiObj.getX(), aoiObj.getY(), e.getId(), e.getX(), e.getY()));
            System.out.println(String.format("send [%d %d:%d] ENTER msg to [%d %d:%d]", e.getId(), e.getX(), e.getY(), aoiObj.getId(), aoiObj.getX(), aoiObj.getY()));
            iterator.remove();
        }

        iterator = moveList.values().iterator();
        while (iterator.hasNext()) {
            AoiObject e = iterator.next();
            System.out.println(String.format("send [%d %d:%d] MOVE msg to [%d %d:%d]", aoiObj.getId(), aoiObj.getX(), aoiObj.getY(), e.getId(), e.getX(), e.getY()));
            iterator.remove();
        }

        iterator = leaveList.values().iterator();
        while (iterator.hasNext()) {
            AoiObject e = iterator.next();
            // 离开场景后应该清空对象的AOI对象列表，所以只需通知仍在场景中的对象
            System.out.println(String.format("send [%d %d:%d] LEAVE msg to [%d %d:%d]", aoiObj.getId(), aoiObj.getX(), aoiObj.getY(), e.getId(), e.getX(), e.getY()));
            iterator.remove();
        }
    }

    /**
     * 搜索对象的AOI范围，执行如下逻辑：
     * <ol>
     * <li>搜索记录的xPos右边</li>
     * <li>搜索记录的xPos左边</li>
     * <li>搜索记录的yPos上面</li>
     * <li>搜索记录的yPos下面</li>
     * 
     * @param aoiObj 基准搜索对象
     * @param set 搜索结果集合
     */
    private void getRangeSet(AoiObject aoiObj, IntHashMap<AoiObject> set) {
        IntHashMap<AoiObject> xSet = new IntHashMap<>();
        int distance = aoiObj.getRadius();
        Node<AoiObject> iter = aoiObj.getxPos();
        if (iter != null && iter.next() != null) { // 向右搜索
            iter = iter.next();
            while (iter != null) {
                AoiObject item = iter.item();
                if (item.getX() - aoiObj.getX() > distance) {
                    break;
                }
                xSet.put(item.getId(), item);
                iter = iter.next();
            }
        }
        iter = aoiObj.getxPos();
        if (iter != null && iter.prev() != null) { // 向左搜索
            iter = iter.prev();
            while (iter != null) {
                AoiObject item = iter.item();
                if (aoiObj.getX() - item.getX() > distance) {
                    break;
                }
                xSet.put(item.getId(), item);
                iter = iter.prev();
            }
        }

        iter = aoiObj.getyPos();
        if (iter != null && iter.next() != null) { // 向上搜索
            iter = iter.next();
            while (iter != null) {
                AoiObject item = iter.item();
                if (item.getY() - aoiObj.getY() > distance) {
                    break;
                }
                if (xSet.containsKey(item.getId())) {
                    set.put(item.getId(), item);
                }
                iter = iter.next();
            }
        }
        iter = aoiObj.getyPos();
        if (iter != null && iter.prev() != null) { // 向下搜索
            iter = iter.prev();
            while (iter != null) {
                AoiObject item = iter.item();
                if (aoiObj.getY() - item.getY() > distance) {
                    break;
                }
                if (xSet.containsKey(item.getId())) {
                    set.put(item.getId(), item);
                }
                iter = iter.prev();
            }
        }
    }

    /**
     * 更新对象位置。根据移动方向计算对象在X、Y轴列表中的新位置
     * 
     * @param aoiObj
     * @param x
     * @param y
     */
    private void updateObjectPosition(AoiObject aoiObj, int x, int y) {
        int oldX = aoiObj.getX();
        int oldY = aoiObj.getY();
        aoiObj.setX(x);
        aoiObj.setY(y);

        Node<AoiObject> pos = null;
        if (x > oldX) { // 向右移动
            Node<AoiObject> iter = aoiObj.getxPos();
            if (iter != null) {
                iter = iter.next();
                if (iter != null && aoiObj.getX() > iter.item().getX()) { // 只有这次移动超过了右边那个对象才更新自己的位置
                    xObjList.remove(aoiObj.getxPos());
                    while (iter != null) { // 从旧位置开始向右查找直到第一个X坐标比我大的，就可以在它之前插入
                        if (aoiObj.getX() < iter.item().getX()) {
                            pos = iter;
                            break;
                        }
                        iter = iter.next();
                    }
                    if (pos != null) {
                        aoiObj.setxPos(xObjList.insertBefore(aoiObj, pos));
                    } else { // 整条X轴上都找不到比对象更大的坐标，则在末尾插入
                        aoiObj.setxPos(xObjList.insertLast(aoiObj));
                    }
                }
            }
        } else if (x < oldX) { // 向左移动
            Node<AoiObject> iter = aoiObj.getxPos();
            if (iter != null) {
                iter = iter.prev();
                if (iter != null && aoiObj.getX() < iter.item().getX()) { // 只有这次移动超过了左边那个对象才更新自己的位置
                    xObjList.remove(aoiObj.getxPos());
                    while (iter != null) { // 从旧位置开始向左查找直到第一个X坐标比我小的，就可以在它之后插入
                        if (aoiObj.getX() > iter.item().getX()) {
                            pos = iter.next();
                            break;
                        }
                        iter = iter.prev();
                    }
                    if (pos != null) { // 找到了第一个X坐标比我小的，在它之后插入
                        aoiObj.setxPos(xObjList.insertBefore(aoiObj, pos));
                    } else {
                        aoiObj.setxPos(xObjList.insertFirst(aoiObj));
                    }
                }
            }
        }

        pos = null;
        if (y > oldY) { // 向上移动
            Node<AoiObject> iter = aoiObj.getyPos();
            if (iter != null) {
                iter = iter.next();
                if (iter != null && aoiObj.getY() > iter.item().getX()) { // 只有这次移动超过了上面那个对象才更新自己的位置
                    yObjList.remove(aoiObj.getyPos());
                    while (iter != null) {
                        if (aoiObj.getY() < iter.item().getY()) {
                            pos = iter;
                            break;
                        }
                        iter = iter.next();
                    }
                    if (pos != null) {
                        aoiObj.setyPos(yObjList.insertBefore(aoiObj, pos));
                    } else {
                        aoiObj.setyPos(yObjList.insertLast(aoiObj));
                    }
                }
            }
        } else { // 向下移动
            Node<AoiObject> iter = aoiObj.getyPos();
            if (iter != null) {
                iter = iter.prev();
                if (iter != null && aoiObj.getY() < iter.item().getY()) { // 只有这次移动超过了下面那个对象才更新自己的位置
                    yObjList.remove(aoiObj.getyPos());
                    while (iter != null) {
                        if (aoiObj.getY() > iter.item().getY()) {
                            pos = iter.next();
                            break;
                        }
                        iter = iter.prev();
                    }
                    if (pos != null) {
                        aoiObj.setyPos(yObjList.insertBefore(aoiObj, pos));
                    } else {
                        aoiObj.setyPos(yObjList.insertLast(aoiObj));
                    }
                }
            }
        }
    }

    /**
     * 对象进入场景，执行如下逻辑：
     * <ol>
     * <li>添加对象到场景对象集合</li>
     * <li>添加对象到场景X轴对象集合合适的位置</li>
     * <li>添加对象到场景Y轴对象集合合适的位置</li>
     * <li>通知所有受影响的对象</li>
     * </ol>
     */
    @Override
    public void enter(int id, int x, int y, int distance) {
        if (objMap.containsKey(id)) {
            throw new SceneException("Duplicated enter operation, id: " + id);
        }
        AoiObject newObj = new AoiObject(id, x, y, distance);
        objMap.put(newObj.getId(), newObj);

        IntHashMap<AoiObject> xSet = new IntHashMap<>();
        int insertPos = 0;
        Iterator<AoiObject> iterator = xObjList.descendingIterator();
        while (iterator.hasNext()) {
            AoiObject e = iterator.next();
            int diff = e.getX() - newObj.getX();
            if (abs(diff) <= distance) {
                xSet.put(e.getId(), e);
            }
            if (diff > distance) {
                break;
            }
            insertPos++;
        }
        Node<AoiObject> xPos = xObjList.insert(insertPos, newObj);
        newObj.setxPos(xPos);

        insertPos = 0;
        iterator = yObjList.descendingIterator();
        while (iterator.hasNext()) {
            AoiObject e = iterator.next();
            int diff = e.getY() - newObj.getY();
            if (abs(diff) <= distance && xSet.containsKey(e.getId())) {
                enterList.put(e.getId(), e);
            }
            if (diff > distance) {
                break;
            }
            insertPos++;
        }
        Node<AoiObject> yPos = yObjList.insert(insertPos, newObj);
        newObj.setyPos(yPos);

        update(newObj);
    }

    @Override
    public void leave(int id) {
        AoiObject aoiObj = objMap.get(id);
        if (aoiObj == null) {
            throw new SceneException("Illegal leave operation, not in scene, id: " + id);
        }

        getRangeSet(aoiObj, leaveList);
        update(aoiObj);
        xObjList.remove(aoiObj.getxPos());
        yObjList.remove(aoiObj.getyPos());
        objMap.remove(id);
    }

    @Override
    public void move(int id, int x, int y) {
        if (!objMap.containsKey(id)) {
            throw new SceneException("Illegal move operation, not in scene, id: " + id + ", x: " + x + ", y: " + y);
        }

        AoiObject aoiObj = objMap.get(id);
        IntHashMap<AoiObject> oldSet = new IntHashMap<>();
        IntHashMap<AoiObject> newSet = new IntHashMap<>();

        getRangeSet(aoiObj, oldSet);

        updateObjectPosition(aoiObj, x, y);

        getRangeSet(aoiObj, newSet);

        // oldSet与newSet的交集是moveList
        for (AoiObject e : oldSet.values()) {
            if (newSet.containsKey(e.getId())) {
                moveList.put(e.getId(), e);
            }
        }

        // newSet与moveList的差集是enterList
        for (AoiObject e : newSet.values()) {
            if (!moveList.containsKey(e.getId())) {
                enterList.put(e.getId(), e);
            }
        }

        // oldSet与moveList的差集是leaveList
        for (AoiObject e : oldSet.values()) {
            if (!moveList.containsKey(e.getId())) {
                leaveList.put(e.getId(), e);
            }
        }

        update(aoiObj);
    }
}
