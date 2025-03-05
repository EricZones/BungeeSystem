// Created by Eric B. 31.01.2021 16:50
package de.ericzones.bungeesystem.collectives.server;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.server.connection.ServerConnectionInfo;
import de.ericzones.bungeesystem.collectives.server.connection.ServerConnectionStatus;

import java.util.List;

public interface ICoreServerManager {

    List<ICoreServer> getCoreServers();

    ICoreServer getCoreServer(ICorePlayer corePlayer);
    ICoreServer getCoreServer(String serverName);
    ICoreServer getCoreServer(ServiceInfoSnapshot serviceInfoSnapshot);
    ICoreServer getCoreServer(RegisteredServer proxyServer);

    int getCoreServerAmount();

    ICoreServer initialCoreServer(String serverName, ServerConnectionInfo connectionInfo, CoreServerType coreServerType, int maxPlayerCount);

    void removeCoreServer(ICoreServer coreServer);

    ServerConnectionStatus getServerConnectionStatus(ServiceInfoSnapshot serviceInfoSnapshot);

    CoreServerType getCoreServerType(ServiceInfoSnapshot serviceInfoSnapshot);

    List<ICoreServer> getCoreServersByType(CoreServerType coreServerType);

    ICoreServer getFreeLobbyServer();
    boolean areLobbiesOnline();

    boolean isCoreServerOnline(ICoreServer coreServer);

}
