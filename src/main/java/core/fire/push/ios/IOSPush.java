package core.fire.push.ios;

import core.fire.push.Push;
import javapns.notification.PushNotificationPayload;

/**
 * 单个推送
 * 
 * @author lhl
 *
 *         2016年8月5日 上午11:25:42
 */
public class IOSPush implements Push
{
    private PushNotificationPayload payload;
    private String devicetoken;
    private IOSPushService iosPushService;

    public IOSPush() {
    }

    @Override
    public void push() {
        iosPushService.pushOne(payload, devicetoken);
    }

    /**
     * 设置推送消息负载
     * 
     * @param payload
     */
    public void setPayload(PushNotificationPayload payload) {
        this.payload = payload;
    }

    /**
     * 设置接收端设备标识符
     * 
     * @param devicetoken
     */
    public void setDevicetoken(String devicetoken) {
        this.devicetoken = devicetoken;
    }

    /**
     * 设置推送服务实现
     * 
     * @param iosPushService
     */
    public void setIosPushService(IOSPushService iosPushService) {
        this.iosPushService = iosPushService;
    }
}
