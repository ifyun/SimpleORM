package top.cloudli.orm;

import lombok.extern.slf4j.Slf4j;
import top.cloudli.orm.annotation.Delete;
import top.cloudli.orm.annotation.Insert;
import top.cloudli.orm.annotation.Select;
import top.cloudli.orm.annotation.Update;

import javax.activation.UnsupportedDataTypeException;
import javax.sql.DataSource;
import java.lang.reflect.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class DaoFactory implements InvocationHandler {

    private DataSource dataSource;

    public DaoFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Create proxy Dao object
     *
     * @param _interface The interface for the proxy class to implement
     * @return The proxy Dao object
     */
    public Object createDao(Class<?> _interface) {
        return Proxy.newProxyInstance(_interface.getClassLoader(), new Class[]{_interface}, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isAnnotationPresent(Select.class)) {
            Type returnType = method.getGenericReturnType();

            if (returnType instanceof ParameterizedType
                    && ((ParameterizedType) returnType).getRawType() == List.class) {
                if (((ParameterizedType) returnType).getActualTypeArguments()[0].getTypeName().contains("HashMap")) {
                    return select(method.getAnnotation(Select.class).sql(), args);
                } else {
                    throw new UnsupportedDataTypeException("Unsupported return type.");
                }
            } else if (returnType instanceof ParameterizedType
                    && ((ParameterizedType) returnType).getRawType() == HashMap.class) {
                return select(method.getAnnotation(Select.class).sql(), args).get(0);
            }
        } else if (method.isAnnotationPresent(Insert.class)) {
            return insert(
                    method.getAnnotation(Insert.class).sql(),
                    args,
                    method.getAnnotation(Insert.class).useGeneratedKey()
            );
        } else if (method.isAnnotationPresent(Update.class)) {
            return update(method.getAnnotation(Update.class).sql(), args);
        } else if (method.isAnnotationPresent(Delete.class)) {
            return update(method.getAnnotation(Delete.class).sql(), args);
        }

        return null;
    }

    /**
     * Execute SELECT
     *
     * @param sql  The DML statements
     * @param args The args for DML statements
     * @return {@link List<HashMap>} The result of SELECT statements
     */
    private List<HashMap<String, Object>> select(String sql, Object[] args) {
        List<HashMap<String, Object>> list = new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            if (args != null)
                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }
            log.debug(statement.toString());
            ResultSet resultSet = statement.executeQuery();
            ResultSetMetaData metaData = resultSet.getMetaData();

            while (resultSet.next()) {
                HashMap<String, Object> map = new HashMap<>();
                for (int i = 0; i < metaData.getColumnCount(); i++) {
                    map.put(metaData.getColumnName(i + 1), resultSet.getObject(i + 1));
                }
                list.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * Execute UPDATE or DELETE
     *
     * @param sql  The DML statements
     * @param args The args for DML statements
     * @return The row count for DML statements
     */
    private long update(String sql, Object[] args) {
        long rows = 0;

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            if (args != null)
                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }
            log.debug(statement.toString());
            rows = statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return rows;
    }

    /**
     * Execute INSERT
     *
     * @param sql             The DML statements
     * @param args            The args for DML statements
     * @param useGeneratedKey Return the generated key or not
     * @return (1) The row count for DML statements or
     * (2) the generated key if <code>useGeneratedKey</code> is true
     */
    private Long insert(String sql, Object[] args, boolean useGeneratedKey) {
        Long result = null;

        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement statement;

            if (useGeneratedKey) {
                statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                statement = connection.prepareStatement(sql);
            }

            if (args != null)
                for (int i = 0; i < args.length; i++) {
                    statement.setObject(i + 1, args[i]);
                }
            log.debug(statement.toString());
            result = (long) statement.executeUpdate();

            if (useGeneratedKey) {
                ResultSet rs = statement.getGeneratedKeys();
                if (rs.next())
                    result = rs.getLong(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
