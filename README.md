## Simple ORM

This is a super simple ORM using Java Reflect and Dynamic Proxy.

### Use

#### Crete Factory

Set a `DataSource`  for `DaoFactory` .

```java
// Create a DataSource (DBCP, HikariCP and so on).
DataSource dataSource = ...;
DaoFactory factory = new DaoFactory(dataSource);
```

#### Create Dao Interface

Use `@Select` , `@Insert` ,`@Update` and `@Delete` to write DML statements.

```java
public interface ItemDao {

    @Select(sql = "select * from item")
    List<HashMap<String, Object>> getAll();

    @Select(sql = "select * from item where id = ?")
    HashMap<String, Object> getById(long id);

    @Insert(sql = "insert into item(name) values(?)", useGeneratedKey = true)
    long add(String name);

    @Delete(sql = "delete from item where id = ?")
    long removeById(long id);
}
```

> The `@Select` annotation only support `HashMap` as return type currently.  
> set `useGeneratedKey` to `true` could return auto-generated primary key.

#### Create Proxy Object

Use proxy object to manipulate data.

```java
ItemDao itemDao = (ItemDao) factory.create(ItemDao.class);
// Select all
List<HashMap<String, Object>> items = itemDao.getAll();
// Select by id
HashMap<String, Object> item = itemDao.getById(id);
// Insert
long id = itemDao.add("NewItem");
```
