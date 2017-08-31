package tv.sonce.pldbagent;
/**
 * Этот класс запускает программу и инициализирует все классы.
 */

import org.apache.log4j.Logger;
import tv.sonce.pldbagent.controller.MainController;
import tv.sonce.pldbagent.model.DBConnector;
import tv.sonce.pldbagent.model.FileAgent;

import java.sql.SQLException;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class);

    private static String pathToStorage = "\\\\storage\\Solarmedia\\pl_lists\\"; // пути к дерикториям, где лежат плейлисты
    private static String pathToD = "d:\\Borman\\pl_lists\\";
    private static String pathToInmedia = "\\\\inmedia\\AsRunLogs\\";

    private static String dbName = "playlistdb_str";
    private static String dbHost = "jdbc:mysql://localhost:3306/";
    private static String dbLogin = "root";
    private static String dbPassword = "root";
    public static long currentDate;

    public static void main(String[] args) {
        currentDate = System.currentTimeMillis();

        FileAgent fileAgentStorage = new FileAgent(pathToStorage);
        FileAgent fileAgentInmedia = new FileAgent(pathToInmedia);
        FileAgent fileAgentD = new FileAgent(pathToD);

        try {
            LOGGER.info(String.format("Создаем экземпляр DBConnector с параметрами: dbHost = %s, dbName = %s, dbLogin = %s, dbPassword = %s", dbHost, dbName, dbLogin, dbPassword));
            DBConnector dbConnector = new DBConnector(dbHost, dbName, dbLogin, dbPassword);
            LOGGER.debug("Экземпляр DBConnector создался. dbConnector = " + dbConnector);

            LOGGER.info(String.format("Создаем экземпляр MainController с параметрами dbConnector = %s, fileAgentStorage = %s, fileAgentInmedia = %s, fileAgentD = %s", dbConnector, fileAgentStorage, fileAgentInmedia, fileAgentD));
            MainController mainController = new MainController(dbConnector, fileAgentD, fileAgentStorage, fileAgentInmedia);
        } catch (SQLException e) {
            LOGGER.error("Не удалось установить соединение с БД. ", e);
            if (e.getCause() != null)
                LOGGER.error("Причина: " + e.getCause().getLocalizedMessage());
        }
        LOGGER.trace("Завершается работа программы.");
    }
}
