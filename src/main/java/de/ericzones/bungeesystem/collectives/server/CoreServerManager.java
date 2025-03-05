// Created by Eric B. 31.01.2021 19:35
package de.ericzones.bungeesystem.collectives.server;

import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.ext.bridge.BridgeServiceProperty;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.server.connection.ServerConnectionInfo;
import de.ericzones.bungeesystem.collectives.server.connection.ServerConnectionStatus;
import de.ericzones.bungeesystem.collectives.server.event.CoreServerChangeStatusEvent;
import de.ericzones.bungeesystem.collectives.server.event.CoreServerCreateEvent;
import de.ericzones.bungeesystem.collectives.server.event.CoreServerDeleteEvent;

import java.util.*;

public class CoreServerManager implements ICoreServerManager {

    private final BungeeSystem instance;
    private final List<ICoreServer> coreServers = new ArrayList<>();

    public CoreServerManager(BungeeSystem instance) {
        this.instance = instance;
        registerCurrentServers();
        CloudNetDriver.getInstance().getEventManager().registerListeners(new CoreServerCreateEvent(instance));
        CloudNetDriver.getInstance().getEventManager().registerListeners(new CoreServerDeleteEvent(instance));
        CloudNetDriver.getInstance().getEventManager().registerListeners(new CoreServerChangeStatusEvent(instance));
    }

    @Override
    public List<ICoreServer> getCoreServers() {
        return coreServers;
    }

    @Override
    public ICoreServer getCoreServer(ICorePlayer corePlayer) {
        return this.coreServers.stream().filter(ICoreServer -> ICoreServer.containsCorePlayer(corePlayer)).findFirst().orElse(null);
    }

    @Override
    public ICoreServer getCoreServer(String serverName) {
        return this.coreServers.stream().filter(ICoreServer -> ICoreServer.getServerName().equalsIgnoreCase(serverName)).findFirst().orElse(null);
    }

    @Override
    public ICoreServer getCoreServer(ServiceInfoSnapshot serviceInfoSnapshot) {
        return this.coreServers.stream().filter(ICoreServer -> ICoreServer.getServiceInfoSnapshot().equals(serviceInfoSnapshot)).findFirst().orElse(null);
    }

    @Override
    public ICoreServer getCoreServer(RegisteredServer proxyServer) {
        return this.coreServers.stream().filter(ICoreServer -> ICoreServer.getProxyServer().equals(proxyServer)).findFirst().orElse(null);
    }

    @Override
    public int getCoreServerAmount() {
        return this.coreServers.size();
    }

    @Override
    public ICoreServer initialCoreServer(String serverName, ServerConnectionInfo connectionInfo, CoreServerType coreServerType, int maxPlayerCount) {
        ICoreServer coreServer = new CoreServer(serverName, connectionInfo, coreServerType, maxPlayerCount);
        this.coreServers.add(coreServer);
        return coreServer;
    }

    @Override
    public void removeCoreServer(ICoreServer coreServer) {
        for(ICorePlayer current : coreServer.getCorePlayers()) {
            if(current.getConnectedCoreServer().equals(coreServer))
                current.setConnectedCoreServer(null);
        }
        coreServer.getCorePlayers().clear();
        this.coreServers.remove(coreServer);
    }

    @Override
    public ServerConnectionStatus getServerConnectionStatus(ServiceInfoSnapshot serviceInfoSnapshot) {
        ServerConnectionStatus serverConnectionStatus = ServerConnectionStatus.OFFLINE;
        Optional<Boolean> isOnline = serviceInfoSnapshot.getProperty(BridgeServiceProperty.IS_ONLINE);
        Optional<Boolean> isStarting = serviceInfoSnapshot.getProperty(BridgeServiceProperty.IS_STARTING);
        if(isStarting.isPresent() && isStarting.get())
            serverConnectionStatus = ServerConnectionStatus.STARTING;
        else if(isOnline.isPresent() && isOnline.get())
            serverConnectionStatus = ServerConnectionStatus.ONLINE;
        return serverConnectionStatus;
    }

    @Override
    public CoreServerType getCoreServerType(ServiceInfoSnapshot serviceInfoSnapshot) {
        CoreServerType coreServerType = CoreServerType.NONE;
        Optional<String> groupName = Arrays.stream(serviceInfoSnapshot.getConfiguration().getGroups()).findFirst();
        if(!groupName.isPresent())
            return coreServerType;
        switch (groupName.get().toLowerCase()) {
            case "lobby":
                coreServerType = CoreServerType.LOBBYSERVER;
                break;
            case "bedwars": case "ttt": case "starbattle": case "skywars":
                coreServerType = CoreServerType.GAMESERVER;
                break;
            case "ryansagt":
                coreServerType = CoreServerType.PRIVATESERVER;
                break;
            case "build":
                coreServerType = CoreServerType.BUILDSERVER;
                break;
            default:
                break;
        }
        return coreServerType;
    }

    @Override
    public List<ICoreServer> getCoreServersByType(CoreServerType coreServerType) {
        List<ICoreServer> coreServerList = new ArrayList<>();
        for(ICoreServer current : this.coreServers) {
            if(current.getServerType() == coreServerType)
                coreServerList.add(current);
        }
        return coreServerList;
    }

    @Override
    public ICoreServer getFreeLobbyServer() {
        ICoreServer coreServer = null;
        List<ICoreServer> lobbyServers = getCoreServersByType(CoreServerType.LOBBYSERVER);
        for(ICoreServer current : lobbyServers) {
            if(coreServer == null || current.getCorePlayerCount() < coreServer.getCorePlayerCount())
                coreServer = current;
        }
        return coreServer;
    }

    @Override
    public boolean areLobbiesOnline() {
        boolean lobbiesOnline = false;
        List<ICoreServer> lobbyServers = getCoreServersByType(CoreServerType.LOBBYSERVER);
        if(lobbyServers.size() != 0)
            lobbiesOnline = true;
        return lobbiesOnline;
    }

    @Override
    public boolean isCoreServerOnline(ICoreServer coreServer) {
        return this.coreServers.contains(coreServer);
    }

    private void registerCurrentServers() {
        Collection<ServiceInfoSnapshot> servers = CloudNetDriver.getInstance().getCloudServiceProvider().getCloudServices();
        for(ServiceInfoSnapshot current : servers) {
            if(current.getConfiguration().getProcessConfig().getEnvironment().isMinecraftProxy() || current.getConfiguration().getProcessConfig().getEnvironment().isMinecraftJavaProxy()) continue;
            String serverName = current.getName();

            int port = current.getAddress().getPort();
            ServerConnectionStatus serverConnectionStatus = getServerConnectionStatus(current);
            String version = "";
            Optional<String> versionCache = current.getProperty(BridgeServiceProperty.VERSION);
            if(versionCache.isPresent())
                version = versionCache.get();
            ServerConnectionInfo connectionInfo = new ServerConnectionInfo(port, serverConnectionStatus, version);
            int maxPlayerCount = 0;
            if(current.getProperty(BridgeServiceProperty.MAX_PLAYERS).isPresent())
                maxPlayerCount = current.getProperty(BridgeServiceProperty.MAX_PLAYERS).get();

            initialCoreServer(serverName, connectionInfo, getCoreServerType(current), maxPlayerCount);
        }
    }

}
