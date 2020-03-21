package top.cloudli.orm;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import top.cloudli.orm.dao.ItemDao;

import java.util.HashMap;
import java.util.List;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SimpleORMTest {

    private static ItemDao itemDao;
    private static long id;

    @BeforeAll
    static void setUp() {
        // create table foo.item
        // (
        //     id   int auto_increment
        //         primary key,
        //     name varchar(32) null
        // );
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/foo");
        config.setUsername("root");
        config.setPassword("root");
        config.setMaximumPoolSize(5);
        HikariDataSource dataSource = new HikariDataSource(config);

        DaoFactory factory = new DaoFactory(dataSource);
        itemDao = (ItemDao) factory.createDao(ItemDao.class);
    }

    @Test
    @Order(1)
    void add() {
        id = itemDao.add("NewItem");
        log.info("Insert, id = {}", id);
    }

    @Test
    @Order(2)
    void getAll() {
        List<HashMap<String, Object>> items = itemDao.getAll();
        log.info("All Items: {}", items);
    }

    @Test
    @Order(3)
    void update() {
        log.info("Update {} rows.", itemDao.update("UpdatedItem", id));
    }

    @Test
    @Order(3)
    void getById() {
        HashMap<String, Object> item = itemDao.getById(id);
        log.info("Item of id = {}: {}", id, item);
    }

    @Test
    @Order(4)
    void delete() {
        log.info("Remove {} rows.", itemDao.removeById(id));
    }
}