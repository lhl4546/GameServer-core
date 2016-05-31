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

    /** tcp协议处理器扫描包，允许用英文逗号分隔多个包名 */
    private String tcpHandlerScanPackages;
    /** tcp协议拦截器扫描包，允许用英文逗号分隔多个包名 */
    private String tcpInterceptorScanPackages;
    /** tcp协议过滤器扫描包，允许用英文逗号分隔多个包名 */
    private String tcpFilterScanPackages;
    /** rpc协议处理器扫描包，允许用英文逗号分隔多个包名 */
    private String rpcHandlerScanPackages;
    /** http协议处理器扫描包，允许用英文逗号分隔多个包名 */
    private String httpHandlerScanPackages;

    /**
     * tcp服务器监听端口
     * 
     * @return
     */
    public int getTcpPort() {
        return tcpPort;
    }

    /**
     * tcp服务器监听端口
     * 
     * @param tcpPort
     */
    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    /**
     * rpc服务器监听端口
     * 
     * @return
     */
    public int getRpcPort() {
        return rpcPort;
    }

    /**
     * rpc服务器监听端口
     * 
     * @param rpcPort
     */
    public void setRpcPort(int rpcPort) {
        this.rpcPort = rpcPort;
    }

    /**
     * tcp协议处理器扫描包，允许用英文逗号分隔多个包名
     * 
     * @return
     */
    public String getTcpHandlerScanPackages() {
        return tcpHandlerScanPackages;
    }

    /**
     * tcp协议处理器扫描包，允许用英文逗号分隔多个包名
     * 
     * @param tcpHandlerScanPackages
     */
    public void setTcpHandlerScanPackages(String tcpHandlerScanPackages) {
        this.tcpHandlerScanPackages = tcpHandlerScanPackages;
    }

    /**
     * rpc协议处理器扫描包，允许用英文逗号分隔多个包名
     * 
     * @return
     */
    public String getRpcHandlerScanPackages() {
        return rpcHandlerScanPackages;
    }

    /**
     * rpc协议处理器扫描包，允许用英文逗号分隔多个包名
     * 
     * @param rpcHandlerScanPackages
     */
    public void setRpcHandlerScanPackages(String rpcHandlerScanPackages) {
        this.rpcHandlerScanPackages = rpcHandlerScanPackages;
    }

    /**
     * http服务器监听端口
     * 
     * @return
     */
    public int getHttpPort() {
        return httpPort;
    }

    /**
     * http服务器监听端口
     * 
     * @param httpPort
     */
    public void setHttpPort(int httpPort) {
        this.httpPort = httpPort;
    }

    /**
     * http协议处理器扫描包，允许用英文逗号分隔多个包名
     * 
     * @return
     */
    public String getHttpHandlerScanPackages() {
        return httpHandlerScanPackages;
    }

    /**
     * http协议处理器扫描包，允许用英文逗号分隔多个包名
     * 
     * @param httpHandlerScanPackages
     */
    public void setHttpHandlerScanPackages(String httpHandlerScanPackages) {
        this.httpHandlerScanPackages = httpHandlerScanPackages;
    }

    /**
     * tcp协议拦截器扫描包，允许用英文逗号分隔多个包名
     * 
     * @return
     */
    public String getTcpInterceptorScanPackages() {
        return tcpInterceptorScanPackages;
    }

    /**
     * tcp协议拦截器扫描包，允许用英文逗号分隔多个包名
     * 
     * @param tcpInterceptorScanPackages
     */
    public void setTcpInterceptorScanPackages(String tcpInterceptorScanPackages) {
        this.tcpInterceptorScanPackages = tcpInterceptorScanPackages;
    }

    /**
     * tcp协议过滤器扫描包，允许用英文逗号分隔多个包名
     * 
     * @return
     */
    public String getTcpFilterScanPackages() {
        return tcpFilterScanPackages;
    }

    /**
     * tcp协议过滤器扫描包，允许用英文逗号分隔多个包名
     * 
     * @param tcpFilterScanPackages
     */
    public void setTcpFilterScanPackages(String tcpFilterScanPackages) {
        this.tcpFilterScanPackages = tcpFilterScanPackages;
    }
}
