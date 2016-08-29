/**
 * 
 */
package core.fire.hash;

/**
 * 分片接口
 * 
 * @author lihuoliang
 *
 */
public interface Shardable<S>
{
    /**
     * 获取分片
     * 
     * @param key 分片关键字
     * @return 结果分片
     */
    S getShardInfo(String key);
}
