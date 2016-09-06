package core.fire.scene.astar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A星寻路。
 * <p>
 * 这里坐标为左上角为(0, 0)，右下角为(x, y)
 * 
 * @author lhl
 *
 */
public class AStarPathFind
{
    private final static int BARRIER = 1; // 1表示障碍物
    private final static int[] dx = { 0, -1, 0, 1, -1, -1, 1, 1 }; // 8个方位的X坐标偏移
    private final static int[] dy = { -1, 0, 1, 0, 1, -1, -1, 1 }; // 8个方位的Y坐标偏移
    private final static int COST_STRAIGHT = 10; // 垂直方向或水平方向移动的路径评分
    private final static int COST_DIAGONAL = 14; // 斜方向移动的路径评分
    private final int[][] map; // 地图数据，1表示障碍，0表示可移动
    private final int row, col; // 地图行、列数

    /**
     * @param map 地图数据，1表示障碍，0表示可移动
     */
    public AStarPathFind(int[][] map) {
        this.map = Objects.requireNonNull(map);
        row = map.length;
        col = map[0].length;
    }

    /**
     * 搜索路径
     * 
     * @param srcX 起始点X坐标
     * @param srcY 起始点Y坐标
     * @param dstX 终点X坐标
     * @param dstY 终点Y坐标
     * @return 对该list逆序遍历可得出正向路径，返回空list表示没有搜索到可行路径
     */
    public List<Point> search(int srcX, int srcY, int dstX, int dstY) {
        if (dstX < 0 || dstX >= row || dstY < 0 || dstY >= col) {
            return Collections.emptyList();
        }
        if (translate(dstX, dstY) == BARRIER) {
            return Collections.emptyList();
        }

        Point start = new Point(srcX, srcY);
        Point end = new Point(dstX, dstY);
        Point currentPoint = new Point(start.x, start.y);
        boolean flag = true;
        ArrayList<Point> openTable = new ArrayList<>();
        ArrayList<Point> closeTable = new ArrayList<>();
        List<Point> searchResult = new ArrayList<>();

        while (flag) {
            for (int i = 0; i < 8; i++) {
                int fx = currentPoint.x + dx[i];
                int fy = currentPoint.y + dy[i];
                if (fx < 0 || fx >= row || fy < 0 || fy >= col) {
                    continue; // 越界了
                }
                if (translate(fx, fy) == BARRIER) { // 这个点是障碍物
                    continue;
                } else {
                    Point tempPoint = new Point(fx, fy);
                    if (end.equals(tempPoint)) {
                        flag = false;
                        end.parent = currentPoint;
                        break;
                    }
                    if (i < 4) {
                        tempPoint.G = currentPoint.G + COST_STRAIGHT;
                    } else {
                        tempPoint.G = currentPoint.G + COST_DIAGONAL;
                    }
                    tempPoint.H = Point.getDis(tempPoint, end);
                    tempPoint.F = tempPoint.G + tempPoint.H;
                    int pos = openTable.indexOf(tempPoint);
                    if (pos > -1) {
                        Point temp = openTable.get(pos);
                        if (temp.F > tempPoint.F) {
                            openTable.remove(pos);
                            openTable.add(tempPoint);
                            tempPoint.parent = currentPoint;
                        }
                    } else if ((pos = closeTable.indexOf(tempPoint)) > -1) {
                        Point temp = closeTable.get(pos);
                        if (temp.F > tempPoint.F) {
                            closeTable.remove(pos);
                            openTable.add(tempPoint);
                            tempPoint.parent = currentPoint;
                        }
                    } else {
                        openTable.add(tempPoint);
                        tempPoint.parent = currentPoint;
                    }
                }
            }

            if (flag == false) {
                break;
            }
            if (openTable.isEmpty()) {
                return searchResult;
            }
            openTable.remove(currentPoint);
            closeTable.add(currentPoint);
            Collections.sort(openTable);
            currentPoint = openTable.get(0);
        }
        Point node = end;
        while (node.parent != null) {
            searchResult.add(node);
            node = node.parent;
        }
        return searchResult;
    }

    /**
     * 坐标转换为二维数组元素
     * 
     * @param x
     * @param y
     * @return
     */
    private int translate(int x, int y) {
        return map[y][x];
    }

    public static class Point implements Comparable<Point>
    {
        int x;
        int y;
        Point parent;
        int F, G, H;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public static int getDis(Point p1, Point p2) {
            int dis = Math.abs(p1.x - p2.x) * 10 + Math.abs(p1.y - p2.y) * 10;
            return dis;
        }

        @Override
        public int compareTo(Point o) {
            return this.F - o.F;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Point && ((Point) obj).x == this.x && ((Point) obj).y == this.y;
        }

        @Override
        public int hashCode() {
            return x + y + parent.hashCode() + F + G + H;
        }

        @Override
        public String toString() {
            return "(" + this.x + "," + this.y + ")";
        }
    }
}
