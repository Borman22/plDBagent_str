package tv.sonce.pldbagent.controller;
/**
 * Этому классу позволяется апдейтить БД. Он берет распарсеный файл, проверяет, каждое событие и если в БД такого события нет -
 * добавляет это событие в БД, так же занимается выборкой из БД
 */

import org.apache.log4j.Logger;
import sun.rmi.runtime.Log;
import tv.sonce.pldbagent.model.DBConnector;
import tv.sonce.pldbagent.model.FileAgent;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBAgentUtil {

    private static final Logger LOGGER = Logger.getLogger(DBAgentUtil.class);
    private static Map<String, Integer> formats = null;

    static void addAllEventsFromNewFile(List<FileParser.Event> parseredFile, DBConnector dbConnector, int id_file_names) {
        if (formats == null) // получаем все форматы из БД - пригодятся
            refreshFormats(dbConnector);

        // Каждое событие из файла надо сравнить с БД и если этого события нет в БД, то добавить ее в БД

        for (FileParser.Event currentEvent : parseredFile) {
            // Сначала получаем id всех форматов
            int[] formats_id = new int[currentEvent.format.length];
            int k = 0;
            for (String currentFormat : currentEvent.format)
                formats_id[k++] = getFormatIdFromDB(currentFormat, dbConnector);

            // Потом получаем id из таблицы asset_id_name
            int[] result = getAssetIdNameId(currentEvent.asset_id, currentEvent.eventName, dbConnector);
            int asset_id_name_id = result[0];
            int isOldAsset_id = result[1]; // 1 - уже был такой asset_id_name_id, 0 - добавили только что

            int currentEventID = -1;
            if (isOldAsset_id == 1) {
                // Запрашиваем у БД инфу про конкретное событие date, time, asset_id, tcin, tcout, duration
                currentEventID = getEventID(dbConnector, currentEvent, asset_id_name_id);
            }

            if (currentEventID < 0)
                addOneEventFromNewFile(currentEvent, dbConnector, formats_id, asset_id_name_id, id_file_names);
            else
                continue;

            currentEventID = getEventID(dbConnector, currentEvent, asset_id_name_id);
            if (currentEventID < 0) continue;

            // добавляем все форматы
            for (int formatID : formats_id) {
                String query = String.format("INSERT INTO formats_in_event (event_id, format_id) VALUES (\'%s\', \'%s\')", currentEventID, formatID);
                try {
                    dbConnector.executeUpdate(query);
                } catch (SQLException e) {
                    LOGGER.error("Не удалось добавить запись в таблицу formats_in_event", e);
                }
            }
        }
    }

    private static int getEventID(DBConnector dbConnector, FileParser.Event currentEvent, int asset_id_name_id) {
        String query = String.format("SELECT * FROM events WHERE " +
                        "date = \'%s\' AND time_in_frame = \'%s\' AND asset_id_name_id = \'%s\'",
                currentEvent.date, currentEvent.time, asset_id_name_id);

        ResultSet rs = null;
        try {
            rs = dbConnector.executeQuery(query);
        } catch (SQLException e) {
            LOGGER.error("Не удалось выполнить запрос к БД  " + query, e);
        }
        try {
            if (rs != null && rs.next())
                return rs.getInt("id");
        } catch (SQLException e) {
            LOGGER.error("Не удалось сделать ResultSet.next()", e);
        }
        return -1;
    }

    private static int[] getAssetIdNameId(int asset_id, String eventName, DBConnector dbConnector) {
        String query = String.format("SELECT * FROM asset_id_name WHERE asset_id = \'%s\' AND name = \'%s\'", asset_id, eventName);
        ResultSet rs;
        try {
            rs = dbConnector.executeQuery(query);
        } catch (SQLException e) {
            LOGGER.error("Не удалось с БД получить asset_id, name", e);
            return new int[]{-1, 1};
        }

        if (rs == null)
            return new int[]{-1, 1};
        try {
            if (rs.next()) {
                return new int[]{rs.getInt("id"), 1};
            }
        } catch (SQLException e) {
            LOGGER.error("Не удалось выполнить rs.next(), когда хотели получить asset_id для файла " + eventName + " с asset_id = " + asset_id, e);
            return new int[]{-1, 1};
        }

        String queryInsert = String.format("INSERT INTO asset_id_name (asset_id, name) VALUES (\'%s\', \'%s\')", asset_id, eventName);
        try {
            dbConnector.executeUpdate(queryInsert);
        } catch (SQLException e) {
            LOGGER.error("Не удалось добавить в БД asset_id and name", e);
            return new int[]{-1, 0};
        }
        try {
            rs = dbConnector.executeQuery(query);
        } catch (SQLException e) {
            LOGGER.error("Не удалось добавить в БД asset_id and name", e);
            return new int[]{-1, 0};
        }

        if (rs == null)
            return new int[]{-1, 0};
        try {
            if (rs.next()) {
                return new int[]{rs.getInt("id"), 0};
            }
        } catch (SQLException e) {
            LOGGER.error("Не удалось выполнить rs.next(), когда хотели получить asset_id для файла " + eventName + " с asset_id = " + asset_id, e);
        }
        return new int[]{-1, 0};
    }

    private static void addOneEventFromNewFile(FileParser.Event event, DBConnector dbConnector, int[] formats_id, int asset_id_name_id, int file_names_id) {
        // Добавляем сначала в event, потом получаем id вновь созданного, потом добавляем в formats_in_event
        String query = String.format("INSERT INTO events (date, time_in_frame, tc_in_in_frame, tc_out_in_frame, asset_id_name_id, file_names_id, duration)" +
                " VALUES (\'%s\', \'%s\', \'%s\', \'%s\', \'%s\', \'%s\', \'%s\')", event.date, event.time, event.tcIn, event.tcOut, asset_id_name_id, file_names_id, event.duration);
        try {
            dbConnector.executeUpdate(query);
        } catch (SQLException e) {
            LOGGER.error("Не удалось добавить новый event в БД (DBAgentUtil.addOneEventFromNewFile) " + query, e);
        }
    }

    // Этот метод добавляет файл (имя, дату создания, путь к директории) в БД, чтобы при следующем запуске его не парсить
    // Если файл уже добавлен, то просто возвращает его id
    static int addNewFile(File originFile, FileAgent fileAgent, DBConnector dbConnector, int id_path) {

        // получим id под которым хранится файл в БД. Если файла нет, то сначала добавим, а потом получим id
        int fileID = getFileID(originFile, dbConnector, id_path);
        if (fileID >= 0)
            return fileID;

        String query = String.format("INSERT INTO file_names (file_name, date_create, date_delete, id_path) VALUES (\'%s\', \'%s\', \'0\', \'%s\')", originFile.getName(), originFile.lastModified(), id_path);  // "INSERT INTO users (name, password) VALUES ('borman', '1234wedfxc')"
        try {
            dbConnector.executeUpdate(query);
        } catch (SQLException e) {
            LOGGER.error("Не удалось в БД добавить файл " + fileAgent.getPathToDir() + originFile.getName() + " query = " + query, e);
        }

        return getFileID(originFile, dbConnector, id_path);
    }

    private static int getFileID(File originFile, DBConnector dbConnector, int id_path) {
        try {
            ResultSet rs = dbConnector.executeQuery(String.format("SELECT id FROM file_names WHERE file_name = \'%s\' AND date_create = \'%s\' AND id_path = \'%s\' AND date_delete = '0'", originFile.getName(), originFile.lastModified(), id_path));
            if (rs.next()) {
                return rs.getInt("id");
            }
        } catch (SQLException e) {
            LOGGER.error("Не удалось получить из БД id данного файла " + originFile.getName(), e);
        }
        return -1;
    }

    private static void refreshFormats(DBConnector dbConnector) {
        formats = new HashMap<>();
        ResultSet rs;
        try {
            rs = dbConnector.executeQuery("SELECT * FROM formats");
        } catch (SQLException e) {
            LOGGER.error("Не удалось получить список форматов из БД", e);
            return;
        }
        if (rs == null) return;

        try {
            while (rs.next()) {
                formats.put(rs.getString("format"), rs.getInt("id"));
            }
        } catch (SQLException e) {
            LOGGER.error("Не получилось выполнить rs.next(), когда хотели получить список всех форматов из БД", e);
        }
    }

    private static int getFormatIdFromDB(String newFormat, DBConnector dbConnector) {
        if (formats == null)
            refreshFormats(dbConnector);
        if (formats == null)
            return -1;

        Integer formatID = formats.get(newFormat);
        if (formatID != null)
            return formatID.intValue();

        String query = "INSERT INTO formats (format) VALUES (\'" + newFormat + "\')";
        try {
            dbConnector.executeUpdate(query);
        } catch (SQLException e) {
            LOGGER.error("Не удалось добавить новый формат " + newFormat + " в БД" + query, e);
        }
        refreshFormats(dbConnector); // обновляем список форматов

        formatID = formats.get(newFormat);
        if (formatID != null)
            return formatID.intValue();
        return -1;
    }

}
