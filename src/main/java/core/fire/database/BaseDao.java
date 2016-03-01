/**
 * 
 */
package core.fire.database;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

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
public class BaseDao<T>
{
    private static final String SQL_INSERT_UPDATE = "INSERT INTO $table ($keys) VALUES ($values) "
            + "ON DUPLICATE KEY UPDATE $assign";
    private static final String SQL_DELETE = "DELETE FROM $table WHERE $primarykey=?";
    private static final String SQL_SELECT_BY_PRIMARY_KEY = "SELECT * FROM $table WHERE $primarykey=?";
    private static final String SQL_SELECT_BY_SECOND_KEY = "SELECT * FROM $table WHERE $secondkey=?";
    private static final BeanProcessor beanProcessor = new BeanProcessor();

    private String sql_insert_update;
    private String sql_delete;
    private String sql_select_by_primary_key;
    private String sql_select_by_second_key;
    private PropertyDescriptor[] props;
    private Field primaryKey;
    private Class<?> type;

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DBService dbService;

    /**
     * @param type 实体类类型
     */
    protected BaseDao(Class<?> type) {
        this.type = Objects.requireNonNull(type);
    }

    @PostConstruct
    public void initialize() {
        getInsertUpdateSQL();
        getDeleteSQL();
        getSelectByPrimaryKeySQL();
        getSelectBySecondKeySQL();
    }

    // SQL generator ------------------------
    // 获取插入\更改操作SQL
    protected String getInsertUpdateSQL() {
        if (sql_insert_update == null) {
            synchronized (this) {
                if (sql_insert_update == null) {
                    sql_insert_update = getInsertUpdateSQL0(type);
                }
            }
        }

        return sql_insert_update;
    }

    // 生成插入\更改操作SQL
    private String getInsertUpdateSQL0(Class<?> type) {
        PropertyDescriptor[] props = propertyDescriptors(type);
        String table = getTable(type);
        String keys = getKeys(props);
        String values = getValues(props);
        String assign = getAssign(props);
        String sql = SQL_INSERT_UPDATE.replace("$table", table);
        sql = sql.replace("$keys", keys);
        sql = sql.replace("$values", values);
        sql = sql.replace("$assign", assign);
        return sql;
    }

    // 获取插入KEY
    private String getKeys(PropertyDescriptor[] props) {
        StringBuilder sb = new StringBuilder();
        for (PropertyDescriptor prop : props) {
            sb.append(prop.getName()).append(",");
        }
        return sb.substring(0, sb.length() - 1);
    }

