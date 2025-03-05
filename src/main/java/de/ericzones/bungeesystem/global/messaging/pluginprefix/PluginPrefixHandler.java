// Created by Eric B. 30.01.2021 23:25
package de.ericzones.bungeesystem.global.messaging.pluginprefix;

import de.ericzones.bungeesystem.collectives.database.ISqlAdapter;

import java.util.Map;

public class PluginPrefixHandler extends SqlPluginPrefix {

    private String[] pluginPrefixes = new String[PluginPrefixType.values().length];

    public PluginPrefixHandler(ISqlAdapter sqlAdapter) {
        super(sqlAdapter);
        reloadPluginPrefixes();
    }

    public String getPluginPrefix(PluginPrefixType pluginPrefixType) {
        return pluginPrefixes[pluginPrefixType.getId()]+" ";
    }

    public void reloadPluginPrefixes() {
        Map<PluginPrefixType, String> pluginPrefixList = getPluginPrefixList();
        for(int i = 0; i < pluginPrefixes.length; i++)
            pluginPrefixes[i] = pluginPrefixList.get(PluginPrefixType.getPluginPrefixTypeFromId(i));
    }

}
