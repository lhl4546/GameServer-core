/**
 * 
 */
package core.fire.push.ios;

import javapns.notification.PushNotificationPayload;

/**
 * @author lihuoliang
 *
 */
public class IOSPushPayload
{
    /** 真实payload */
    private PushNotificationPayload payload;

    private IOSPushPayload() {
    }

    /**
     * 获取推送真实payload
     * 
     * @return
     */
    public PushNotificationPayload getPayload() {
        return payload;
    }

    public static class IOSPushPayloadBuilder
    {
        String message;
        int badge;
        String sound;

        public static IOSPushPayloadBuilder newBuilder() {
            return new IOSPushPayloadBuilder();
        }

        /**
         * 设置推送消息文本内容
         * 
         * @param message
         * @return
         */
        public IOSPushPayloadBuilder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * 设置图标显示数字
         * 
         * @param badge
         * @return
         */
        public IOSPushPayloadBuilder badge(int badge) {
            this.badge = badge;
            return this;
        }

        /**
         * 设置提示声音
         * 
         * @param sound
         * @return
         */
        public IOSPushPayloadBuilder sound(String sound) {
            this.sound = sound;
            return this;
        }

        public IOSPushPayload build() {
            IOSPushPayload payloadHolder = new IOSPushPayload();
            payloadHolder.payload = PushNotificationPayload.combined(message, badge, sound);
            return payloadHolder;
        }
    }
}
