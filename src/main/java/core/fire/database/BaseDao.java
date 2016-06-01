/**
 * 
 */
package core.fire.database;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import core.fire.Callback;

/**
 * 基于Spring {@linkplain org.springframework.jdbc.core.JdbcTemplate} 和
 * {@linkplain org.apache.commons.dbutils.BeanProcessor}实现的ORM。
 * {@code JdbcTemplate}只与SQL和{@code ResultSet}取值器打交道，那么该类就首先利用
 * Java反射原理生成操作SQL与SQL参数，然后利用{BeanProcessor}实现{@code ResultSet} 到实体类的转换。
 * 
 * @author lhl
 *
 *         2016年2月24日 上午10:00:50
 */
public abstract class BaseDao<T> implements AsyncDataAccess<T>
{
    // SQL模版
    private static final String SQL_UPDATE = "UPDATE $table SET $assignment WHERE $primarykey=?";
    private static final String SQL_INSERT = "INSERT INTO $table ($keys) VALUES ($values)";
    private static final String SQL_DELETE_BY_PRIMARY_KEY = "DELETE FROM $table WHERE $primarykey=?";
    private static final String SQL_SELECT_BY_PRIMARY_KEY = "SELECT * FROM $table WHERE $primarykey=?";
    private static final String SQL_SELECT_BY_SECOND_KEY = "SELECT * FROM $table WHERE $secondkey=?";

    // SQL实例
    private String sql_update; // 更新SQL
    private String sql_insert; // 插入SQL
    private String sql_delete; // 删除SQL
    private String sql_select_by_primary_key; // 主键查询SQL
    private String sql_select_by_second_key; // 索引查询SQL

    private Field[] tableField; // 数据库表字段
    private Field primaryKey; // 主键
    private Field secondaryKey; // 索引
    private Class<?> type; // 实体类型
    private String tableName; // 数据库表名

    private final BeanProcessor beanProcessor = new BeanProcessor();

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DBService dbService;

    protected BaseDao(Class<T> type) {
        this.type = Objects.requireNonNull(type);
    }

    @PostConstruct
    public void initialize() {
        initializeTableName();
        initializeColumns();
        initializeSQL();
    }

    /**
     * 初始化数据库表名
     */
    private void initializeTableName() {
        Table tableAnnotation = type.getAnnotation(Table.class);
        if (tableAnnotation == null)
            throw new IllegalStateException("Table annotation not found for type " + type.getName());

        tableName = tableAnnotation.value();
        getLogger().debug("table name is [{}]", tableName);
    }

