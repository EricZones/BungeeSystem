// Created by Eric B. 12.02.2021 22:27
package de.ericzones.bungeesystem.collectives.punish;

import de.ericzones.bungeesystem.collectives.database.ISqlAdapter;
import de.ericzones.bungeesystem.collectives.database.SqlDataType;
import de.ericzones.bungeesystem.collectives.object.Pair;
import de.ericzones.bungeesystem.collectives.punish.ban.BanReason;
import de.ericzones.bungeesystem.collectives.punish.mute.MuteReason;

import java.util.*;

public abstract class SqlPunish {

    private final ISqlAdapter sqlAdapter;

    private final String tableNameBans = "Punish_Banlist";
    private final String[] sqlKeysBans = new String[]{"uuid", "ip", "reason", "expiry", "creator", "creationtime"};
    private final SqlDataType[] sqlTypesBans = new SqlDataType[]{SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR,
            SqlDataType.VARCHAR};

    private final String tableNameBanHistory = "Punish_Banhistory";
    private final String[] sqlKeysBanHistory = new String[]{"uuid", "reason", "creator", "creationtime"};
    private final SqlDataType[] sqlTypesBanHistory = new SqlDataType[]{SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR};

    private final String tableNameMutes = "Punish_Mutelist";
    private final String[] sqlKeysMutes = new String[]{"uuid", "reason", "expiry", "creator", "creationtime"};
    private final SqlDataType[] sqlTypesMutes = new SqlDataType[]{SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR,
            SqlDataType.VARCHAR};

    private final String tableNameMuteHistory = "Punish_Mutehistory";
    private final String[] sqlKeysMuteHistory = new String[]{"uuid", "reason", "creator", "creationtime"};
    private final SqlDataType[] sqlTypesMuteHistory = new SqlDataType[]{SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR};

    public SqlPunish(ISqlAdapter sqlAdapter) {
        this.sqlAdapter = sqlAdapter;
        this.sqlAdapter.createTable(tableNameBans, getTableInformation(tableNameBans), sqlKeysBans[0]);
        this.sqlAdapter.createTable(tableNameBanHistory, getTableInformation(tableNameBanHistory), sqlKeysBanHistory[3]);
        this.sqlAdapter.createTable(tableNameMutes, getTableInformation(tableNameMutes), sqlKeysMutes[0]);
        this.sqlAdapter.createTable(tableNameMuteHistory, getTableInformation(tableNameMuteHistory), sqlKeysMuteHistory[3]);
    }

    private Pair<String, SqlDataType>[] getTableInformation(String tableName) {
        Pair<String, SqlDataType>[] content;
        switch (tableName) {
            case tableNameBans:
                content = new Pair[sqlKeysBans.length];
                for(int i = 0; i < sqlKeysBans.length; i++) content[i] = new Pair(sqlKeysBans[i], sqlTypesBans[i]);
                break;
            case tableNameBanHistory:
                content = new Pair[sqlKeysBanHistory.length];
                for(int i = 0; i < sqlKeysBanHistory.length; i++) content[i] = new Pair(sqlKeysBanHistory[i], sqlTypesBanHistory[i]);
                break;
            case tableNameMutes:
                content = new Pair[sqlKeysMutes.length];
                for(int i = 0; i < sqlKeysMutes.length; i++) content[i] = new Pair(sqlKeysMutes[i], sqlTypesMutes[i]);
                break;
            case tableNameMuteHistory:
                content = new Pair[sqlKeysMuteHistory.length];
                for(int i = 0; i < sqlKeysMuteHistory.length; i++) content[i] = new Pair(sqlKeysMuteHistory[i], sqlTypesMuteHistory[i]);
                break;
            default:
                content = new Pair[1];
                break;
        }
        return content;
    }

    public void createBan(UUID uuid, String ipAddress, BanReason reason, Long expiry, UUID creator, Long creationTime) {
        this.sqlAdapter.addToTable(tableNameBans, Arrays.asList(sqlKeysBans), Arrays.asList(uuid.toString(), ipAddress, reason.toString(), expiry, creator.toString(), creationTime));
        this.sqlAdapter.addToTable(tableNameBanHistory, Arrays.asList(sqlKeysBanHistory), Arrays.asList(uuid.toString(), reason.toString(), creator.toString(), creationTime));
    }

    public void deleteBan(UUID uuid) {
        this.sqlAdapter.removeFromTable(tableNameBans, sqlKeysBans[0], uuid.toString());
    }

    public void deleteBanHistory(UUID uuid) {
        this.sqlAdapter.removeFromTable(tableNameBanHistory, sqlKeysBanHistory[0], uuid.toString());
    }

