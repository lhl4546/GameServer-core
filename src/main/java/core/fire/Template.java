/**
 * 
 */
package core.fire;

/**
 * 支持多次加载的数值模版接口。 使用场景示例：
 * <p>
 * 
 * <pre>
 * class ConfigDataTemplate implements Template
 * {
 *     private Map<String, ConfigBean> config;
 * 
 *     public void load() {
 *         List<ConfigBean> t = DataParser.parse("data/config.txt", ConfigBean.class);
 *         Map<String, ConfigBean> tmp = new HashMap<>();
 *         t.foreach(x -> tmp.put(x.getId(), x));
 *         this.config = tmp;
 *     }
 * }
 * </pre>
 * 
 * 注意上例中config变量是在load方法中赋值的，这也给数值重新加载提供了可能，重新加载操作只需再次调用load方法即可达到更新config的目的，
 * 因为只是config的引用被更新了。
 * 从程序上来说这个操作是线程安全的，但是从业务上来说就不一定，比如当某个地方正从config中获取数据，而此时执行了一次加载导致config的引用被更新，
 * 那么再次从config中获取的数据就是更新后的数据(同一个key可能对应了不同的值)，如果恰好业务上下文依赖这个值的话可能会导致结果不一致。
 * 
 * @author lihuoliang
 *
 */
public interface Template
{
    void load();
}
