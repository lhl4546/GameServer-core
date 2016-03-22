/**
 * 
 */
package core.fire.rpc.pool;

import org.apache.thrift.transport.TSocket;

import core.fire.Config;

/**
 * @author lhl
 *
 *         2016年2月22日 下午3:41:04
 */
public class ConnectionManager
{
    private ConnectionProvider provider;

    public ConnectionManager() {
        this.provider = new ConnectionProviderImpl(Config.getString("RPC_SERVER_HOST"),
                Config.getInt("RPC_SERVER_PORT"));
    }

    public TSocket getConnection() {
        return provider.getConnection();
    }

    public void returnConnection(TSocket conn) {
        provider.returnConnection(conn);
    }
}
