/**
 * 
 */
package core.fire.rpc;

import org.apache.thrift.protocol.TProtocol;

/**
 * RPC调用接口
 * 
 * @author lhl
 *
 *         2016年2月25日 下午3:13:40
 */
public interface RPCAction
{
    /**
     * 
     * @param prot
     * @throws Exception
     */
    void action(TProtocol prot) throws Exception;
}
