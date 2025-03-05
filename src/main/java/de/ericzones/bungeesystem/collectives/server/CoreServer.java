// Created by Eric B. 31.01.2021 15:04
package de.ericzones.bungeesystem.collectives.server;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.server.connection.ServerConnectionInfo;
import de.ericzones.bungeesystem.collectives.server.whitelist.Whitelist;
import net.kyori.adventure.text.TextComponent;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class CoreServer implements ICoreServer {

    private String serverName;
    private int corePlayerCount;
    private int maxPlayerCount;
    private List<UUID> corePlayers;
    private ServerConnectionInfo connectionInfo;
    private CoreServerType serverType;
    private Whitelist whitelist;

    public CoreServer(String serverName, ServerConnectionInfo connectionInfo, CoreServerType coreServerType, int maxPlayerCount) {
        this.serverName = serverName;
        this.connectionInfo = connectionInfo;
        this.serverType = coreServerType;
        this.corePlayerCount = 0;
        this.maxPlayerCount = maxPlayerCount;
        corePlayers = new ArrayList<>();
        whitelist = new Whitelist(Whitelist.WhitelistType.NONE);
    }

    @Override
    public ICorePlayer getCorePlayer(UUID uuid) {
        if(!corePlayers.contains(uuid)) return null;
        return BungeeSystem.getInstance().getCorePlayerManager().getCorePlayer(uuid);
    }

    @Override
    public ICorePlayer getCorePlayer(String username) {
        ICorePlayer corePlayer = BungeeSystem.getInstance().getCorePlayerManager().getCorePlayer(username);
        if(corePlayer == null) return null;
        if(!corePlayers.contains(corePlayer.getUniqueID())) return null;
        return corePlayer;
    }

    @Override
    public void addCorePlayer(ICorePlayer corePlayer) {
        if(!corePlayers.contains(corePlayer.getUniqueID())) {
            corePlayers.add(corePlayer.getUniqueID());
            corePlayerCount++;
            corePlayer.setConnectedCoreServer(this);
        }
    }

    @Override
    public void removeCorePlayer(ICorePlayer corePlayer) {
        if(corePlayers.contains(corePlayer.getUniqueID())) {
            corePlayers.remove(corePlayer.getUniqueID());
            corePlayerCount--;
        }
    }

    @Override
    public boolean containsCorePlayer(ICorePlayer corePlayer) {
        return corePlayers.contains(corePlayer);
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public int getCorePlayerCount() {
        return corePlayerCount;
    }

    @Override
    public List<ICorePlayer> getCorePlayers() {
        List<ICorePlayer> coreplayers = new ArrayList<>();
        for(UUID current : this.corePlayers) {
            ICorePlayer corePlayer = BungeeSystem.getInstance().getCorePlayerManager().getCorePlayer(current);
            if(corePlayer != null) coreplayers.add(corePlayer);
        }
        return coreplayers;
    }

    @Override
    public RegisteredServer getProxyServer() {
        RegisteredServer registeredServer = null;
        if(BungeeSystem.getInstance().getProxyServer().matchServer(serverName).stream().findFirst().isPresent())
            registeredServer = BungeeSystem.getInstance().getProxyServer().matchServer(serverName).stream().findFirst().get();
        return registeredServer;
    }

    @Override
    public ServiceInfoSnapshot getServiceInfoSnapshot() {
        return CloudNetDriver.getInstance().getCloudServiceProvider().getCloudServiceByName(serverName);
    }

    @Override
    public ServerConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    @Override
    public CoreServerType getServerType() {
        return serverType;
    }

    @Override
    public int getMaxPlayerCount() {
        return this.maxPlayerCount;
    }

    @Override
    public void setMaxPlayerCount(int maxPlayerCount) {
        ByteArrayDataOutput dataOutput = ByteStreams.newDataOutput();
        dataOutput.writeUTF("setMaxPlayerCount");
        dataOutput.writeUTF(String.valueOf(maxPlayerCount));
        getProxyServer().sendPluginMessage(new LegacyChannelIdentifier("Core"), dataOutput.toByteArray());

        this.maxPlayerCount = maxPlayerCount;
    }

    @Override
    public boolean isFull() {
        return getCorePlayerCount() >= getMaxPlayerCount();
    }

    @Override
    public ICorePlayer getLowerJoinPowerPlayer(int joinPower) {
        ICorePlayer corePlayer = null;
        List<ICorePlayer> corePlayerList = getCorePlayers();
        Collections.shuffle(corePlayerList);
        for(ICorePlayer current : corePlayerList) {
            if(current.canJoinFullServer()) continue;
            corePlayer = current;
            break;
        }
        if(corePlayer != null)
            return corePlayer;
        for(ICorePlayer current : corePlayerList) {
            if(current.canJoinFullServer() && current.getFullServerJoinPower() >= joinPower) continue;
            corePlayer = current;
            break;
        }
        return corePlayer;
    }

    @Override
    public void stopServer(String reason) {
        if(corePlayerCount != 0) {
            BungeeSystem.getInstance().getPluginDataManager().sendPluginData(getProxyServer(), "server", "stop");
            for (ICorePlayer current : getCorePlayers()) {
                if (reason != null)
                    current.sendMessage(reason);
                current.sendToLobby();
            }
        } else
            getServiceInfoSnapshot().provider().stop();
    }

    @Override
    public void kickAllPlayer(String reason, List<ICorePlayer> ignorePlayers) {
        List<ICorePlayer> corePlayerList = getCorePlayers();
        if(ignorePlayers.size() != 0)
            corePlayerList.removeAll(ignorePlayers);
        for(ICorePlayer current : corePlayerList) {
            if(reason != null)
                current.sendMessage(reason);
            current.sendToLobby();
        }
    }

    @Override
    public boolean isRestricted() {
        return this.whitelist.getWhitelistType() == Whitelist.WhitelistType.RESTRICTED;
    }

    @Override
    public boolean isInMaintenance() {
        return this.whitelist.getWhitelistType() == Whitelist.WhitelistType.MAINTENANCE;
    }

    @Override
    public void setRestricted(boolean restricted) {
        if(restricted)
            this.whitelist = new Whitelist(Whitelist.WhitelistType.RESTRICTED);
        else {
            if(isRestricted())
                this.whitelist = new Whitelist(Whitelist.WhitelistType.NONE);
        }
    }

    @Override
    public void setMaintenance(boolean maintenance) {
        if(maintenance)
            this.whitelist = new Whitelist(Whitelist.WhitelistType.MAINTENANCE);
        else {
            if(isInMaintenance())
                this.whitelist = new Whitelist(Whitelist.WhitelistType.NONE);
        }
    }

    @Override
    public Whitelist getWhitelist() {
        return this.whitelist;
    }

    @Override
    public boolean isPlaytimeEnabled() {
        if(serverType == CoreServerType.LOBBYSERVER || serverType == CoreServerType.NONE)
            return false;
        return true;
    }

    @Override
    public void broadcastMessage(String message) {
        for(ICorePlayer current : getCorePlayers())
            current.sendMessage(message);
    }

    @Override
    public void broadcastMessage(TextComponent message) {
        for(ICorePlayer current : getCorePlayers())
            current.sendMessage(message);
    }

}
