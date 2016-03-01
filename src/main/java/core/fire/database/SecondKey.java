/**
 * 
 */
package core.fire.database;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 索引注解，用于表明被注解字段是数据表的非主键索引
 * <p>
 * 一个实体类只能出现一次SecondKey
 * 
 * @author lhl
 *
 *         2016年2月24日 上午10:51:11
 */
@Target(FIELD)
@Retention(RUNTIME)
public @interface SecondKey {
}
