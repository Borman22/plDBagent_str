package tv.sonce.pldbagent.controller;

import org.apache.log4j.Logger;
import tv.sonce.pldbagent.model.DBConnector;
import tv.sonce.pldbagent.model.FileAgent;
import tv.sonce.pldbagent.model.tables.Row_file_names;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Этот класс занимается поиском удаленных и новых файлов
 */

class FileFinder {

    private static final Logger LOGGER = Logger.getLogger(FileFinder.class);

    private File[] existingFilesInDir;
    private Map<String, Long> existingFilesInDirMap;
    private DBConnector dbConnector;
    private String pathToDir;
    private String pathToDirForQuery;

    FileFinder(FileAgent fileAgent, DBConnector dbConnector) {
        this.pathToDir = fileAgent.getPathToDir();
        this.pathToDirForQuery = "\'" + pathToDir.replace("\\", "\\\\") + "\'";
        this.existingFilesInDir = fileAgent.getFolderEntries();
        this.dbConnector = dbConnector;

        if (existingFilesInDir != null) {
            existingFilesInDirMap = new HashMap<>();
            for (File tempFile : existingFilesInDir)
                existingFilesInDirMap.put(tempFile.getName(), tempFile.lastModified());
        }
    }


    List<Row_file_names> getDeletedFilesInDir() {
        // Получаем список существующих файлов в БД и смотрим, есть ли файлы,
        // которые по имени и дате создания не соответствуют ни одному файлу из текущей дериктории
        if (existingFilesInDir == null)
            return null;   // Если нет доступа к директории - не значит, что файлы из нее удалены

        List<Row_file_names> existingFilesInDB = getExistingFilesInDirInDB(dbConnector);
        List<Row_file_names> deletedFiles = new ArrayList<>();


        for (Row_file_names currentFileInDB : existingFilesInDB) {
            if (existingFilesInDirMap.get(currentFileInDB.getFile_name()) == null || currentFileInDB.getDate_create() != existingFilesInDirMap.get(currentFileInDB.getFile_name()))
                deletedFiles.add(currentFileInDB);
        }
        return deletedFiles;
    }

    List<File> getNewFilesInDir() {
        // Получаем список существующих файлов в директории и смотрим, есть ли файлы, которые по имени
        // и дате создания не соответствуют ни одному файлу из базы данных из той же дериктории

        if (existingFilesInDir == null)
            return null;   // нет доступа к директории - выходим

        List<Row_file_names> existingFilesInDB = getExistingFilesInDirInDB(dbConnector);

        Map<String, Row_file_names> existingFilesInDBMap = new HashMap<>();
        for (Row_file_names tempFile : existingFilesInDB)
            existingFilesInDBMap.put(tempFile.getFile_name(), tempFile);

        List<File> newFiles = new ArrayList<>();

        for (File tempFileInDir : existingFilesInDir) {
            if (existingFilesInDBMap.get(tempFileInDir.getName()) == null || tempFileInDir.lastModified() != existingFilesInDBMap.get(tempFileInDir.getName()).getDate_create()) {
                newFiles.add(tempFileInDir);
            }
        }

        return newFiles;

    }

    // получим из БД список всех когда либо существовавших файлов в заданой папке
//    private List<Row_file_names> getAllFilesInDirInDB(DBConnector dbConnector){
//        String query = "SELECT * FROM file_names, path WHERE file_names.id_path = path.id AND path.path = " + pathToDirForQuery;
//        return getTable_file_names(dbConnector, query);
//    }

    // получим из БД список всех удаленных файлов из заданой папки
//    private List<Row_file_names> getDeletedFilesInDirInDB(DBConnector dbConnector){
//        String query = "SELECT * FROM file_names, path WHERE file_names.date_delete != 0 AND file_names.id_path = path.id AND path.path = " + pathToDirForQuery;
//        return getTable_file_names(dbConnector, query);
//    }

    // получим из БД список всех существующих файлов в заданой папке
    private List<Row_file_names> getExistingFilesInDirInDB(DBConnector dbConnector) {
        String query = "SELECT * FROM file_names, path WHERE file_names.date_delete = 0 AND file_names.id_path = path.id AND path.path = " + pathToDirForQuery;
        return getTable_file_names(dbConnector, query);
    }

    private List<Row_file_names> getTable_file_names(DBConnector dbConnector, String query) {
        ResultSet rs = null;
        try {
            rs = dbConnector.executeQuery(query);
        } catch (SQLException e) {
            LOGGER.error("Не удалось получить данные из БД", e);
        }
        if (rs == null) return null;

        List<Row_file_names> table_file_names = new ArrayList<>();
        try {
            int tempIdPk;
            String tempFileName;
            long tempDateCreate;
            long tempDateDeleted;
            int tempPathIdFk;

            while (rs.next()) {
                tempIdPk = rs.getInt("id");
                tempFileName = rs.getString("file_name");
                tempDateCreate = rs.getLong("date_create");
                tempDateDeleted = rs.getLong("date_delete");
                tempPathIdFk = rs.getInt("id_path");

                table_file_names.add(new Row_file_names(tempIdPk, tempFileName, tempDateCreate, tempDateDeleted, tempPathIdFk));
            }
            return table_file_names;
        } catch (SQLException e) {
            LOGGER.error("Не получилось прочитать данные из ResultSet", e);
        }

        return null;
    }

}
