// Created by Eric B. 31.01.2021 14:40
package de.ericzones.bungeesystem.collectives.server;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.server.connection.ServerConnectionInfo;
import de.ericzones.bungeesystem.collectives.server.whitelist.Whitelist;
import net.kyori.adventure.text.TextComponent;

import java.util.List;
import java.util.UUID;

public interface ICoreServer {

    String getServerName();

    int getCorePlayerCount();

    List<ICorePlayer> getCorePlayers();

    RegisteredServer getProxyServer();

    ServiceInfoSnapshot getServiceInfoSnapshot();

    ServerConnectionInfo getConnectionInfo();

    int getMaxPlayerCount();
    void setMaxPlayerCount(int maxPlayerCount);

    boolean isFull();

    void addCorePlayer(ICorePlayer corePlayer);

    void removeCorePlayer(ICorePlayer corePlayer);

    boolean containsCorePlayer(ICorePlayer corePlayer);

    CoreServerType getServerType();

    ICorePlayer getCorePlayer(UUID uuid);
    ICorePlayer getCorePlayer(String username);

    void stopServer(String reason);

    void kickAllPlayer(String reason, List<ICorePlayer> ignorePlayers);

    ICorePlayer getLowerJoinPowerPlayer(int joinPower);

    boolean isRestricted();
    boolean isInMaintenance();

    void setRestricted(boolean restricted);
    void setMaintenance(boolean maintenance);

    Whitelist getWhitelist();

    boolean isPlaytimeEnabled();

    void broadcastMessage(String message);
    void broadcastMessage(TextComponent message);

}
