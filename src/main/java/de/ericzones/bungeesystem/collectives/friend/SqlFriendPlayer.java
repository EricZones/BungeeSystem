// Created by Eric B. 18.02.2021 15:09
package de.ericzones.bungeesystem.collectives.friend;

import de.ericzones.bungeesystem.collectives.database.ISqlAdapter;
import de.ericzones.bungeesystem.collectives.database.SqlDataType;
import de.ericzones.bungeesystem.collectives.object.Pair;

import java.util.*;

public abstract class SqlFriendPlayer {

    private final ISqlAdapter sqlAdapter;

    private final String tableNamePlayers = "Friend_Friendplayers";
    private final String[] sqlKeysPlayers = new String[]{"uuid", "requests", "partyinvites", "messages", "jumping", "status", "friendstatus", "joinmes"};
    private final SqlDataType[] sqlTypesPlayers = new SqlDataType[]{SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR,
                                                                SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR};

    private final String tableNameFriends = "Friend_Friendlist";
    private final String[] sqlKeysFriends = new String[]{"creationtime", "uuid", "uuid2"};
    private final SqlDataType[] sqlTypesFriends = new SqlDataType[]{SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR};

    private final String tableNameRequests = "Friend_Requestlist";
    private final String[] sqlKeysRequests = new String[]{"creationtime", "requester", "target"};
    private final SqlDataType[] sqlTypesRequests = new SqlDataType[]{SqlDataType.VARCHAR, SqlDataType.VARCHAR, SqlDataType.VARCHAR};

    public SqlFriendPlayer(ISqlAdapter sqlAdapter) {
        this.sqlAdapter = sqlAdapter;
        this.sqlAdapter.createTable(tableNamePlayers, getTableInformation(tableNamePlayers), sqlKeysPlayers[0]);
        this.sqlAdapter.createTable(tableNameFriends, getTableInformation(tableNameFriends), sqlKeysFriends[0]);
        this.sqlAdapter.createTable(tableNameRequests, getTableInformation(tableNameRequests), sqlKeysRequests[0]);
    }

    private Pair<String, SqlDataType>[] getTableInformation(String tableName) {
        Pair<String, SqlDataType>[] content;
        switch (tableName) {
            case tableNamePlayers:
                content = new Pair[sqlKeysPlayers.length];
                for(int i = 0; i < sqlKeysPlayers.length; i++) content[i] = new Pair(sqlKeysPlayers[i], sqlTypesPlayers[i]);
                break;
            case tableNameFriends:
                content = new Pair[sqlKeysFriends.length];
                for(int i = 0; i < sqlKeysFriends.length; i++) content[i] = new Pair(sqlKeysFriends[i], sqlTypesFriends[i]);
                break;
            case tableNameRequests:
                content = new Pair[sqlKeysRequests.length];
                for(int i = 0; i < sqlKeysRequests.length; i++) content[i] = new Pair(sqlKeysRequests[i], sqlTypesRequests[i]);
                break;
            default:
                content = new Pair[1];
                break;
        }
        return content;
    }

    public void createFriendPlayer(UUID uuid) {
        this.sqlAdapter.addToTable(tableNamePlayers, Arrays.asList(sqlKeysPlayers), Arrays.asList(FriendProperty.getDefaultValues(uuid)));
    }

    public void updateFriendPlayer(UUID uuid, Map<FriendProperty, FriendProperty.Setting> properties) {
        this.sqlAdapter.updateInTable(tableNamePlayers, sqlKeysPlayers[0], uuid.toString(), sqlKeysPlayers[1], properties.get(FriendProperty.REQUESTS).toString());
        this.sqlAdapter.updateInTable(tableNamePlayers, sqlKeysPlayers[0], uuid.toString(), sqlKeysPlayers[2], properties.get(FriendProperty.PARTYINVITES).toString());
        this.sqlAdapter.updateInTable(tableNamePlayers, sqlKeysPlayers[0], uuid.toString(), sqlKeysPlayers[3], properties.get(FriendProperty.MESSAGES).toString());
        this.sqlAdapter.updateInTable(tableNamePlayers, sqlKeysPlayers[0], uuid.toString(), sqlKeysPlayers[4], properties.get(FriendProperty.JUMPING).toString());
        this.sqlAdapter.updateInTable(tableNamePlayers, sqlKeysPlayers[0], uuid.toString(), sqlKeysPlayers[5], properties.get(FriendProperty.STATUS).toString());
        this.sqlAdapter.updateInTable(tableNamePlayers, sqlKeysPlayers[0], uuid.toString(), sqlKeysPlayers[6], properties.get(FriendProperty.FRIENDSTATUS).toString());
        this.sqlAdapter.updateInTable(tableNamePlayers, sqlKeysPlayers[0], uuid.toString(), sqlKeysPlayers[7], properties.get(FriendProperty.JOINMES).toString());
    }

    public void createFriendRequest(UUID requester, UUID target) {
        this.sqlAdapter.addToTable(tableNameRequests, Arrays.asList(sqlKeysRequests), Arrays.asList(String.valueOf(System.currentTimeMillis()), requester.toString(), target.toString()));
    }

    public void deleteFriendRequest(UUID requester, UUID target) {
        this.sqlAdapter.removeFromTable(tableNameRequests, sqlKeysRequests[1], requester.toString(), sqlKeysRequests[2], target.toString());
    }

