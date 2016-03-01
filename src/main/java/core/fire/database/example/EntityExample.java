/**
 * 
 */
package core.fire.database.example;

import core.fire.database.PrimaryKey;
import core.fire.database.SecondKey;
import core.fire.database.Table;

/**
 * @author lhl
 *
 *         2016年2月25日 上午11:10:45
 */
@Table("TABLE_EXAMPLE")
public class EntityExample
{
    @PrimaryKey
    private int id;
    @SecondKey
    private String name;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
