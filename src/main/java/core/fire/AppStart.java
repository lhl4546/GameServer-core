package core.fire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import core.fire.database.DBService;
import core.fire.executor.DispatcherHandler;
import core.fire.net.tcp.NettyServer;

public class AppStart implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(AppStart.class);
    public static final AppStart INSTANCE = new AppStart();
    public AnnotationConfigApplicationContext appCtx;

    private AppStart() {
    }

    public static void main(String[] args) {
        INSTANCE.start();
    }

    @Override
    public void start() {
        try {
            Config.parse("app.properties");
            appCtx = new AnnotationConfigApplicationContext(AppConfigExample.class);
            registerShutdownHook();
            appCtx.getBean(Timer.class).start();
            appCtx.getBean(DBService.class).start();
            appCtx.getBean(DispatcherHandler.class).start();
            appCtx.getBean(NettyServer.class).start();
            appCtx.registerShutdownHook();
            appCtx.publishEvent(new ComponentsReadyEvent(appCtx));
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
            appCtx.getBean(NettyServer.class).stop();
            appCtx.getBean(DispatcherHandler.class).stop();
            appCtx.getBean(Timer.class).stop();
            appCtx.getBean(DBService.class).stop();
        } catch (Exception e) {
            LOG.error("Server stop failed", e);
        }
    }
}
