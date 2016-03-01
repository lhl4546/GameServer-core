/**
 * 
 */
package core.fire;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.BoneCPDataSource;

/**
 * @author lhl
 *
 *         2016年2月24日 下午5:14:36
 */
@Configuration
@ComponentScan("core.fire")
public class AppConfigExample
{
    public DataSource getDataSource() {
        // 从类路径下加载bonecp-config.xml配置以覆盖默认配置
        // 默认配置位于jar包中
        BoneCPConfig config = new BoneCPConfig();
        DataSource datasource = new BoneCPDataSource(config);
        return datasource;
    }

    @Bean
    public JdbcTemplate getJdbcTemplate() {
        DataSource datasource = getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
        return jdbcTemplate;
    }
}
