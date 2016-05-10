/**
 * 
 */
package core.fire.database;

import java.util.List;

/**
 * @author lhl
 *
 *         2016年5月9日 下午2:38:15
 */
public interface DataAccess<T>
{
    void add(T t);

    void delete(T t);

    void update(T t);

    T get(int primaryKey);

    List<T> getBySecondaryKey(int secondaryKey);
}
