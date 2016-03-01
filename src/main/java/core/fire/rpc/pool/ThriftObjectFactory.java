/**
 * 
 */
package core.fire.rpc.pool;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.thrift.transport.TSocket;

/**
 * @author lhl
 *
 *         2016年2月22日 下午3:30:08
 */
public class ThriftObjectFactory implements PoolableObjectFactory<TSocket>
{
    private String host;
    private int port;
    private int timeout;
    private static final int DEFAULT_TIMEOUT_IN_MILLIS = 10 * 1000;

    public ThriftObjectFactory(String host, int port) {
        this(host, port, DEFAULT_TIMEOUT_IN_MILLIS);
    }

    public ThriftObjectFactory(String host, int port, int timeout) {
        this.host = host;
        this.port = port;
        this.timeout = timeout;
    }

    @Override
    public TSocket makeObject() throws Exception {
        TSocket socket = new TSocket(host, port, timeout);
        socket.open();
        return socket;
    }

    @Override
    public void destroyObject(TSocket obj) throws Exception {
        if (obj.isOpen()) {
            obj.close();
        }
    }

    @Override
    public boolean validateObject(TSocket obj) {
        return obj.isOpen();
    }

    @Override
    public void activateObject(TSocket obj) throws Exception {
    }

    @Override
    public void passivateObject(TSocket obj) throws Exception {
    }
}
