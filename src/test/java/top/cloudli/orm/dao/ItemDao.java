package top.cloudli.orm.dao;

import top.cloudli.orm.annotation.Delete;
import top.cloudli.orm.annotation.Insert;
import top.cloudli.orm.annotation.Select;
import top.cloudli.orm.annotation.Update;

import java.util.HashMap;
import java.util.List;

public interface ItemDao {

    @Select(sql = "select * from item")
    List<HashMap<String, Object>> getAll();

    @Select(sql = "select * from item where id = ?")
    HashMap<String, Object> getById(long id);

    @Insert(sql = "insert into item(name) values(?)", useGeneratedKey = true)
    long add(String name);

    @Update(sql = "update item set name = ? where id = ?")
    long update(String name, long id);

    @Delete(sql = "delete from item where id = ?")
    long removeById(long id);
}
