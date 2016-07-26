package core.fire.net.tcp;

import com.google.protobuf.Message;

import core.fire.Dumpable;

/**
 * 数据包通用接口
 * 
 * @author lihuoliang
 *
 */
public interface IPacket extends Dumpable
{
    /**
     * @return 返回数据包标识符
     */
    short getCode();

    /**
     * 设置数据包标识符
     * 
     * @param code
     */
    void setCode(short code);

    /**
     * @return 返回数据包体
     */
    byte[] getBody();

    /**
     * 设置数据包体
     * 
     * @param body
     */
    void setBody(byte[] body);

    /**
     * 转换数据包体为具体Proto类型
     * 
     * @param t
     * @return
     */
    <T extends Message> T toProto(T t);
}