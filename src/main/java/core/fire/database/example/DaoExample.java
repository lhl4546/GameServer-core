/**
 * 
 */
package core.fire.database.example;

import java.util.List;

import org.springframework.stereotype.Repository;

import core.fire.database.BaseDao;

/**
 * @author lhl
 *
 *         2016年2月25日 上午9:58:26
 */
@Repository
public class DaoExample extends BaseDao<EntityExample>
{
    public DaoExample() {
        super(EntityExample.class);
    }

    public EntityExample findById(int id) {
        return selectByPrimarykey(Integer.valueOf(id));
    }

    public List<EntityExample> findByName(String name) {
        return selectBySecondKey(name);
    }

    public void save(EntityExample obj) {
        insertOrUpdate(obj);
    }

    public void delete(EntityExample obj) {
        delete(obj);
    }
}
