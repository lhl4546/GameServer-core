/**
 * 
 */
package core.fire.database;

import java.util.List;

import org.springframework.dao.DataAccessException;

/**
 * 数据访问接口
 * <p>
 * 约定：
 * <ul>
 * <li>数据表拥有一个int类型主键</li>
 * <li>不返回自增值</li>
 * <li>数据表包含一个任意受JDBC支持的类型的索引</li>
 * </ul>
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
     * @throws DataAccessException
     */
    void add(T t) throws DataAccessException;

    /**
     * 删除一条记录，根据主键
     * 
     * @param primaryKey
     * @throws DataAccessException
     */
    void delete(int primaryKey) throws DataAccessException;

    /**
     * 更新一条记录
     * 
     * @param t
     * @throws DataAccessException
     */
    void update(T t) throws DataAccessException;

    /**
     * 查询一条记录，根据主键
     * 
     * @param primaryKey
     * @return
     * @throws DataAccessException
     */
    T get(int primaryKey) throws DataAccessException;

    /**
     * 查询多条记录，根据索引
     * 
     * @param secondaryKey
     * @return
     * @throws DataAccessException
     */
    List<T> getBySecondaryKey(Object secondaryKey) throws DataAccessException;
}
