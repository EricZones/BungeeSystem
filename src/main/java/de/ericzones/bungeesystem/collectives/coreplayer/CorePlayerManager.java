// Created by Eric B. 30.01.2021 18:53
package de.ericzones.bungeesystem.collectives.coreplayer;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.proxy.Player;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.ext.bridge.player.ICloudPlayer;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.connection.PlayerConnectionInfo;
import de.ericzones.bungeesystem.collectives.coreplayer.event.CorePlayerDisconnectEvent;
import de.ericzones.bungeesystem.collectives.coreplayer.event.CorePlayerSwitchServerEvent;
import de.ericzones.bungeesystem.collectives.database.ISqlAdapter;
import de.ericzones.bungeesystem.collectives.server.ICoreServer;
import de.ericzones.bungeesystem.collectives.coreplayer.connection.PlayerVersion;
import de.ericzones.bungeesystem.global.language.Language;

import java.net.InetAddress;
import java.util.*;

public class CorePlayerManager extends SqlCorePlayer implements ICorePlayerManager {

    private final BungeeSystem instance;
    private final List<ICorePlayer> corePlayers = new ArrayList<>();

    public CorePlayerManager(BungeeSystem instance, ISqlAdapter sqlAdapter) {
        super(sqlAdapter); this.instance = instance;
        EventManager eventManager = instance.getProxyServer().getEventManager();
        eventManager.register(instance, new CorePlayerDisconnectEvent(instance, sqlAdapter));
        eventManager.register(instance, new CorePlayerSwitchServerEvent(instance, sqlAdapter));
    }

    @Override
    public List<ICorePlayer> getCorePlayers() {
        return this.corePlayers;
    }

    @Override
    public ICorePlayer getCorePlayer(UUID uuid) {
        return this.corePlayers.stream().filter(ICorePlayer -> ICorePlayer.getUniqueID().equals(uuid)).findFirst().orElse(null);
    }

    @Override
    public ICorePlayer getCorePlayer(String username) {
        return this.corePlayers.stream().filter(ICorePlayer -> ICorePlayer.getUsername().equalsIgnoreCase(username)).findFirst().orElse(null);
    }

    @Override
    public ICorePlayer getCorePlayer(Player proxyPlayer) {
        return this.corePlayers.stream().filter(ICorePlayer -> ICorePlayer.getProxyPlayer().equals(proxyPlayer)).findFirst().orElse(null);
    }

    @Override
    public ICorePlayer getCorePlayer(ICloudPlayer cloudPlayer) {
        return this.corePlayers.stream().filter(ICorePlayer -> ICorePlayer.getCloudPlayer().equals(cloudPlayer)).findFirst().orElse(null);
    }

    @Override
    public ICorePlayer getCorePlayer(InetAddress ipAddress) {
        return this.corePlayers.stream().filter(ICorePlayer -> ICorePlayer.getConnectionInfo().getIpAddress().equals(ipAddress.getHostAddress())).findFirst().orElse(null);
    }

    @Override
    public IOfflineCorePlayer getOfflineCorePlayer(UUID uuid) {
        if(uuid == null) return null;
        if(!corePlayerExists(uuid))
            return null;

        return new OfflineCorePlayer(getCorePlayerInformation(uuid, 1), getCorePlayerIpAddress(uuid), uuid, getCorePlayerInformation(uuid, 3),
                getCorePlayerLogoutMillis(uuid), getCorePlayerCreationTimeMillis(uuid), getCorePlayerPreviousPlaytime(uuid), getCorePlayerCoins(uuid),
                getCorePlayerVersion(uuid), getCorePlayerLanguage(uuid));
    }

    @Override
    public IOfflineCorePlayer getOfflineCorePlayer(String username) {
        return getOfflineCorePlayer(getCorePlayerUUID(username));
    }

