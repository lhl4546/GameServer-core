/**
 * 
 */
package core.fire.push.ios;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.util.Util;
import javapns.Push;
import javapns.communication.exceptions.KeystoreException;
import javapns.notification.PushNotificationPayload;
import javapns.notification.transmission.PushQueue;

/**
 * IOS推送实现
 * 
 * @author lhl
 *
 *         2016年8月5日 上午11:09:34
 */
public class IOSPushService
{
    private static final Logger LOG = LoggerFactory.getLogger(IOSPushService.class);
    private boolean isProductionEnvironment; // 推送环境(生产环境or沙盒环境)
    private String p12FilePath; // p12文件(以文件方式加载)
    private String p12Password; // p12密码
    private PushQueue pushQueue; // 推送队列，用于单条推送

    public boolean isProductionEnvironment() {
        return isProductionEnvironment;
    }

    public void setProductionEnvironment(boolean isProductionEnvironment) {
        this.isProductionEnvironment = isProductionEnvironment;
    }

    public String getP12FilePath() {
        return p12FilePath;
    }

    public void setP12FilePath(String p12FilePath) {
        this.p12FilePath = p12FilePath;
    }

    public String getP12Password() {
        return p12Password;
    }

    public void setP12Password(String p12Password) {
        this.p12Password = p12Password;
    }

    /**
     * 初始化推送队列
     */
    public void initialize() {
        try {
            pushQueue = QueuedNotificationPushThread.newPushQueue(p12FilePath, p12Password, isProductionEnvironment);
            pushQueue.start();
        } catch (KeystoreException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 推送单条消息
     * 
     * @param payload 消息负载
     * @param devicetoken 接收端标识符
     */
    public void pushOne(PushNotificationPayload payload, String devicetoken) {
        if (pushQueue == null) {
            LOG.warn("推送队列尚未初始化，无法发送单条推送");
            return;
        }

        try {
            pushQueue.add(payload, devicetoken);
        } catch (Exception e) {
            LOG.error("iOS推送发生异常", e);
        }
    }

    /**
     * 批量推送，所有接收方将收到同样的消息
     * 
     * @param payload 消息负载
     * @param devicetokens 接收端标识符
     */
    public void pushBatch(PushNotificationPayload payload, List<String> devicetokens) {
        if (Util.isNullOrEmpty(devicetokens)) {
            return;
        }

        try {
            Push.payload(payload, p12FilePath, p12Password, isProductionEnvironment, 8, devicetokens);
        } catch (Exception e) {
            LOG.error("iOS推送发生异常", e);
        }
    }
}
