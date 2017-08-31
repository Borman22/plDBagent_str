package tv.sonce.pldbagent.model.tables;

public class Row_file_names {
    private int id_pk;
    private String file_name;
    private long date_create;
    private long date_delete;
    private int path_id_fk;

    public Row_file_names(int id_pk, String file_name, long date_create, long date_delete, int path_id_fk) {
        this.id_pk = id_pk;
        this.file_name = file_name;
        this.date_create = date_create;
        this.date_delete = date_delete;
        this.path_id_fk = path_id_fk;
    }

    public int getId_pk() {
        return id_pk;
    }

    public String getFile_name() {
        return file_name;
    }

    public long getDate_create() {
        return date_create;
    }

    public long getDate_delete() {
        return date_delete;
    }

    public int getPath_id_fk() {
        return path_id_fk;
    }

    @Override
    public String toString() {
        return "Row_file_names{" +
                "id_pk=" + id_pk +
                ", file_name='" + file_name + '\'' +
                ", date_create=" + date_create +
                ", date_delete=" + date_delete +
                ", path_id_fk=" + path_id_fk +
                '}';
    }
}
