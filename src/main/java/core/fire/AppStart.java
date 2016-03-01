package core.fire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import core.fire.database.DBService;
import core.fire.executor.DispatcherHandler;
import core.fire.net.netty.NettyServer;

public class AppStart implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(AppStart.class);
    private AnnotationConfigApplicationContext app;

    static {
        ConfigParser.parseFromClassPath("app.properties", Config.class);
    }

    public static void main(String[] args) {
        AppStart demo = new AppStart();
        demo.start();
    }

    @Override
    public void start() {
        try {
            app = new AnnotationConfigApplicationContext(AppConfigExample.class);
            registerShutdownHook();
            app.getBean(Timer.class).start();
            app.getBean(DBService.class).start();
            app.getBean(DispatcherHandler.class).start();
            app.getBean(NettyServer.class).start();
            stop();
        } catch (Exception e) {
            LOG.error("Server start failed", e);
            stop();
        }
    }

    public void registerShutdownHook() {
        Thread thread = new Thread(() -> stop(), "SHUTDOWN_HOOK");
        Runtime.getRuntime().addShutdownHook(thread);
        LOG.debug("Register shutdown hook");
    }

    @Override
    public void stop() {
        try {
            app.getBean(NettyServer.class).stop();
            app.getBean(DispatcherHandler.class).stop();
            app.getBean(Timer.class).stop();
            app.getBean(DBService.class).stop();
        } catch (Exception e) {
            LOG.error("Server stop failed", e);
        }
    }
}
