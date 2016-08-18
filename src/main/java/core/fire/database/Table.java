/**
 * 
 */
package core.fire.database;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 表名注解，放置于实体类上标注该实体类对应的数据表名
 * 
 * @author lhl
 *
 * 2016年2月24日 上午10:09:50
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Table {
    /**
     * @return 表名
     */
    String value();
}
