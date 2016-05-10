/**
 * 
 */
package core.fire.database;

import java.util.List;

/**
 * 数据访问接口
 * <p>
 * 约定：
 * <ol>
 * <li>数据表拥有一个int类型主键</li>
 * <li>主键需要在实体中赋值，否则将无法获得自增返回值</li>
 * <li>数据表包含一个任意类型的索引</li>
 * </ol>
 * 
 * @author lhl
 *
 *         2016年5月9日 下午2:38:15
 */
public interface DataAccess<T>
{
    /**
     * 增加一条记录
     * 
     * @param t
     */
    void add(T t);

    /**
     * 删除一条记录，根据主键
     * 
     * @param primaryKey
     */
    void delete(int primaryKey);

    /**
     * 更新一条记录
     * 
     * @param t
     */
    void update(T t);

    /**
     * 查询一条记录，根据主键
     * 
     * @param primaryKey
     * @return
     */
    T get(int primaryKey);

    /**
     * 查询多条记录，根据索引
     * 
     * @param secondaryKey
     * @return
     */
    List<T> getBySecondaryKey(Object secondaryKey);
}
