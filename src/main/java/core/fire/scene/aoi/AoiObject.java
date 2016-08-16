package core.fire.scene.aoi;

import core.fire.scene.aoi.LinkedList.Node;

/**
 * Aoi对象
 * 
 * @author lhl
 *
 */
public class AoiObject
{
    /** 对象id */
    private int id;
    /** 对象X坐标 */
    private int x;
    /** 对象Y坐标 */
    private int y;
    /** 可视距离 */
    private int radius;
    /** 在X轴元素链表中的位置 */
    private Node<AoiObject> xPos;
    /** 在Y轴元素链表中的位置 */
    private Node<AoiObject> yPos;

    /**
     * @param id 对象id
     * @param x x坐标
     * @param y y坐标
     * @param radius 可视距离(x，y轴使用同一个可视距离)
     */
    public AoiObject(int id, int x, int y, int radius) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public Node<AoiObject> getxPos() {
        return xPos;
    }

    public void setxPos(Node<AoiObject> xPos) {
        this.xPos = xPos;
    }

    public Node<AoiObject> getyPos() {
        return yPos;
    }

    public void setyPos(Node<AoiObject> yPos) {
        this.yPos = yPos;
    }
}
