package tv.sonce.pldbagent.model;
/**
 * Этот класс инкапсулирует в себе БД и операции для получения к ней доступа
 */

import org.apache.log4j.Logger;

import java.sql.*;

public class DBConnector {

    private static final Logger LOGGER = Logger.getLogger(DBConnector.class);

    private Connection connection = null;
    private Statement statement = null;
    private String settings = "?serverTimezone=UTC&useSSL=false";

    public DBConnector(String hostPort, String dbName, String login, String password) throws SQLException {
        if (connection != null && !connection.isClosed()) {
            throw new SQLException("Соединение с базой данных уже установлено." +
                    "Чтобы установить новое соединение, сначала необходимо закрыть текущее");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
//            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("ОШИБКА! Не подключен SQL драйвер JDBC!", e);
        }

        connection = DriverManager.getConnection(hostPort + dbName + settings, login, password);
        if ((connection == null) || (connection.isClosed()))
            throw new SQLException("Не удалось подключитсья к базе данных");
        statement = connection.createStatement();
    }

    private boolean isConnected() {
        try {
            if (connection != null && !connection.isClosed())
                return true;
        } catch (SQLException e) {
//            DoNothing
        }
        return false;
    }

    public ResultSet executeQuery(String query) throws SQLException { // пока сделаем так. потом переделаем по человечески
        return statement.executeQuery(query);
    }

    public void executeUpdate(String query) throws SQLException { // пока сделаем так. потом переделаем по человечески
        statement.executeUpdate(query);
    }


    public void close() {
        if (isConnected())
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.error("Не удалось закрыть соединение с БД", e);
            }
    }

    protected void finalize() {
        if (this.isConnected())
            close();
    }
}
