// Created by Eric B. 18.02.2021 14:29
package de.ericzones.bungeesystem.collectives.friend;

import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.collectives.plugindata.object.DataFriendPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FriendPlayer {

    private final UUID uuid;
    private Map<UUID, Long> friends;
    private Map<UUID, Long> requests;
    private Map<FriendProperty, FriendProperty.Setting> properties;

    public FriendPlayer(UUID uuid, Map<UUID, Long> friends, Map<UUID, Long> requests, Map<FriendProperty, FriendProperty.Setting> properties) {
        this.uuid = uuid;
        this.friends = friends;
        this.requests = requests;
        this.properties = properties;
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public ICorePlayer getCorePlayer() {
        return BungeeSystem.getInstance().getCorePlayerManager().getCorePlayer(this.uuid);
    }

    public IOfflineCorePlayer getOfflineCorePlayer() {
        return BungeeSystem.getInstance().getCorePlayerManager().getOfflineCorePlayer(this.uuid);
    }

    public Map<UUID, Long> getFriends() {
        return this.friends;
    }

    public Map<UUID, Long> getRequests() {
        return this.requests;
    }

    public boolean isFriend(UUID uuid) {
        return this.friends.containsKey(uuid);
    }

    public boolean isRequested(UUID uuid) {
        return this.requests.containsKey(uuid);
    }

    public long getFriendCreationTime(UUID uuid) {
        return this.friends.get(uuid);
    }

    public long getRequestCreationTime(UUID uuid) {
        return this.requests.get(uuid);
    }

    public Map<FriendProperty, FriendProperty.Setting> getProperties() {
        return this.properties;
    }

    public void setProperty(FriendProperty property, FriendProperty.Setting setting) {
        this.properties.put(property, setting);
    }

    public FriendProperty.Setting getPropertySetting(FriendProperty property) {
        return this.properties.get(property);
    }

    public Map<UUID, Long> getOnlineFriends() {
        Map<UUID, Long> onlineFriends = new HashMap<>();
        for(UUID current : this.friends.keySet()) {
            if (BungeeSystem.getInstance().getCorePlayerManager().getCorePlayer(current) != null)
                onlineFriends.put(current, this.friends.get(current));
        }
        return onlineFriends;
    }

    public Map<UUID, Long> getOnlineRequests() {
        Map<UUID, Long> onlineRequests = new HashMap<>();
        for(UUID current : this.requests.keySet()) {
            if (BungeeSystem.getInstance().getCorePlayerManager().getCorePlayer(current) != null)
                onlineRequests.put(current, this.requests.get(current));
        }
        return onlineRequests;
    }

    public DataFriendPlayer getDataFriendPlayer() {
        return new DataFriendPlayer(this.uuid, this.friends, getOnlineFriends(), this.requests, this.properties);
    }

}
