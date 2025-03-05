// Created by Eric B. 16.05.2021 13:40
package de.ericzones.bungeesystem.collectives.plugindata.object;

import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.connection.PlayerVersion;
import de.ericzones.bungeesystem.global.language.Language;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class DataCorePlayer {

    private String username, skinValue;
    private UUID uuid;
    private String connectedCoreServer;
    private String ipAddress;
    private PlayerVersion version;
    private Long creationTimeMillis;
    private long playtime;
    private int coins;
    private Language language;
    private String rankName;
    private String rankPrefix;
    private String ping;

    public DataCorePlayer(String username, UUID uuid, String ipAddress, PlayerVersion version, String skinValue, Long creationTimeMillis, Long playtime, int coins, Language language, String connectedCoreServer, String rankName, String rankPrefix, String ping) {
        this.username = username;
        this.skinValue = skinValue;
        this.uuid = uuid;
        this.ipAddress = ipAddress;
        this.version = version;
        this.creationTimeMillis = creationTimeMillis;
        this.playtime = playtime;
        this.coins = coins;
        this.language = language;
        this.connectedCoreServer = connectedCoreServer;
        this.rankName = rankName;
        this.rankPrefix = rankPrefix;
        this.ping = ping;
    }

    public String getConnectedCoreServer() {
        return this.connectedCoreServer;
    }

    public void setConnectedCoreServer(String connectedCoreServer) {
        this.connectedCoreServer = connectedCoreServer;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getSkinValue() {
        return this.skinValue;
    }

    public void setSkinValue(String skinValue) {
        this.skinValue = skinValue;
    }

    public String getIpAddress() {
        return this.ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public PlayerVersion getVersion() {
        return this.version;
    }

    public void setVersion(PlayerVersion version) {
        this.version = version;
    }

    public Long getCreationTimeMillis() {
        return this.creationTimeMillis;
    }

    public void setCreationTimeMillis(Long creationTimeMillis) {
        this.creationTimeMillis = creationTimeMillis;
    }

    public String getCreationTimeDate() {
        Date date = new Date(this.creationTimeMillis);
        date.setHours(date.getHours()+ BungeeSystem.getInstance().getTimezoneNumber());
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
    }

    public Long getPlaytime() {
        return this.playtime;
    }

    public void setPlaytime(long playtime) {
        this.playtime = playtime;
    }

    public int getCoins() {
        return this.coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public Language getLanguage() {
        if(this.language == null)
            return Language.ENGLISH;
        return this.language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean hasSelectedLanguage() {
        return this.language != null;
    }

    public String getRankName() {
        return rankName;
    }

    public String getRankPrefix() {
        return rankPrefix;
    }

    public String getPing() {
        return ping;
    }

}
