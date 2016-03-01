/**
 * 
 */
package core.fire;

/**
 * 组件
 * 
 * @author lhl
 *
 *         2016年1月29日 下午6:14:45
 */
public interface Component
{
    /**
     * 启动组件
     */
    void start() throws Exception;

    /**
     * 停止组件
     */
    void stop() throws Exception;
}
