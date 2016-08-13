/**
 * 
 */
package core.fire.database.datasource;

/**
 * 数据源选择器。根据String类型key选择数据源
 * 
 * @author lihuoliang
 *
 */
public class DataSourceContext
{
    /** 使用ThreadLocal避免线程安全问题 */
    private static final ThreadLocal<String> DATASOURCE_LOOKUP_KEY = new ThreadLocal<>();

    /**
     * 选择数据源
     * 
     * @param dataSourceLookupKey
     */
    public static void choose(String dataSourceLookupKey) {
        DATASOURCE_LOOKUP_KEY.set(dataSourceLookupKey);
    }

    /**
     * 获取当前选择的数据源
     * 
     * @return
     */
    public static String get() {
        return DATASOURCE_LOOKUP_KEY.get();
    }

    /**
     * 清楚当前的选择
     */
    public static void clear() {
        DATASOURCE_LOOKUP_KEY.remove();
    }
}
