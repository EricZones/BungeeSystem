// Created by Eric B. 30.01.2021 18:43
package de.ericzones.bungeesystem.collectives.coreplayer;

import de.dytanic.cloudnet.driver.permission.IPermissionUser;
import de.dytanic.cloudnet.ext.bridge.player.ICloudOfflinePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.collectible.Coins;
import de.ericzones.bungeesystem.collectives.coreplayer.connection.PlayerVersion;
import de.ericzones.bungeesystem.global.language.Language;

import java.util.UUID;

public interface IOfflineCorePlayer {

    String getUsername();

    String getIpAddress();

    UUID getUniqueID();

    String getSkinValue();

    IPermissionUser getPermissionUser();

    boolean hasPermission(String permission);

    boolean canJoinFullServer();
    int getFullServerJoinPower();

    Long getLogoutMillis();
    String getLogoutDate();
    Long getCreationTimeMillis();
    String getCreationTimeDate();

    String getRankPrefix();
    String getRankName();

    Long getPlaytime();
    String getPlaytimeName();
    void resetPlaytime();

    int getCoins();
    void resetCoins();
    void addCoins(int coins);
    void setCoins(int coins);
    boolean removeCoins(int coins);

    PlayerVersion getVersion();

    boolean isOnline();

    Language getLanguage();
    void setLanguage(Language language);
    boolean hasSelectedLanguage();

}