    public void createFriendship(UUID uuid, UUID uuid2) {
        this.sqlAdapter.addToTable(tableNameFriends, Arrays.asList(sqlKeysFriends), Arrays.asList(String.valueOf(System.currentTimeMillis()), uuid.toString(), uuid2.toString()));
    }

    public void deleteFriendship(UUID uuid, UUID uuid2) {
        this.sqlAdapter.removeFromTable(tableNameFriends, sqlKeysFriends[1], uuid, sqlKeysFriends[2], uuid2);
        this.sqlAdapter.removeFromTable(tableNameFriends, sqlKeysFriends[2], uuid, sqlKeysFriends[1], uuid2);
    }

    public Map<FriendProperty, FriendProperty.Setting> getFriendPlayerSettings(UUID uuid) {
        Map<FriendProperty, FriendProperty.Setting> properties = new HashMap<>();
        FriendProperty.Setting requests = FriendProperty.Setting.valueOf(String.valueOf(this.sqlAdapter.getObjectFromTable(tableNamePlayers, sqlKeysPlayers[0], uuid.toString(), sqlKeysPlayers[1])));
        FriendProperty.Setting partyInvites = FriendProperty.Setting.valueOf(String.valueOf(this.sqlAdapter.getObjectFromTable(tableNamePlayers, sqlKeysPlayers[0], uuid.toString(), sqlKeysPlayers[2])));
        FriendProperty.Setting messages = FriendProperty.Setting.valueOf(String.valueOf(this.sqlAdapter.getObjectFromTable(tableNamePlayers, sqlKeysPlayers[0], uuid.toString(), sqlKeysPlayers[3])));
        FriendProperty.Setting jumping = FriendProperty.Setting.valueOf(String.valueOf(this.sqlAdapter.getObjectFromTable(tableNamePlayers, sqlKeysPlayers[0], uuid.toString(), sqlKeysPlayers[4])));
        FriendProperty.Setting status = FriendProperty.Setting.valueOf(String.valueOf(this.sqlAdapter.getObjectFromTable(tableNamePlayers, sqlKeysPlayers[0], uuid.toString(), sqlKeysPlayers[5])));
        FriendProperty.Setting friendStatus = FriendProperty.Setting.valueOf(String.valueOf(this.sqlAdapter.getObjectFromTable(tableNamePlayers, sqlKeysPlayers[0], uuid.toString(), sqlKeysPlayers[6])));
        FriendProperty.Setting joinmes =  FriendProperty.Setting.valueOf(String.valueOf(this.sqlAdapter.getObjectFromTable(tableNamePlayers, sqlKeysPlayers[0], uuid.toString(), sqlKeysPlayers[7])));
        properties.put(FriendProperty.REQUESTS, requests); properties.put(FriendProperty.PARTYINVITES, partyInvites);
        properties.put(FriendProperty.MESSAGES, messages); properties.put(FriendProperty.JUMPING, jumping);
        properties.put(FriendProperty.STATUS, status); properties.put(FriendProperty.FRIENDSTATUS, friendStatus);
        properties.put(FriendProperty.JOINMES, joinmes);
        return properties;
    }

    public Map<UUID, Map<FriendProperty, FriendProperty.Setting>> getAllFriendPlayers() {
        Map<String, List<Object>> objectList = this.sqlAdapter.getAllObjectsFromTablePrimaryKey(tableNamePlayers, sqlKeysPlayers);
        Map<UUID, Map<FriendProperty, FriendProperty.Setting>> friendPlayers = new HashMap<>();

        for(String current : objectList.keySet()) {
            UUID uuid = UUID.fromString(current);
            List<Object> currentList = objectList.get(current);
            Map<FriendProperty, FriendProperty.Setting> properties = new HashMap<>();
            for(int i = 0; i < currentList.size(); i++)
                properties.put(FriendProperty.valueOf(sqlKeysPlayers[i+1].toUpperCase()), FriendProperty.Setting.valueOf(String.valueOf(currentList.get(i))));
            friendPlayers.put(uuid, properties);
        }
        return friendPlayers;
    }

    public Map<UUID, Long> getFriendPlayerFriends(UUID uuid) {
        List<List<Object>> objectList = this.sqlAdapter.getAllObjectsFromTable(tableNameFriends, sqlKeysFriends[1], sqlKeysFriends[2], uuid.toString(), sqlKeysFriends);
        Map<UUID, Long> friends = new HashMap<>();

        for(List<Object> current : objectList) {
            long creationTime = Long.parseLong(String.valueOf(current.get(0)));
            UUID uuid1 = UUID.fromString(String.valueOf(current.get(1)));
            UUID uuid2 = UUID.fromString(String.valueOf(current.get(2)));
            if(!uuid1.equals(uuid))
                friends.put(uuid1, creationTime);
            else
                friends.put(uuid2, creationTime);
        }

        return friends;
    }

    public Map<UUID, Long> getFriendPlayerRequests(UUID uuid) {
        List<List<Object>> objectList = this.sqlAdapter.getAllObjectsFromTable(tableNameRequests, sqlKeysRequests[2], uuid.toString(), sqlKeysRequests);
        Map<UUID, Long> requests = new HashMap<>();

        for(List<Object> current : objectList) {
            long creationTime = Long.parseLong(String.valueOf(current.get(0)));
            UUID requester = UUID.fromString(String.valueOf(current.get(1)));
            requests.put(requester, creationTime);
        }

        return requests;
    }

}