    /**
     * 初始化数据库表字段对应的类字段
     */
    private void initializeColumns() {
        Class<?> c = this.type;
        Field[] fields = c.getDeclaredFields();
        List<Field> list = new ArrayList<>();
        for (Field field : fields) {
            if (field.isAnnotationPresent(TableField.class)) {
                field.setAccessible(true);
                list.add(field);
            }
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                this.primaryKey = field;
            }
            if (field.isAnnotationPresent(SecondKey.class)) {
                this.secondaryKey = field;
            }
        }
        this.tableField = list.toArray(new Field[list.size()]);
        getLogger().debug("column is [{}]", dumpFields(list));
    }
    
    private String dumpFields(List<Field> fields) {
        StringBuilder builder = new StringBuilder();
        for (Field field : fields) {
            builder.append(field.getName()).append(", ");
        }
        return builder.substring(0, builder.length() - 1);
    }

    /**
     * 初始化SQL实例
     */
    private void initializeSQL() {
        initUpdateSQL();
        initInsertSQL();
        initDeleteSQL();
        initSelectSQL1();
        initSelectSQL2();
    }

    private void initUpdateSQL() {
        String assign = getAssignWithoutPrimaryKey();
        String sql = SQL_UPDATE.replace("$table", tableName);
        sql = sql.replace("$assignment", assign);
        sql = sql.replace("$primarykey", this.primaryKey.getName());
        this.sql_update = sql;
        getLogger().debug("update sql is [{}]", sql_update);
    }

    // 获取SQL赋值格式(key1=?,key2=?...)
    private String getAssignWithoutPrimaryKey() {
        StringBuilder sb = new StringBuilder();
        for (Field field : tableField) {
            if (field != primaryKey)
                sb.append(field.getName()).append("=?,");
        }
        return sb.substring(0, sb.length() - 1);
    }

    private void initInsertSQL() {
        String keys = getKeys();
        String values = getValues();
        String sql = SQL_INSERT.replace("$table", tableName);
        sql = sql.replace("$keys", keys);
        sql = sql.replace("$values", values);
        this.sql_insert = sql;
        getLogger().debug("insert sql is [{}]", sql_insert);
    }

    // 获取插入KEY
    private String getKeys() {
        StringBuilder sb = new StringBuilder();
        for (Field field : tableField) {
            sb.append(field.getName()).append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }

    // 获取插入参数格式(?,?...)
    private String getValues() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tableField.length; i++) {
            sb.append("?,");
        }
        return sb.substring(0, sb.length() - 1);
    }

    // 生成删除操作SQL
    private void initDeleteSQL() {
        String sql = SQL_DELETE_BY_PRIMARY_KEY.replace("$table", tableName);
        sql = sql.replace("$primarykey", this.primaryKey.getName());
        this.sql_delete = sql;
        getLogger().debug("delete sql is [{}]", sql_delete);
    }

    // 生成主键查询操作SQL
    private void initSelectSQL1() {
        String sql = SQL_SELECT_BY_PRIMARY_KEY.replace("$table", tableName);
        sql = sql.replace("$primarykey", this.primaryKey.getName());
        this.sql_select_by_primary_key = sql;
        getLogger().debug("select by primarykey sql is [{}]", sql_select_by_primary_key);
    }

    // 生成其他关键字查询操作SQL
    private void initSelectSQL2() {
        if (secondaryKey == null) {
            return;
        }

        String sql = SQL_SELECT_BY_SECOND_KEY.replace("$table", tableName);
        sql = sql.replace("$secondkey", this.secondaryKey.getName());
        this.sql_select_by_second_key = sql;
        getLogger().debug("select by secondarykey sql is [{}]", sql_select_by_second_key);
    }

    // 生成SQL参数
    protected Object[] getUpdateParam(Object obj) {
        Field[] fields = this.tableField;
        Object[] values = getFieldValue(obj);
        Object[] result = new Object[values.length];
        Object primaryKeyValue = null;
        int j = 0;
        for (int i = 0; i < values.length; i++) {
            if (fields[i] == primaryKey) {
                primaryKeyValue = values[i];
                continue;
            }
            result[j++] = values[i];
        }
        result[j] = primaryKeyValue;
        return result;
    }

    // 获取插入\更改操作参数
    protected Object[] getInsertParam(Object obj) {
        return getFieldValue(obj);
    }

    // 生成插入SQL参数列表
    private Object[] getFieldValue(Object obj) {
        Field[] fields = this.tableField;
        Object[] values = new Object[fields.length];
        try {
            for (int i = 0; i < values.length; i++) {
                Field field = fields[i];
                values[i] = field.get(obj);
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
        return values;
    }

    // Bean processor -----------------------------------------------------
    protected ResultSetExtractor<T> beanExtrator = new ResultSetExtractor<T>() {
        @Override
        public T extractData(ResultSet rs) throws SQLException, DataAccessException {
            if (!rs.next()) {
                return null;
            }

            return rsToEntity(rs);
        }
    };

    protected RowMapper<T> beanListExtractor = new RowMapper<T>() {
        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rsToEntity(rs);
        }
    };

    @SuppressWarnings("unchecked")
    protected T rsToEntity(ResultSet rs) throws SQLException {
        return (T) beanProcessor.toBean(rs, type);
    }

    // JDBC operation ------------------------
    protected JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Override
    public void add(T t) {
        Timer timer = Timer.start();
        String sql = sql_insert;
        Object param = getInsertParam(t);
        getJdbcTemplate().update(sql, param);
        timer.end();
        getLogger().debug("Table {}.add, time = {} ns", tableName, timer.time());
    }

    @Override
    public void delete(int primaryKey) {
        Timer timer = Timer.start();
        String sql = sql_delete;
        getJdbcTemplate().update(sql, new Object[] { primaryKey });
        timer.end();
        getLogger().debug("Table {}.delete, time = {} ns", tableName, timer.time());
    }

    @Override
    public void update(T t) {
        Timer timer = Timer.start();
        String sql = sql_update;
        Object[] param = getUpdateParam(t);
        getJdbcTemplate().update(sql, param);
        timer.end();
        getLogger().debug("Table {}.update, time = {} ns", tableName, timer.time());
    }

    @Override
    public T get(int primaryKey) {
        Timer timer = Timer.start();
        String sql = sql_select_by_primary_key;
        try {
            return getJdbcTemplate().query(sql, new Object[] { primaryKey }, beanExtrator);
        } finally {
            timer.end();
            getLogger().debug("Table {}.get, time = {} ns", tableName, timer.time());
        }
    }

    @Override
    public List<T> getBySecondaryKey(Object secondaryKey) {
        Timer timer = Timer.start();
        String sql = sql_select_by_second_key;
        try {
            return getJdbcTemplate().query(sql, new Object[] { secondaryKey }, beanListExtractor);
        } finally {
            timer.end();
            getLogger().debug("Table {}.getBySecondaryKey, time = {} ns", tableName, timer.time());
        }
    }

    @Override
    public void asyncAdd(T t) {
        Runnable task = () -> add(t);
        addTask(task);
    }

    @Override
    public void asyncDelete(int primaryKey) {
        Runnable task = () -> delete(primaryKey);
        addTask(task);
    }

    @Override
    public void asyncUpdate(T t) {
        Runnable task = () -> update(t);
        addTask(task);
    }

    @Override
    public void asyncGet(int primaryKey, Callback cb) {
        Runnable task = () -> {
            try {
                T t = get(primaryKey);
                cb.onSuccess(t);
            } catch (Throwable e) {
                cb.onError(e);
            }
        };
        addTask(task);
    }

    @Override
    public void asyncGetBySecondaryKey(Object secondaryKey, Callback cb) {
        Runnable task = () -> {
            try {
                List<T> t = getBySecondaryKey(secondaryKey);
                cb.onSuccess(t);
            } catch (Throwable e) {
                cb.onError(e);
            }
        };
        addTask(task);
    }

    // 提交任务
    protected void addTask(Runnable task) {
        dbService.addTask(task);
    }

    protected abstract Logger getLogger();

    /**
     * 时间测试，用法:
     * 
     * <pre>
     * Timer timer = Timer.start();
     * your code here;
     * timer.end();
     * System.out.println("time = " + timer.time());
     * </pre>
     * 
     * @author lhl
     *
     *         2016年5月10日 下午3:47:31
     */
    static class Timer
    {
        long startTime;
        long endTime;

        private Timer() {
        }

        static Timer start() {
            Timer timer = new Timer();
            timer.startTime = System.nanoTime();
            return timer;
        }

        void end() {
            endTime = System.nanoTime();
        }

        long time() {
            return endTime - startTime;
        }
    }
}
