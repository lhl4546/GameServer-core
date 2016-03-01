/**
 * 
 */
package core.fire.database;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 主键注解，用于表明被注解字段是数据表的主键
 * 
 * @author lhl
 *
 *         2016年2月24日 上午10:51:11
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface PrimaryKey {
}