    // 获取插入参数格式(?,?...)
    private String getValues(PropertyDescriptor[] props) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < props.length; i++) {
            sb.append("?,");
        }
        return sb.substring(0, sb.length() - 1);
    }

    // 获取SQL赋值格式(key1=?,key2=?...)
    private String getAssign(PropertyDescriptor[] props) {
        StringBuilder sb = new StringBuilder();
        for (PropertyDescriptor prop : props) {
            sb.append(prop.getName()).append("=?,");
        }
        return sb.substring(0, sb.length() - 1);
    }

    // 获取删除操作SQL
    protected String getDeleteSQL() {
        if (sql_delete == null) {
            synchronized (this) {
                if (sql_delete == null) {
                    sql_delete = getDeleteSQL0(type);
                }
            }
        }

        return sql_delete;
    }

    // 生成删除操作SQL
    private String getDeleteSQL0(Class<?> type) {
        String table = getTable(type);
        String primaryKey = getPrimaryKey(type);
        String sql = SQL_DELETE.replace("$table", table);
        sql = sql.replace("$primarykey", primaryKey);
        return sql;
    }

    // 获取主键查询操作SQL
    protected String getSelectByPrimaryKeySQL() {
        if (sql_select_by_primary_key == null) {
            synchronized (this) {
                if (sql_select_by_primary_key == null) {
                    sql_select_by_primary_key = getSelectByPrimaryKeySQL0(type);
                }
            }
        }

        return sql_select_by_primary_key;
    }

    // 生成主键查询操作SQL
    private String getSelectByPrimaryKeySQL0(Class<?> type) {
        String table = getTable(type);
        String primaryKey = getPrimaryKey(type);
        String sql = SQL_SELECT_BY_PRIMARY_KEY.replace("$table", table);
        sql = sql.replace("$primarykey", primaryKey);
        return sql;
    }

    // 获取其他关键字查询操作SQL
    protected String getSelectBySecondKeySQL() {
        if (sql_select_by_second_key == null) {
            synchronized (this) {
                if (sql_select_by_second_key == null) {
                    sql_select_by_second_key = getSelectBySecondKeySQL0(type);
                }
            }
        }

        return sql_select_by_second_key;
    }

    // 生成其他关键字查询操作SQL
    private String getSelectBySecondKeySQL0(Class<?> type) {
        String table = getTable(type);
        String secondKey = getSecondKey(type);
        String sql = SQL_SELECT_BY_SECOND_KEY.replace("$table", table);
        sql = sql.replace("$secondkey", secondKey);
        return sql;
    }

    // 获取插入\更改操作参数
    protected Object[] getInsertUpdateParam(Object obj) {
        PropertyDescriptor[] props = this.props;
        Object[] param = getInsertParam(obj, props);
        Object[] result = new Object[param.length * 2];
        System.arraycopy(param, 0, result, 0, param.length);
        System.arraycopy(param, 0, result, param.length, param.length);
        return result;
    }

    private Object[] getInsertParam(Object obj, PropertyDescriptor[] props) {
        Object[] vals = new Object[props.length];
        for (int i = 0; i < vals.length; i++) {
            PropertyDescriptor prop = props[i];
            try {
                vals[i] = prop.getReadMethod().invoke(obj);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return vals;
    }

    // 获取删除操作参数
    protected Object getDeleteParam(Object obj) {
        Field primaryKey = this.primaryKey;
        try {
            return primaryKey.get(obj);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // 获取实体类对应的数据表名，由Table注解获得
    private String getTable(Class<?> type) {
        Table tableAnnotation = type.getAnnotation(Table.class);
        return tableAnnotation.value();
    }

    // 获取实体类主键，由PrimaryKey注解获得
    private String getPrimaryKey(Class<?> type) {
        String primaryKey = null;
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            PrimaryKey keyAnnotation = field.getAnnotation(PrimaryKey.class);
            if (keyAnnotation != null) {
                primaryKey = field.getName();
                field.setAccessible(true);
                this.primaryKey = field;
            }
        }
        return primaryKey;
    }

    // 获取实体类第二主键(索引)，由SecondKey获得
    private String getSecondKey(Class<?> type) {
        String secondKey = null;
        Field[] fields = type.getDeclaredFields();
        for (Field field : fields) {
            SecondKey keyAnnotation = field.getAnnotation(SecondKey.class);
            if (keyAnnotation != null) {
                secondKey = field.getName();
            }
        }
        return secondKey;
    }

    // 提取实体类属性
    private PropertyDescriptor[] propertyDescriptors(Class<?> c) {
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(c);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }

        PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
        PropertyDescriptor[] newProps = filterClass(props);
        this.props = newProps;
        return newProps;
    }

    // 过滤class属性，这不是我们需要的
    private PropertyDescriptor[] filterClass(PropertyDescriptor[] source) {
        List<PropertyDescriptor> list = new ArrayList<>(source.length - 1);
        for (int i = 0; i < source.length; i++) {
            if (!"class".equals(source[i].getName())) {
                list.add(source[i]);
            }
        }
        return list.toArray(new PropertyDescriptor[list.size()]);
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

    // 有则更新，无则插入
    protected void insertOrUpdate(T t) {
        String sql = getInsertUpdateSQL();
        Object[] param = getInsertUpdateParam(t);
        getJdbcTemplate().update(sql, param);
    }

    // 删除
    protected void delete(T t) {
        String sql = getDeleteSQL();
        Object param = getDeleteParam(t);
        getJdbcTemplate().update(sql, param);
    }

    // 根据主键查询，需要实体类有标注了@PrimaryKey注解的字段
    protected T selectByPrimarykey(Object primaryKey) {
        String sql = getSelectByPrimaryKeySQL();
        return getJdbcTemplate().query(sql, new Object[] { primaryKey }, beanExtrator);
    }

    // 根据索引查询，需要实体类有标注了@SecondKey注解的字段
    protected List<T> selectBySecondKey(Object secondKey) {
        String sql = getSelectBySecondKeySQL();
        return getJdbcTemplate().query(sql, new Object[] { secondKey }, beanListExtractor);
    }

    // JDBC asynchronous operation -----------------
    // 提交异步任务
    protected void addTask(Runnable task) {
        dbService.addTask(task);
    }

    // 异步插入|更新
    protected void asyncInsertOrUpdate(T t) {
        Runnable task = () -> insertOrUpdate(t);
        addTask(task);
    }

    // 异步删除
    protected void asyncDelete(T t) {
        Runnable task = () -> delete(t);
        addTask(task);
    }

    // 异步查询(根据主键)，需要提供一个回调接口以执行后续操作
    protected void asyncSelectByPrimaryKey(Object primaryKey, Callback cb) {
        Callback newCb = Objects.requireNonNull(cb);
        Runnable task = () -> {
            try {
                T t = selectByPrimarykey(primaryKey);
                newCb.onSuccess(t);
            } catch (Exception e) {
                newCb.onError(e);
            }
        };
        addTask(task);
    }

    // 异步查询(根据索引)，需要提供一个回调接口以执行后续操作
    protected void asyncSelectBySecondKey(Object secondKey, Callback cb) {
        Callback newCb = Objects.requireNonNull(cb);
        Runnable task = () -> {
            try {
                List<T> list = selectBySecondKey(secondKey);
                newCb.onSuccess(list);
            } catch (Exception e) {
                newCb.onError(e);
            }
        };
        addTask(task);
    }
}