    @Override
    public IOfflineCorePlayer getOfflineCorePlayer(InetAddress ipAddress) {
        return getOfflineCorePlayer(getCorePlayerUUID(ipAddress));
    }

    @Override
    public ICorePlayer initialCorePlayer(UUID uuid, String username, String skinValue, PlayerConnectionInfo connectionInfo, ICoreServer coreServer) {
        if(!corePlayerExists(uuid))
            createCorePlayer(uuid, username, connectionInfo.getIpAddress(), skinValue, connectionInfo.getVersion().getVersionName());
        else
            updateCorePlayer(uuid, username, connectionInfo.getIpAddress(), skinValue, connectionInfo.getVersion().getVersionName());

        ICorePlayer corePlayer = new CorePlayer(username, uuid, connectionInfo, skinValue, getCorePlayerCreationTimeMillis(uuid), getCorePlayerPreviousPlaytime(uuid),
                getCorePlayerCoins(uuid), getCorePlayerLanguage(uuid));
        corePlayer.setConnectedCoreServer(coreServer);
        coreServer.addCorePlayer(corePlayer);
        this.corePlayers.add(corePlayer);
        return corePlayer;
    }

    @Override
    public void removeCorePlayer(ICorePlayer corePlayer) {
        if(corePlayer == null)
            return;
        corePlayer.getConnectedCoreServer().removeCorePlayer(corePlayer);
        this.corePlayers.remove(corePlayer);
        updateDisconnectedCorePlayer(corePlayer.getUniqueID(), corePlayer.getPlaytime().getTotalPlaytime(), corePlayer.getCoins().getCurrentCoins());
    }

    @Override
    public void serverSwitchCorePlayer(ICorePlayer corePlayer, ICoreServer coreServer) {
        corePlayer.getConnectedCoreServer().removeCorePlayer(corePlayer);
        corePlayer.setConnectedCoreServer(coreServer);
        coreServer.addCorePlayer(corePlayer);
    }

    @Override
    public void resetOfflineCorePlayerPlaytime(IOfflineCorePlayer offlineCorePlayer) {
        resetCorePlayerPlaytime(offlineCorePlayer.getUniqueID());
    }

    @Override
    public void updateOfflineCorePlayerCoins(IOfflineCorePlayer offlineCorePlayer) {
        updateCorePlayerCoins(offlineCorePlayer.getUniqueID(), offlineCorePlayer.getCoins());
    }

    @Override
    public int getOnlinePlayerAmount() {
        return this.corePlayers.size();
    }

    @Override
    public PlayerConnectionInfo createConnectionInfo(UUID uuid, String ipAddress) {
        ICloudPlayer cloudPlayer = CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class).getOnlinePlayer(uuid);
        PlayerVersion version = PlayerVersion.getPlayerVersionById(cloudPlayer.getNetworkConnectionInfo().getVersion());
        int port = cloudPlayer.getNetworkConnectionInfo().getAddress().getPort();
        boolean legacy = cloudPlayer.getNetworkConnectionInfo().isLegacy();
        return new PlayerConnectionInfo(uuid, version, ipAddress, port, legacy);
    }

    @Override
    public void updateCorePlayerLanguage(ICorePlayer corePlayer, Language language) {
        corePlayer.setLanguage(language);
        updateCorePlayerLanguage(corePlayer.getUniqueID(), language.toString());
    }

    @Override
    public List<IOfflineCorePlayer> getTopPlaytimePlayers(int amount) {
        List<IOfflineCorePlayer> topPlaytimePlayers = new ArrayList<>();
        List<UUID> topPlaytimePlayersRaw = loadTopPlaytimePlayers(amount);
        for(UUID current : topPlaytimePlayersRaw)
            topPlaytimePlayers.add(getOfflineCorePlayer(current));
        return topPlaytimePlayers;
    }

    private String getCorePlayerInformation(UUID uuid, int id) {
        return String.valueOf(getTableInformation(uuid, id));
    }

}