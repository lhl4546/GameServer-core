package core.fire.database;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * JdbcTempate回调
 * 
 * @author lhl
 *
 *         2016年8月3日 下午5:45:12
 */
public interface JdbcTemplateCallback<T>
{
    /**
     * 使用jdbcTemplate实现自定义行为
     * 
     * @param jdbcTemplate
     * @return 可以返回一个对象
     * @throws DataAccessException
     */
    T doInJdbcTemplate(JdbcTemplate jdbcTemplate) throws DataAccessException;
}
