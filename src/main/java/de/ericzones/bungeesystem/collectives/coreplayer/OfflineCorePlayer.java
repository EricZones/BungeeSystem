// Created by Eric B. 30.01.2021 21:13
package de.ericzones.bungeesystem.collectives.coreplayer;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.permission.IPermissionUser;
import de.dytanic.cloudnet.ext.bridge.player.ICloudOfflinePlayer;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.collectible.Coins;
import de.ericzones.bungeesystem.collectives.coreplayer.connection.PlayerVersion;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.permission.PermissionType;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public class OfflineCorePlayer implements IOfflineCorePlayer {

    private String username, skinValue, ipAddress;
    private UUID uuid;
    private Long logoutMillis, creationTimeMillis, playtime;
    private Coins coins;
    private PlayerVersion version;
    private Language language;

    public OfflineCorePlayer(String username, String previousIpAddress, UUID uuid, String skinValue, Long logoutMillis, Long creationTimeMillis, Long previousPlaytime, int previousCoins, String previousVersion, String language) {
        this.username = username;
        this.skinValue = skinValue;
        this.uuid = uuid;
        this.logoutMillis = logoutMillis;
        this.creationTimeMillis = creationTimeMillis;
        this.playtime = previousPlaytime;
        this.coins = new Coins(previousCoins);
        this.version = PlayerVersion.getPlayerVersionByName(previousVersion);
        this.ipAddress = previousIpAddress;
        if(!language.equals("null"))
            this.language = Language.valueOf(language);
        else
            this.language = null;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getIpAddress() {
        return this.ipAddress;
    }

    @Override
    public UUID getUniqueID() {
        return this.uuid;
    }

    @Override
    public String getSkinValue() {
        return this.skinValue;
    }

    @Override
    public IPermissionUser getPermissionUser() {
        return CloudNetDriver.getInstance().getPermissionManagement().getUser(uuid);
    }

    @Override
    public boolean hasPermission(String permission) {
        return CloudNetDriver.getInstance().getPermissionManagement().hasPermission(getPermissionUser(), permission);
    }

    @Override
    public boolean canJoinFullServer() {
        return hasPermission(BungeeSystem.getInstance().getPermissionHandler().getPermission(PermissionType.FULLSERVERJOIN));
    }

    @Override
    public int getFullServerJoinPower() {
        return CloudNetDriver.getInstance().getPermissionManagement().getHighestPermissionGroup(getPermissionUser()).getPotency();
    }

    @Override
    public Long getLogoutMillis() {
        return this.logoutMillis;
    }

    @Override
    public String getLogoutDate() {
        Date date = new Date(this.logoutMillis);
        date.setHours(date.getHours()+BungeeSystem.getInstance().getTimezoneNumber());
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
    }

    @Override
    public Long getCreationTimeMillis() {
        return this.creationTimeMillis;
    }

    @Override
    public String getCreationTimeDate() {
        Date date = new Date(this.creationTimeMillis);
        date.setHours(date.getHours()+BungeeSystem.getInstance().getTimezoneNumber());
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
    }

    @Override
    public String getRankName() {
        return CloudNetDriver.getInstance().getPermissionManagement().getHighestPermissionGroup(getPermissionUser()).getName();
    }

    @Override
    public String getRankPrefix() {
        return CloudNetDriver.getInstance().getPermissionManagement().getHighestPermissionGroup(getPermissionUser()).getColor();
    }

    @Override
    public Long getPlaytime() {
        return this.playtime;
    }

    @Override
    public String getPlaytimeName() {
        long playtimeMillis = this.playtime;
        long seconds = 0, minutes = 0, hours = 0, days = 0;
        while(playtimeMillis > 1000) {
            playtimeMillis-=1000;
            seconds++;
        }
        while(seconds > 60) {
            seconds-=60;
            minutes++;
        }
        while(minutes > 60) {
            minutes-=60;
            hours++;
        }
        while(hours > 24) {
            hours-=24;
            days++;
        }
        return days + "d " + hours + "h " + minutes + "m";
    }

    @Override
    public void resetPlaytime() {
        BungeeSystem.getInstance().getCorePlayerManager().resetOfflineCorePlayerPlaytime(this);
        this.playtime = 0L;
    }

    @Override
    public int getCoins() {
        return this.coins.getCurrentCoins();
    }

    @Override
    public void resetCoins() {
        this.coins.resetCoins();
        BungeeSystem.getInstance().getCorePlayerManager().updateOfflineCorePlayerCoins(this);
    }

    @Override
    public void addCoins(int coins) {
        this.coins.addCoins(coins);
        BungeeSystem.getInstance().getCorePlayerManager().updateOfflineCorePlayerCoins(this);
    }

    @Override
    public void setCoins(int coins) {
        this.coins.setCoins(coins);
        BungeeSystem.getInstance().getCorePlayerManager().updateOfflineCorePlayerCoins(this);
    }

    @Override
    public boolean removeCoins(int coins) {
        boolean success = this.coins.removeCoins(coins);
        if(success)
            BungeeSystem.getInstance().getCorePlayerManager().updateOfflineCorePlayerCoins(this);
        return success;
    }

    @Override
    public PlayerVersion getVersion() {
        return this.version;
    }

    @Override
    public boolean isOnline() {
        return BungeeSystem.getInstance().getCorePlayerManager().getCorePlayer(this.uuid) != null;
    }

    @Override
    public Language getLanguage() {
        if(this.language == null)
            return Language.ENGLISH;
        return this.language;
    }

    @Override
    public void setLanguage(Language language) {
        this.language = language;
    }

    @Override
    public boolean hasSelectedLanguage() {
        return this.language != null;
    }

}
