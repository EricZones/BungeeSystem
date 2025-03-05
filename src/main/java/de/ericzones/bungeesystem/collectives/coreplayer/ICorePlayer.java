// Created by Eric B. 30.01.2021 18:38
package de.ericzones.bungeesystem.collectives.coreplayer;

import com.velocitypowered.api.proxy.Player;
import de.dytanic.cloudnet.driver.permission.IPermissionUser;
import de.dytanic.cloudnet.ext.bridge.player.ICloudPlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.collectible.Coins;
import de.ericzones.bungeesystem.collectives.coreplayer.connection.PlayerConnectionInfo;
import de.ericzones.bungeesystem.collectives.coreplayer.collectible.Playtime;
import de.ericzones.bungeesystem.collectives.plugindata.object.DataCorePlayer;
import de.ericzones.bungeesystem.collectives.server.ICoreServer;
import de.ericzones.bungeesystem.global.language.Language;
import net.kyori.adventure.text.TextComponent;

import java.util.UUID;

public interface ICorePlayer {

    String getUsername();
    UUID getUniqueID();
    String getSkinValue();

    IPermissionUser getPermissionUser();
    ICloudPlayer getCloudPlayer();
    Player getProxyPlayer();

    PlayerConnectionInfo getConnectionInfo();
    ICoreServer getConnectedCoreServer();
    void setConnectedCoreServer(ICoreServer coreServer);

    boolean hasPermission(String permission);

    void sendMessage(String message);
    void sendMessage(TextComponent textComponent);

    void disconnect(String reason);
    void sendToLobby();
    CorePlayerSwitchResult sendToServer(ICoreServer coreServer);
    CorePlayerSwitchResult sendToServerAsAdmin(ICoreServer coreServer);

    boolean canJoinFullServer();
    int getFullServerJoinPower();

    Long getCreationTimeMillis();
    String getCreationTimeDate();

    String getRankPrefix();
    String getRankName();

    Playtime getPlaytime();
    void resetPlaytime();

    Coins getCoins();

    Language getLanguage();
    void setLanguage(Language language);
    boolean hasSelectedLanguage();

    String getPing();

    DataCorePlayer getDataCorePlayer();

}
