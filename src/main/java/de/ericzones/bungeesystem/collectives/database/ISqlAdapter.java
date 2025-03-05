// Created by Eric B. 30.01.2021 19:16
package de.ericzones.bungeesystem.collectives.database;

import de.ericzones.bungeesystem.collectives.object.Pair;

import java.sql.ResultSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ISqlAdapter {

    void createTable(String tableName, Pair<String, SqlDataType>[] content, String primaryKey);
    void updateInTable(String tableName, String column, Object value, String setColumn, Object setValue);
    void updateAllInTable(String tableName, String column, Object setValue);
    void addToTable(String tableName, List<String> columns, List<Object> values);

    void removeFromTable(String tableName, String column, Object value, String column2, Object value2);
    void removeFromTable(String tableName, String column, Object value);
    void removeAllFromTable(String tableName);

    boolean existsInTable(String tableName, String column, Object value);
    boolean existsInTable(String tableName, String column, Object value, String column2, Object value2);

    ResultSet getResultsFromTable(String tableName, String column, Object value);
    ResultSet getResultsFromTable(String tableName, String column, String column2, Object value);
    ResultSet getResultsFromTable(String tableName);

    Object getObjectFromTable(String tableName, String column, Object value, String neededColumn);
    List<Object> getObjectListFromTable(String tableName, String column, Object value, String[] columnNames);

    Map<String, List<Object>> getAllObjectsFromTablePrimaryKey(String tableName, String[] columnNames);
    List<List<Object>> getAllObjectsFromTable(String tableName, String[] columnNames);
    List<List<Object>> getAllObjectsFromTable(String tableName, String column, Object value, String[] columnNames);
    List<List<Object>> getAllObjectsFromTable(String tableName, String column, String column2, Object value, String[] columnNames);

    ResultSet getDescAmountResultsFromTable(String tableName, String orderColumn, int amount);
}
