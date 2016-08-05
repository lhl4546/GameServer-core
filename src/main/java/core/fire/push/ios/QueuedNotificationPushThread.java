/**
 * 
 */
package core.fire.push.ios;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.Payload;
import javapns.notification.PayloadPerDevice;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushedNotifications;
import javapns.notification.transmission.NotificationThreads;
import javapns.notification.transmission.PushQueue;

/**
 * 单线程消息推送队列
 * 
 * @author lhl
 *
 *         2016年6月22日 上午10:15:08
 */
public class QueuedNotificationPushThread implements Runnable, PushQueue
{
    private static final Logger LOG = LoggerFactory.getLogger(QueuedNotificationPushThread.class);

    private static final int DEFAULT_MAXNOTIFICATIONSPERCONNECTION = 1 << 8; // 重连间隔(使用2的幂提高计算效率)
    private static final int DEFAULT_SLEEPMILLISBETWEENNOTIFICATION = 0; // 每个通知发送间隔(毫秒)

    private Thread thread;
    private boolean started = false;
    private PushNotificationManager notificationManager;
    private AppleNotificationServer server;
    private int maxNotificationsPerConnection = DEFAULT_MAXNOTIFICATIONSPERCONNECTION;
    private int sleepBetweenNotifications = DEFAULT_SLEEPMILLISBETWEENNOTIFICATION;
    private int nextMessageIdentifier = 1;
    private PushedNotifications notifications = new PushedNotifications();

    private BlockingQueue<PayloadPerDevice> messages = new LinkedBlockingQueue<>();

    /**
     * Create one thread in QUEUE mode, awaiting messages to push.
     * 
     * @param threads the parent NotificationThreads object that is coordinating
     *            multiple threads
     * @param notificationManager the notification manager to use
     * @param server the server to communicate with
     */
    public QueuedNotificationPushThread(NotificationThreads threads, PushNotificationManager notificationManager, AppleNotificationServer server) {
        this.thread = new Thread(threads, this, "JavaPNS" + (threads != null ? " grouped" : " standalone") + " notification thread in QUEUE mode");
        this.notificationManager = notificationManager == null ? new PushNotificationManager() : notificationManager;
        this.server = server;
        this.thread.setDaemon(true);
    }

    /**
     * Create a standalone thread in QUEUE mode, awaiting messages to push.
     * 
     * @param server the server to communicate with
     */
    public QueuedNotificationPushThread(AppleNotificationServer server) {
        this(null, new PushNotificationManager(), server);
    }

    public static QueuedNotificationPushThread newPushQueue(Object keystore, String password, boolean production) throws KeystoreException {
        AppleNotificationServer server = new AppleNotificationServerBasicImpl(keystore, password, production);
        return new QueuedNotificationPushThread(server);
    }

    /**
     * Start the transmission thread.
     * 
     * This method returns immediately, as the thread starts working on its own.
     */
    public QueuedNotificationPushThread start() {
        if (started)
            return this;
        started = true;
        this.thread.start();
        return this;
    }

    @Override
    public void run() {
        runQueue();
    }

    private void runQueue() {
        try {
            notificationManager.initializeConnection(server);
        } catch (KeystoreException | CommunicationException e) {
            LOG.error("iOS队列推送模式连接异常", e);
            return;
        }

        int notificationsPushed = 0;
        while (true) {
            try {
                PayloadPerDevice message = messages.take();
                if (message == null) {
                    continue;
                }

                notificationsPushed++;
                int messageId = newMessageIdentifier();
                notificationManager.sendNotification(message.getDevice(), message.getPayload(), false, messageId);
                if ((notificationsPushed & (maxNotificationsPerConnection - 1)) == 0) {
                    restartConnection();
                } else {
                    if (sleepBetweenNotifications > 0) {
                        sleep(sleepBetweenNotifications);
                    }
                }
            } catch (Throwable t) {
                LOG.error("iOS队列推送模式异常", t);
                sleep(10 * 1000);
            }
        }
    }

    private void sleep(int millis) {
        try {
            TimeUnit.MILLISECONDS.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    private void restartConnection() {
        try {
            LOG.debug("开始重启iOS队列推送模式连接");
            notificationManager.restartConnection(server);
            LOG.debug("重启iOS队列推送模式连接完成");
        } catch (CommunicationException | KeystoreException e) {
            LOG.error("重启iOS队列推送模式连接失败，下次再试", e);
        }
    }

    @Override
    public PushQueue add(Payload payload, String token) throws InvalidDeviceTokenFormatException {
        return add(new PayloadPerDevice(payload, token));
    }

    @Override
    public PushQueue add(Payload payload, Device device) {
        return add(new PayloadPerDevice(payload, device));
    }

    @Override
    public PushQueue add(PayloadPerDevice message) {
        messages.add(message);
        return this;
    }

    /**
     * Return a new sequential message identifier.
     * 
     * @return a message identifier unique to all NotificationThread objects
     */
    public int newMessageIdentifier() {
        return nextMessageIdentifier++;
    }

    /**
     * 这里不存储通知，取到的将是空List
     * <p>
     * {@inheritDoc}
     */
    @Override
    public PushedNotifications getPushedNotifications() {
        return notifications;
    }

    @Override
    public void clearPushedNotifications() {
    }

    /**
     * 这里返回的是个空List
     * <p>
     * {@inheritDoc}
     */
    @Override
    public List<Exception> getCriticalExceptions() {
        return Collections.emptyList();
    }
}
