package core.fire;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import core.fire.database.DBService;
import core.fire.executor.DispatcherHandler;
import core.fire.net.tcp.NettyServer;

/**
 * 应用启动器，可继承此类，建议子类实现单例模式以提供对Spring应用上下文的访问。 注意： 1. 具体工程必须import
 * LauncherConfig类， 2. 需要提供CoreConfiguration(或其子类)Spring bean 3.
 * 如需使用数据库访问，需要提供JDBCTemplate Spring bean
 * 
 * @author lhl
 *
 *         2016年5月30日 上午9:11:54
 */
public class Launcher implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(Launcher.class);
    /**
     * Spring应用上下文(基于注解扫描)
     */
    protected final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    @Override
    public final void start() {
        try {
            registerConfigClass(context);
            context.refresh();
            registerShutdownHook();
            doStart();
        } catch (Exception e) {
            LOG.error("Server start failed", e);
            System.exit(-1);
        }
    }

    /**
     * 子类重写该方法以注册自定义配置类
     * 
     * @param ctx
     */
    protected void registerConfigClass(AnnotationConfigApplicationContext ctx) {
    }

    /**
     * 注册系统关闭回调
     */
    private void registerShutdownHook() {
        Thread thread = new Thread(() -> stop(), "SHUTDOWN_HOOK");
        Runtime.getRuntime().addShutdownHook(thread);
        LOG.debug("Register shutdown hook");
    }

    /**
     * 启动组件，子类重写该方法以启动自定义组件。
     * <p>
     * core提供组件如下:
     * <ul>
     * <li>Timer</li>
     * <li>DBService</li>
     * <li>DispatcherHandler</li>
     * <li>NettyServer</li>
     * <li>HttpServer</li>
     * <li>HttpServerDispatcher</li>
     * <li>RPCServer</li>
     * </ul>
     * 
     * @throws Exception
     */
    protected void doStart() throws Exception {
    }

    @Override
    public final void stop() {
        try {
            doStop();
        } catch (Exception e) {
            LOG.error("Server stop failed", e);
        }
    }

    /**
     * 停止组件
     * 
     * @throws Exception
     */
    protected void doStop() throws Exception {
        context.getBean(NettyServer.class).stop();
        context.getBean(DispatcherHandler.class).stop();
        context.getBean(Timer.class).stop();
        context.getBean(DBService.class).stop();
    }
}
