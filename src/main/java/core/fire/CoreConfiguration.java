package core.fire;

/**
 * 核心配置类，继承此类以提供更多配置
 * 
 * @author lhl
 *
 *         2016年5月30日 上午10:27:39
 */
public class CoreConfiguration
{
    /** tcp服务器监听端口 */
    private int tcpPort;
    /** rpc服务器监听端口 */
    private int rpcPort;
    /** http服务器监听端口 */
    private int httpPort;

    /** tcp协议处理器扫描包 */
    private String tcpHandlerScanPackages;
    /** rpc协议处理器扫描包 */
    private String rpcHandlerScanPackages;
    /** http协议处理器扫描包 */
    private String httpHandlerScanPackages;

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public int getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(int rpcPort) {
        this.rpcPort = rpcPort;
    }

    public String getTcpHandlerScanPackages() {
        return tcpHandlerScanPackages;
    }

    public void setTcpHandlerScanPackages(String tcpHandlerScanPackages) {
        this.tcpHandlerScanPackages = tcpHandlerScanPackages;
    }

    public String getRpcHandlerScanPackages() {
        return rpcHandlerScanPackages;
    }

    public void setRpcHandlerScanPackages(String rpcHandlerScanPackages) {
        this.rpcHandlerScanPackages = rpcHandlerScanPackages;
    }

    public int getHttpPort() {
        return httpPort;
    }

    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    public String getHttpHandlerScanPackages() {
        return httpHandlerScanPackages;
    }

    public void setHttpHandlerScanPackages(String httpHandlerScanPackages) {
        this.httpHandlerScanPackages = httpHandlerScanPackages;
    }
}
