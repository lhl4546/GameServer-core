/**
 * 
 */
package core.fire.rpc.pool;

import org.apache.thrift.transport.TSocket;

/**
 * @author lhl
 *
 *         2016年2月22日 下午3:41:04
 */
public class ConnectionManager
{
    private ConnectionProvider provider;

    public ConnectionManager(String host, int port) {
        this.provider = new ConnectionProviderImpl(host, port);
    }

    public TSocket getConnection() {
        return provider.getConnection();
    }

    public void returnConnection(TSocket conn) {
        provider.returnConnection(conn);
    }
}
