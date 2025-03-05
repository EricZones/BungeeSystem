// Created by Eric B. 30.01.2021 18:37
package de.ericzones.bungeesystem.collectives.coreplayer;

import com.velocitypowered.api.proxy.Player;
import de.dytanic.cloudnet.ext.bridge.player.ICloudPlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.connection.PlayerConnectionInfo;
import de.ericzones.bungeesystem.collectives.server.ICoreServer;
import de.ericzones.bungeesystem.global.language.Language;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ICorePlayerManager {

    List<ICorePlayer> getCorePlayers();

    ICorePlayer getCorePlayer(UUID uuid);
    ICorePlayer getCorePlayer(String username);
    ICorePlayer getCorePlayer(Player proxyPlayer);
    ICorePlayer getCorePlayer(ICloudPlayer cloudPlayer);
    ICorePlayer getCorePlayer(InetAddress ipAddress);

    IOfflineCorePlayer getOfflineCorePlayer(UUID uuid);
    IOfflineCorePlayer getOfflineCorePlayer(String username);
    IOfflineCorePlayer getOfflineCorePlayer(InetAddress ipAddress);

    int getOnlinePlayerAmount();

    ICorePlayer initialCorePlayer(UUID uuid, String username, String skinValue, PlayerConnectionInfo connectionInfo, ICoreServer coreServer);

    void removeCorePlayer(ICorePlayer corePlayer);

    void serverSwitchCorePlayer(ICorePlayer corePlayer, ICoreServer coreServer);

    PlayerConnectionInfo createConnectionInfo(UUID uuid, String ipAddress);

    void resetOfflineCorePlayerPlaytime(IOfflineCorePlayer offlineCorePlayer);
    void updateOfflineCorePlayerCoins(IOfflineCorePlayer offlineCorePlayer);

    void updateCorePlayerLanguage(ICorePlayer corePlayer, Language language);

    List<IOfflineCorePlayer> getTopPlaytimePlayers(int amount);

}
