package tv.sonce.pldbagent.model;
/**
 * Этот класс инкапсулирует каталог с файлами и все функции, которые можно с ними производить.
 * Получает список всех файлов, которые лежат в заданной папке
 */

import java.io.File;

public class FileAgent {
    private final String pathToDir;
    private final String pathToDirForQuery;
    private File[] folderEntries = null;

    public FileAgent(String pathToDir){
        this.pathToDir = pathToDir;
        this.pathToDirForQuery = "\'" + pathToDir.replace("\\", "\\\\")+ "\'";
        folderEntries = new File(pathToDir).listFiles();
    }

    public File [] getFolderEntries(){
        return folderEntries;
    }

    public String getPathToDir(){
        return pathToDir;
    }

    public String getPathToDirForQuery(){
        return pathToDirForQuery;
    }

}
