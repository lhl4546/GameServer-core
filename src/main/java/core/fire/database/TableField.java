/**
 * 
 */
package core.fire.database;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 数据库表字段
 * 
 * @author lhl
 *
 *         2016年3月17日 下午1:47:53
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface TableField {
}
