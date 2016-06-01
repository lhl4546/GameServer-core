/**
 * 
 */
package core.fire.database;

import core.fire.Callback;

/**
 * 异步数据访问接口
 * 
 * @author lhl
 *
 *         2016年5月10日 下午2:33:11
 */
public interface AsyncDataAccess<T> extends DataAccess<T>
{
    /**
     * 异步增加一条记录
     * 
     * @param t
     */
    void asyncAdd(T t);

    /**
     * 异步删除一条记录，根据主键
     * 
     * @param primaryKey
     */
    void asyncDelete(int primaryKey);

    /**
     * 异步更新一条记录
     * 
     * @param t
     */
    void asyncUpdate(T t);

    /**
     * 异步查询一条记录，根据主键。操作结束后将触发回调
     * 
     * @param primaryKey
     * @param cb
     */
    void asyncGet(int primaryKey, Callback cb);

    /**
     * 异步查询多条记录，根据索引。操作结束后将触发回调
     * 
     * @param secondaryKey
     * @param cb
     */
    void asyncGetBySecondaryKey(Object secondaryKey, Callback cb);
}
