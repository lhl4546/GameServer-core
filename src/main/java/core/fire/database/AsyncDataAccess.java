/**
 * 
 */
package core.fire.database;

import java.util.List;

import core.fire.async.Callback;

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
     * @param cb
     */
    void asyncAdd(T t, Callback<?> cb);

    /**
     * 异步删除一条记录，根据主键
     * 
     * @param primaryKey
     * @param cb
     */
    void asyncDelete(int primaryKey, Callback<?> cb);

    /**
     * 异步更新一条记录
     * 
     * @param t
     * @param cb
     */
    void asyncUpdate(T t, Callback<?> cb);

    /**
     * 异步查询一条记录，根据主键。操作结束后将触发回调
     * 
     * @param primaryKey
     * @param cb
     */
    void asyncGet(int primaryKey, Callback<T> cb);

    /**
     * 异步查询多条记录，根据索引。操作结束后将触发回调
     * 
     * @param secondaryKey
     * @param cb
     */
    void asyncGetBySecondaryKey(Object secondaryKey, Callback<List<T>> cb);
}
