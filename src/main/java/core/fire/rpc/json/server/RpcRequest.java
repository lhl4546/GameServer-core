/**
 * 
 */
package core.fire.rpc.json.server;

/**
 * RPC请求包装器，封装RPC基本参数与调用方参数
 * 
 * @author lihuoliang
 *
 */
public class RpcRequest
{
    // RPC调用标识符
    private long id;
    // 方法名
    private String methodName;
    // 方法参数，具体类型
    private String methodParams;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodParams() {
        return methodParams;
    }

    public void setMethodParams(String methodParams) {
        this.methodParams = methodParams;
    }

    /**
     * 转换成HTTP请求参数格式
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("id=").append(id);
        sb.append("&methodname=").append(methodName);
        sb.append("&methodparams=").append(methodParams);
        return sb.toString();
    }
}
