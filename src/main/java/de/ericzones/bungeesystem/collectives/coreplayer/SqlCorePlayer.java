// Created by Eric B. 30.01.2021 20:31
package de.ericzones.bungeesystem.collectives.coreplayer;

import de.ericzones.bungeesystem.collectives.object.Pair;
import de.ericzones.bungeesystem.collectives.database.ISqlAdapter;
import de.ericzones.bungeesystem.collectives.database.SqlDataType;

import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public abstract class SqlCorePlayer {

    private final ISqlAdapter sqlAdapter;
    private final String tableName = "Core_Coreplayers";
    private final String[] sqlKeys = new String[]{"uuid", "username", "ip", "skinvalue", "version", "logout", "creationtime", "playtime", "coins", "language"};
    private final SqlDataType[] sqlTypes = new SqlDataType[]{SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.TEXT, SqlDataType.VARCHAR,
                                            SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.INT, SqlDataType.INT, SqlDataType.VARCHAR};

    public SqlCorePlayer(ISqlAdapter sqlAdapter) {
        this.sqlAdapter = sqlAdapter;
        this.sqlAdapter.createTable(tableName, getTableInformation(), sqlKeys[0]);
    }

    private Pair<String, SqlDataType>[] getTableInformation() {
        Pair<String, SqlDataType>[] content = new Pair[sqlKeys.length];
        for(int i = 0; i < sqlKeys.length; i++) content[i] = new Pair(sqlKeys[i], sqlTypes[i]);
        return content;
    }

    public boolean corePlayerExists(UUID uuid) {
        return this.sqlAdapter.existsInTable(tableName, sqlKeys[0], uuid.toString());
    }

    public void createCorePlayer(UUID uuid, String username, String ipAddress, String skinValue, String version) {
        this.sqlAdapter.addToTable(tableName, Arrays.asList(sqlKeys), Arrays.asList(uuid, username, ipAddress, skinValue, version, "Online", System.currentTimeMillis(), 0, 0, null));
    }

    public void updateCorePlayer(UUID uuid, String username, String ipAddress, String skinValue, String version) {
        this.sqlAdapter.updateInTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[1], username);
        this.sqlAdapter.updateInTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[2], ipAddress);
        this.sqlAdapter.updateInTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[3], skinValue);
        this.sqlAdapter.updateInTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[4], version);
        this.sqlAdapter.updateInTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[5], "Online");
    }

    public void updateDisconnectedCorePlayer(UUID uuid, Long totalPlaytime, int totalCoins) {
        this.sqlAdapter.updateInTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[5], System.currentTimeMillis());
        this.sqlAdapter.updateInTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[7], totalPlaytime);
        this.sqlAdapter.updateInTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[8], totalCoins);
    }

    public Object getTableInformation(UUID uuid, int id) {
        return this.sqlAdapter.getObjectFromTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[id]);
    }

    public UUID getCorePlayerUUID(String username) {
        String result = String.valueOf(this.sqlAdapter.getObjectFromTable(tableName, sqlKeys[1], username, sqlKeys[0]));
        if(result == null || result.equalsIgnoreCase("null")) {
            return null;
        }
        return UUID.fromString(result);
    }

    public UUID getCorePlayerUUID(InetAddress ipAddress) {
        String result = String.valueOf(this.sqlAdapter.getObjectFromTable(tableName, sqlKeys[2], ipAddress.getHostAddress(), sqlKeys[0]));
        if(result == null || result.equalsIgnoreCase("null")) {
            return null;
        }
        return UUID.fromString(result);
    }

    public String getCorePlayerIpAddress(UUID uuid) {
        return String.valueOf(this.sqlAdapter.getObjectFromTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[2]));
    }

    public String getCorePlayerVersion(UUID uuid) {
        return String.valueOf(this.sqlAdapter.getObjectFromTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[4]));
    }

    public Long getCorePlayerCreationTimeMillis(UUID uuid) {
        return Long.parseLong(String.valueOf(this.sqlAdapter.getObjectFromTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[6])));
    }

    public Long getCorePlayerLogoutMillis(UUID uuid) {
        String logout = String.valueOf(this.sqlAdapter.getObjectFromTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[5]));
        try {
            return Long.parseLong(logout);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public Long getCorePlayerPreviousPlaytime(UUID uuid) {
        return Long.parseLong(String.valueOf(this.sqlAdapter.getObjectFromTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[7])));
    }

    public void resetCorePlayerPlaytime(UUID uuid) {
        this.sqlAdapter.updateInTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[7], 0);
    }

    public int getCorePlayerCoins(UUID uuid) {
        return Integer.parseInt(String.valueOf(this.sqlAdapter.getObjectFromTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[8])));
    }

    public void updateCorePlayerCoins(UUID uuid, int coins) {
        this.sqlAdapter.updateInTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[8], coins);
    }

    public String getCorePlayerLanguage(UUID uuid) {
        return String.valueOf(this.sqlAdapter.getObjectFromTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[9]));
    }

    public void updateCorePlayerLanguage(UUID uuid, String language) {
        this.sqlAdapter.updateInTable(tableName, sqlKeys[0], uuid.toString(), sqlKeys[9], language);
    }

    public List<UUID> loadTopPlaytimePlayers(int amount) {
        ResultSet resultSet = this.sqlAdapter.getDescAmountResultsFromTable(tableName, sqlKeys[7], amount);
        List<UUID> topPlaytimePlayers = new ArrayList<>();
        try {
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString(sqlKeys[0]));
                topPlaytimePlayers.add(uuid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return topPlaytimePlayers;
    }

}
