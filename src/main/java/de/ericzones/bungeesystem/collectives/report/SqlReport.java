// Created by Eric B. 15.02.2021 12:07
package de.ericzones.bungeesystem.collectives.report;

import de.ericzones.bungeesystem.collectives.database.ISqlAdapter;
import de.ericzones.bungeesystem.collectives.database.SqlDataType;
import de.ericzones.bungeesystem.collectives.object.Pair;

import java.util.*;

public abstract class SqlReport {

    private final ISqlAdapter sqlAdapter;
    private final String tableName = "Report_Reportlist";
    private final String[] sqlKeys = new String[]{"creationtime", "creationdate", "target", "reason", "creator"};
    private final SqlDataType[] sqlTypes = new SqlDataType[]{SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR,
                                                    SqlDataType.VARCHAR};

    public SqlReport(ISqlAdapter sqlAdapter) {
        this.sqlAdapter = sqlAdapter;
        this.sqlAdapter.createTable(tableName, getTableInformation(), sqlKeys[0]);
    }

    private Pair<String, SqlDataType>[] getTableInformation() {
        Pair<String, SqlDataType>[] content = new Pair[sqlKeys.length];
        for(int i = 0; i < sqlKeys.length; i++) content[i] = new Pair(sqlKeys[i], sqlTypes[i]);
        return content;
    }

    public void createReport(UUID creator, UUID target, ReportReason reason, String creationTimeName, Long creationTime) {
        this.sqlAdapter.addToTable(tableName, Arrays.asList(sqlKeys), Arrays.asList(creationTime, creationTimeName, target.toString(), reason.toString(), creator.toString()));
    }

    public void deleteReport(Long creationTime) {
        this.sqlAdapter.removeFromTable(tableName, sqlKeys[0], creationTime);
    }

    private boolean isReportExpired(Long creationTime) {
        if(creationTime+24*60*60*1000 < System.currentTimeMillis()) {
            deleteReport(creationTime);
            return true;
        }
        return false;
    }

    public Map<Long, Map<ReportProperty, String>> getAllReports() {
        Map<String, List<Object>> objectList = this.sqlAdapter.getAllObjectsFromTablePrimaryKey(tableName, sqlKeys);
        Map<Long, Map<ReportProperty, String>> reports = new HashMap<>();

        for(String current : objectList.keySet()) {
            Long creationTime = Long.parseLong(current);
            List<Object> currentList = objectList.get(current);
            Map<ReportProperty, String> properties = new HashMap<>();
            for(int i = 0; i < currentList.size(); i++)
                properties.put(ReportProperty.valueOf(sqlKeys[i+1].toUpperCase()), String.valueOf(currentList.get(i)));

            if(isReportExpired(creationTime))
                continue;
            reports.put(creationTime, properties);
        }
        return reports;
    }

}