    public Map<UUID, Map<PunishProperty, String>> getAllBans() {
        Map<String, List<Object>> objectList = this.sqlAdapter.getAllObjectsFromTablePrimaryKey(tableNameBans, sqlKeysBans);
        Map<UUID, Map<PunishProperty, String>> bans = new HashMap<>();

        for(String current : objectList.keySet()) {
            UUID uuid = UUID.fromString(current);
            List<Object> currentList = objectList.get(current);
            Map<PunishProperty, String> properties = new HashMap<>();
            for(int i = 0; i < currentList.size(); i++)
                properties.put(PunishProperty.valueOf(sqlKeysBans[i+1].toUpperCase()), String.valueOf(currentList.get(i)));

            if(isBanExpired(uuid, Long.parseLong(properties.get(PunishProperty.EXPIRY))))
                continue;
            bans.put(uuid, properties);
        }
        return bans;
    }

    public List<Pair<UUID, Map<PunishProperty, String>>> getAllBanHistories() {
        List<List<Object>> objectList = this.sqlAdapter.getAllObjectsFromTable(tableNameBanHistory, sqlKeysBanHistory);
        List<Pair<UUID, Map<PunishProperty, String>>> banHistories = new ArrayList<>();

        for(List<Object> current : objectList) {
            Map<PunishProperty, String> properties = new HashMap<>();
            for(int i = 0; i < current.size(); i++)
                properties.put(PunishProperty.valueOf(sqlKeysBanHistory[i].toUpperCase()), String.valueOf(current.get(i)));
            UUID uuid = UUID.fromString(properties.get(PunishProperty.UUID));
            properties.remove(PunishProperty.UUID);

            Pair<UUID, Map<PunishProperty, String>> currentPair = new Pair<>(uuid, properties);
            banHistories.add(currentPair);
        }
        return banHistories;
    }

    private boolean isBanExpired(UUID uuid, Long expiry) {
        if(expiry < System.currentTimeMillis()) {
            deleteBan(uuid);
            return true;
        }
        return false;
    }

    public void createMute(UUID uuid, MuteReason reason, Long expiry, UUID creator, Long creationTime) {
        this.sqlAdapter.addToTable(tableNameMutes, Arrays.asList(sqlKeysMutes), Arrays.asList(uuid.toString(), reason.toString(), expiry, creator.toString(), creationTime));
        this.sqlAdapter.addToTable(tableNameMuteHistory, Arrays.asList(sqlKeysMuteHistory), Arrays.asList(uuid.toString(), reason.toString(), creator.toString(), creationTime));
    }

    public void deleteMute(UUID uuid) {
        this.sqlAdapter.removeFromTable(tableNameMutes, sqlKeysMutes[0], uuid.toString());
    }

    public void deleteMuteHistory(UUID uuid) {
        this.sqlAdapter.removeFromTable(tableNameMuteHistory, sqlKeysMuteHistory[0], uuid.toString());
    }

    public Map<UUID, Map<PunishProperty, String>> getAllMutes() {
        Map<String, List<Object>> objectList = this.sqlAdapter.getAllObjectsFromTablePrimaryKey(tableNameMutes, sqlKeysMutes);
        Map<UUID, Map<PunishProperty, String>> mutes = new HashMap<>();

        for(String current : objectList.keySet()) {
            UUID uuid = UUID.fromString(current);
            List<Object> currentList = objectList.get(current);
            Map<PunishProperty, String> properties = new HashMap<>();
            for(int i = 0; i < currentList.size(); i++)
                properties.put(PunishProperty.valueOf(sqlKeysMutes[i+1].toUpperCase()), String.valueOf(currentList.get(i)));

            if(isMuteExpired(uuid, Long.parseLong(properties.get(PunishProperty.EXPIRY))))
                continue;
            mutes.put(uuid, properties);
        }
        return mutes;
    }

    public List<Pair<UUID, Map<PunishProperty, String>>> getAllMuteHistories() {
        List<List<Object>> objectList = this.sqlAdapter.getAllObjectsFromTable(tableNameMuteHistory, sqlKeysMuteHistory);
        List<Pair<UUID, Map<PunishProperty, String>>> muteHistories = new ArrayList<>();

        for(List<Object> current : objectList) {
            Map<PunishProperty, String> properties = new HashMap<>();
            for(int i = 0; i < current.size(); i++)
                properties.put(PunishProperty.valueOf(sqlKeysMuteHistory[i].toUpperCase()), String.valueOf(current.get(i)));
            UUID uuid = UUID.fromString(properties.get(PunishProperty.UUID));
            properties.remove(PunishProperty.UUID);

            Pair<UUID, Map<PunishProperty, String>> currentPair = new Pair<>(uuid, properties);
            muteHistories.add(currentPair);
        }
        return muteHistories;
    }

    private boolean isMuteExpired(UUID uuid, Long expiry) {
        if(expiry < System.currentTimeMillis()) {
            deleteMute(uuid);
            return true;
        }
        return false;
    }

}
