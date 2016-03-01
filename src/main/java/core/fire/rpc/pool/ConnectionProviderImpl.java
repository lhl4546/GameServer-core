package core.fire.rpc.pool;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.thrift.transport.TSocket;

/**
 * 
 * @author lhl
 *
 *         2016年2月22日 下午3:21:27
 */
public class ConnectionProviderImpl implements ConnectionProvider
{
    private ObjectPool<TSocket> objectPool;

    public ConnectionProviderImpl(String host, int port) {
        PoolableObjectFactory<TSocket> objectFactory = new ThriftObjectFactory(host, port);
        GenericObjectPool.Config config = new GenericObjectPool.Config();
        objectPool = new GenericObjectPool<>(objectFactory, config);
    }

    @Override
    public TSocket getConnection() {
        try {
            return objectPool.borrowObject();
        } catch (Exception e) {
            throw new RuntimeException("Can not get connection", e);
        }
    }

    @Override
    public void returnConnection(TSocket conn) {
        try {
            objectPool.returnObject(conn);
        } catch (Exception e) {
            throw new RuntimeException("Can not return to pool", e);
        }
    }

}
