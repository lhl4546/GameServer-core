package core.fire.push.ios;

import java.util.List;

import core.fire.push.Push;
import javapns.notification.PushNotificationPayload;

/**
 * 批量推送
 * 
 * @author lhl
 *
 *         2016年8月5日 上午11:25:33
 */
public class IOSBatchPush implements Push
{
    private PushNotificationPayload payload;
    private List<String> devicetokens;
    private IOSPushService iosPushService;

    public IOSBatchPush() {
    }

    @Override
    public void push() {
        iosPushService.pushBatch(payload, devicetokens);
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
     * @param devicetokens
     */
    public void setDevicetokens(List<String> devicetokens) {
        this.devicetokens = devicetokens;
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
