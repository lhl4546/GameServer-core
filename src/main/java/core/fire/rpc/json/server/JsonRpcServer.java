/**
 * 
 */
package core.fire.rpc.json.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.Component;
import core.fire.net.http.HttpDispatcher;
import core.fire.net.http.HttpServer;

/**
 * 基于JSON格式的RPC服务器，内含一个{@linkplain core.fire.net.http.HttpServer}
 * 
 * @author lihuoliang
 *
 */
public class JsonRpcServer implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(JsonRpcServer.class);
    private HttpServer server;
    private HttpDispatcher httpDispatcher;
    private RpcDispatcher rpcDispatcher;

    /**
     * @param port RPC服务端口
     * @param rpcHandlerScanPackages RPC处理器扫描包，多个包使用英文逗号分隔
     */
    public JsonRpcServer(int port, String rpcHandlerScanPackages) {
        this.httpDispatcher = new HttpDispatcher("");
        this.server = new HttpServer(httpDispatcher, port);
        this.rpcDispatcher = new RpcDispatcher(rpcHandlerScanPackages);
        registerRpcDispatcher();
    }

    /**
     * 注册rpc请求派发器，该操作将/rpc与RpcDispatcherHandler关联，所有发往/
     * rpc的请求都将被RpcDispatcherHandler处理
     */
    protected void registerRpcDispatcher() {
        httpDispatcher.addHandler("/rpc", rpcDispatcher);
    }

    @Override
    public void start() throws Exception {
        server.start();
        rpcDispatcher.start();
        LOG.debug("JsonRpcServer start");
    }

    @Override
    public void stop() throws Exception {
        server.stop();
        rpcDispatcher.stop();
        LOG.debug("JsonRpcServer stop");
    }
}
