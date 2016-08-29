/**
 * 
 */
package core.fire.hash;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * 一致性HASH
 * 
 * <p>
 * 将物理节点按hash值分散到不同虚拟节点，虚拟节点分散越均匀越好
 * 
 * @author lihuoliang
 *
 */
public class ConsistencyHash<S> implements Shardable<S>
{
    // 虚拟节点，key=节点hash值，value=节点，按照节点hash值正序排序
    private TreeMap<Long, S> nodes;
    // 物理节点
    private List<S> shards;
    // 每个物理节点映射的虚拟节点数
    private static final int VIRTUAL_NODE_NUM = 100;

    /**
     * 使用物理节点列表作为参数初始化一致性hash
     * 
     * @param shards 物理节点
     */
    public ConsistencyHash(List<S> shards) {
        if (shards == null || shards.isEmpty()) {
            throw new IllegalArgumentException("at least one shard is required");
        }
        this.shards = shards;
        init();
    }

    /**
     * 初始化虚拟节点
     */
    private void init() {
        nodes = new TreeMap<Long, S>();
        for (int i = 0; i < shards.size(); ++i) {
            S shardInfo = shards.get(i);
            for (int n = 0; n < VIRTUAL_NODE_NUM; n++) {
                nodes.put(hash("shard-" + i + "-node-" + n), shardInfo);
            }
        }
    }

    /**
     * 查找参数{@code key}对应的分片。
     * 
     * <p>
     * 查找方法为首先取得{@code key}的hash值，然后获得虚拟节点中所有key大于等于hash值的子集，然后返回此子集中的第一个元素，即是参数
     * {@code key} 对应的分片
     * 
     * @param key 关键字
     * @return 返回参数对应的分片
     */
    @Override
    public S getShardInfo(String key) {
        SortedMap<Long, S> tail = nodes.tailMap(hash(key));
        if (tail.size() == 0) {
            return nodes.firstEntry().getValue();
        }
        return tail.get(tail.firstKey());
    }

    /**
     * http://murmurhash.googlepages.com/
     * <p>
     * MurMurHash算法
     * 
     * @param key
     * @return
     */
    private long hash(String key) {
        ByteBuffer buf = ByteBuffer.wrap(key.getBytes());
        int seed = 0x1234ABCD;

        ByteOrder byteOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        long m = 0xc6a4a7935bd1e995L;
        int r = 47;

        long h = seed ^ (buf.remaining() * m);

        long k;
        while (buf.remaining() >= 8) {
            k = buf.getLong();

            k *= m;
            k ^= k >>> r;
            k *= m;

            h ^= k;
            h *= m;
        }

        if (buf.remaining() > 0) {
            ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
            // for big-endian version, do this first:
            // finish.position(8-buf.remaining());
            finish.put(buf).rewind();
            h ^= finish.getLong();
            h *= m;
        }

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        buf.order(byteOrder);
        return h;
    }
}
