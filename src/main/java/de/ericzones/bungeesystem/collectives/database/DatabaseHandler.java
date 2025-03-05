// Created by Eric B. 31.01.2021 01:48
package de.ericzones.bungeesystem.collectives.database;

public class DatabaseHandler {

    private ISqlAdapter sqlAdapter;
    private SqlAPI sqlAPI;

    public DatabaseHandler(String database) {
        this.sqlAPI = new SqlAPI("localhost", 3306, database, "username", "password");
        sqlAPI.connect();
        this.sqlAdapter = new SqlAdapter(sqlAPI);
    }

    public void disconnectDatabase() {
        this.sqlAPI.disconnect();
    }

    public ISqlAdapter getSqlAdapter() {
        return sqlAdapter;
    }
}
