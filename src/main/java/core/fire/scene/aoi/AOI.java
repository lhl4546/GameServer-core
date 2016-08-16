package core.fire.scene.aoi;

/**
 * AOI功能接口
 * 
 * @author lhl
 *
 */
public interface AOI
{
    /**
     * 进入场景
     * 
     * @param id
     * @param x
     * @param y
     * @param distance
     */
    void enter(int id, int x, int y, int distance);

    /**
     * 离开场景
     * 
     * @param id
     */
    void leave(int id);

    /**
     * 场景内移动
     * 
     * @param id
     * @param x
     * @param y
     */
    void move(int id, int x, int y);
}
