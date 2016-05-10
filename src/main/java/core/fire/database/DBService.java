/**
 * 
 */
package core.fire.database;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import core.fire.Component;
import core.fire.NamedThreadFactory;
import core.fire.util.BaseUtil;

/**
 * @author lhl
 *
 *         2016年2月16日 下午3:45:01
 */
@org.springframework.stereotype.Component
public class DBService implements Component
{
    private static final Logger LOG = LoggerFactory.getLogger(DBService.class);
    private ExecutorService worker;

    public DBService() {
        worker = Executors.newSingleThreadExecutor(new NamedThreadFactory("DBService"));
    }

    public void addTask(Runnable task) {
        worker.submit(task);
    }

    @Override
    public void start() throws Exception {
        LOG.debug("DBService start");
    }

    /**
     * 该组件应当在DispatcherHandler停止后再停止，否则可能导致数据丢失
     */
    @Override
    public void stop() throws Exception {
        BaseUtil.shutdownThreadPool(worker, 10 * 1000);
        LOG.debug("DBService stop");
    }
}
