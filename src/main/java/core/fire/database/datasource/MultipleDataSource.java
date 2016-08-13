/**
 * 
 */
package core.fire.database.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * 基于String类型的key实行数据源选择。
 * <p>
 * 对于需要使用多个数据源的情况，可以使用该数据源作为唯一数据源，根据不同key选择不同数据源
 * <p>
 * 用法:
 * <p>
 * 初始化数据源
 * 
 * <pre>
 * MultipleDataSource mds = new MultipleDataSource();
 * Map<Object, Object> dsmap = new HashMap<>();
 * dsmap.put("ds1", getDataSource1()); // 放置数据源1
 * dsmap.put("ds2", getDataSource2()); // 放置数据源2
 * mds.setTargetDataSources(dsmap); // 设置备选数据源
 * mds.setDefaultTargetDataSource(dsmap.get("ds1")); // 设置默认数据源，当未指定数据源时默认使用该数据源
 * </pre>
 * 
 * 使用数据源
 * 
 * <pre>
 * DataSourceContext.choose("ds1");
 * Connection conn = mds.getConnection();
 * do something with conn
 * close conn
 * </pre>
 * 
 * 如上，获取连接前需先调用DataSourceContext.choose方法选择数据源
 * <p>
 * 注意，<tt>setDefaultTargetDataSource</tt>方法建议使用<tt>DataSource</tt>
 * 类型作为参数，否则需要额外设置 <tt>dataSourceLookup</tt>。
 * 
 * @author lihuoliang
 *
 */
public class MultipleDataSource extends AbstractRoutingDataSource
{
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContext.get();
    }
}
