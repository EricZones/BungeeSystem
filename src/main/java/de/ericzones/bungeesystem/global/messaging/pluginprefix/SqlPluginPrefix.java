// Created by Eric B. 04.02.2021 23:18
package de.ericzones.bungeesystem.global.messaging.pluginprefix;

import de.ericzones.bungeesystem.collectives.database.ISqlAdapter;
import de.ericzones.bungeesystem.collectives.database.SqlDataType;
import de.ericzones.bungeesystem.collectives.object.Pair;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;

import java.util.*;

public abstract class SqlPluginPrefix {

    private final ISqlAdapter sqlAdapter;
    private final String tableName = "Core_Pluginprefixes";
    private final String[] sqlKeys = new String[]{"type", "prefix"};
    private final SqlDataType[] sqlTypes = new SqlDataType[]{SqlDataType.VARCHAR, SqlDataType.VARCHAR};

    public SqlPluginPrefix(ISqlAdapter sqlAdapter) {
        this.sqlAdapter = sqlAdapter;
        this.sqlAdapter.createTable(tableName, getTableInformation(), sqlKeys[0]);
    }

    private Pair<String, SqlDataType>[] getTableInformation() {
        Pair<String, SqlDataType>[] content = new Pair[sqlKeys.length];
        for(int i = 0; i < sqlKeys.length; i++) content[i] = new Pair(sqlKeys[i], sqlTypes[i]);
        return content;
    }

    public Map<PluginPrefixType, String> getPluginPrefixList() {
        Map<PluginPrefixType, String> pluginPrefixList = new HashMap<>();
        for(PluginPrefixType current : PluginPrefixType.values()) {
            if(existsInTable(sqlKeys[0], current.toString()))
                pluginPrefixList.put(current, String.valueOf(this.sqlAdapter.getObjectFromTable(tableName, sqlKeys[0], current.toString(), sqlKeys[1])));
            else {
                pluginPrefixList.put(current, current.getStandardPrefix());
                createPluginPrefix(current);
            }
        }
        return pluginPrefixList;
    }

    private void createPluginPrefix(PluginPrefixType pluginPrefixType) {
        this.sqlAdapter.addToTable(tableName, Arrays.asList(sqlKeys), Arrays.asList(new String[]{pluginPrefixType.toString(), pluginPrefixType.getStandardPrefix()}));
    }

    private boolean existsInTable(String column, String value) {
        return this.sqlAdapter.existsInTable(tableName, column, value);
    }
}
