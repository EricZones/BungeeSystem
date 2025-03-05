// Created by Eric B. 20.05.2021 17:04
package de.ericzones.bungeesystem.collectives.plugindata.object;

import de.ericzones.bungeesystem.collectives.friend.FriendProperty;

import java.util.Map;
import java.util.UUID;

public class DataFriendPlayer {

    private UUID uuid;
    private Map<UUID, Long> friends;
    private Map<UUID, Long> onlineFriends;
    private Map<UUID, Long> requests;
    private Map<FriendProperty, FriendProperty.Setting> properties;

    public DataFriendPlayer(UUID uuid, Map<UUID, Long> friends, Map<UUID, Long> onlineFriends, Map<UUID, Long> requests, Map<FriendProperty, FriendProperty.Setting> properties) {
        this.uuid = uuid;
        this.friends = friends;
        this.onlineFriends = onlineFriends;
        this.requests = requests;
        this.properties = properties;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Map<UUID, Long> getFriends() {
        return friends;
    }

    public void setFriends(Map<UUID, Long> friends) {
        this.friends = friends;
    }

    public Map<UUID, Long> getOnlineFriends() {
        return onlineFriends;
    }

    public void setOnlineFriends(Map<UUID, Long> onlineFriends) {
        this.onlineFriends = onlineFriends;
    }

    public Map<UUID, Long> getRequests() {
        return requests;
    }

    public void setRequests(Map<UUID, Long> requests) {
        this.requests = requests;
    }

    public Map<FriendProperty, FriendProperty.Setting> getProperties() {
        return properties;
    }

    public void setProperties(Map<FriendProperty, FriendProperty.Setting> properties) {
        this.properties = properties;
    }

    public FriendProperty.Setting getPropertySetting(FriendProperty property) {
        return this.properties.get(property);
    }

    public long getFriendCreationTime(UUID uuid) {
        return this.friends.get(uuid);
    }

    public long getRequestCreationTime(UUID uuid) {
        return this.requests.get(uuid);
    }

    public boolean isFriend(UUID uuid) {
        return this.friends.containsKey(uuid);
    }

    public boolean isRequested(UUID uuid) {
        return this.requests.containsKey(uuid);
    }

}
