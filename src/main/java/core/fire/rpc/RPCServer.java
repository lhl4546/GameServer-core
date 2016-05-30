/**
 * 
 */
package core.fire.rpc;

import java.lang.reflect.Constructor;
import java.util.List;

import org.apache.thrift.TMultiplexedProcessor;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.server.AbstractNonblockingServer;
import org.apache.thrift.server.TThreadedSelectorServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import core.fire.Component;
import core.fire.CoreConfiguration;
import core.fire.util.ClassUtil;
import core.fire.util.Util;

/**
 * @author lhl
 *
 *         2016年2月19日 下午3:24:04
 */
@org.springframework.stereotype.Component
public class RPCServer implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(RPCServer.class);
    private TMultiplexedProcessor multiplex;
    private TNonblockingServerTransport transport;
    private TProtocolFactory protocol;
    private AbstractNonblockingServer server;

    @Autowired
    private CoreConfiguration config;

    public RPCServer() {
        multiplex = new TMultiplexedProcessor();
        protocol = new TCompactProtocol.Factory();
    }

    @Override
    public void start() throws Exception {
        int port = config.getRpcPort();
        transport = new TNonblockingServerSocket(port);
        TThreadedSelectorServer.Args args = new TThreadedSelectorServer.Args(transport);
        args.processor(multiplex).protocolFactory(protocol).selectorThreads(2).workerThreads(1);
        server = new TThreadedSelectorServer(args);
        loadProcessor();
        // serve是个阻塞方法，启动线程调用避免阻塞后续操作
        new Thread(() -> server.serve(), "RPC_START").start();

        LOG.debug("RPC server start listen on port {}", port);
    }

    public void addProcessor(String serviceName, TProcessor processor) {
        multiplex.registerProcessor(serviceName, processor);
    }

    public void loadProcessor() throws Exception {
        String searchPackages = config.getRpcHandlerScanPackages();
        String[] packageArray = Util.split(searchPackages.trim(), ",");
        for (String onePackage : packageArray) {
            loadProcessor0(onePackage);
        }
    }

    /**
     * Scan specified package for RPC handler and add it to multiplex
     * 
     * @param searchPackage
     * @throws Exception
     */
    private void loadProcessor0(String searchPackage) throws Exception {
        List<Class<?>> classList = ClassUtil.getClasses(searchPackage);
        for (Class<?> handler : classList) {
            RPCHandler annotation = handler.getAnnotation(RPCHandler.class);
            if (annotation != null) {
                String serviceName = annotation.serviceName();
                Class<? extends TProcessor> processorType = annotation.processor();
                Class<?> iface = annotation.iface();
                if (!iface.isAssignableFrom(handler)) {
                    throw new IllegalStateException("Handler " + handler.getName() + " is not subclass of iface " + iface.getName());
                }
                Constructor<? extends TProcessor> constructor = processorType.getConstructor(iface);
                Object handlerinstance = handler.newInstance();
                TProcessor processor = constructor.newInstance(handlerinstance);
                addProcessor(serviceName, processor);
            }
        }
    }

    @Override
    public void stop() throws Exception {
        server.stop();

        LOG.debug("RPC server stop");
    }
}
