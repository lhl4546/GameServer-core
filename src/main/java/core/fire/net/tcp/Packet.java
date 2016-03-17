/**
 * 
 */
package core.fire.net.tcp;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessage.Builder;

/**
 * 网络协议结构<br>
 * ++++++++++++++++++++++++<br>
 * 分隔符 + 包长 + 指令 + 包体<br>
 * short + short+short+short + long + byte[]<br>
 * ++++++++++++++++++++++++<br>
 * 分隔符用来分隔每个数据包，身份标识用来标识连接的身份
 * 
 * @author Administrator
 *
 */
public class Packet
{
    public static final short HEAD_SIZE = 6;
    public static final short FLAG = 9527;
    public short length; // 包长
    public short code; // 指令
    public byte[] body; // 包体

    Packet(short code) {
        this.code = code;
    }

    /**
     * 将Packet转换为Protobuf，<b>当packet包体为空时不要调用此方法</b>
     * 
     * @param pkt Packet对象
     * @param t 目标Protobuf对象
     * @return proto
     * @throws IllegalStateException
     */
    @SuppressWarnings("unchecked")
    public <T extends GeneratedMessage> T toProto(T t) throws IllegalStateException {
        try {
            return (T) t.newBuilderForType().mergeFrom(body).build();
        } catch (Exception e) {
            throw new IllegalStateException("Packet转Pb时出错:" + toString(), e);
        }
    }

    /**
     * 利用Protobuf构建Packet
     * 
     * @param code 协议号
     * @param builder 当需要构建空负载包(包体为空)时请将该参数传入null
     * @return packet
     */
    public static Packet from(short code, Builder<?> builder) {
        Packet packet = new Packet(code);
        if (builder != null) {
            packet.body = builder.build().toByteArray();
            packet.length += packet.body.length;
        }
        packet.length += HEAD_SIZE;
        return packet;
    }

    @Override
    public String toString() {
        return "Packet: [code = " + code + ", length = " + length + "]";
    }
}
