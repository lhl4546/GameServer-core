package core.fire.rpc.pool;

import org.apache.thrift.transport.TSocket;

/**
 * 
 * @author lhl
 *
 *         2016年2月22日 下午3:18:51
 */
public interface ConnectionProvider
{
    /**
     * 获取连接
     * 
     * @return
     */
    TSocket getConnection();

    /**
     * 收回连接
     * 
     * @param conn
     */
    void returnConnection(TSocket conn);
}
