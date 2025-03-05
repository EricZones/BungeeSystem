// Created by Eric B. 30.01.2021 21:13
package de.ericzones.bungeesystem.collectives.coreplayer;

import com.velocitypowered.api.proxy.Player;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.permission.IPermissionUser;
import de.dytanic.cloudnet.ext.bridge.player.ICloudPlayer;
import de.dytanic.cloudnet.ext.bridge.player.IPlayerManager;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.collectible.Coins;
import de.ericzones.bungeesystem.collectives.coreplayer.connection.PlayerConnectionInfo;
import de.ericzones.bungeesystem.collectives.coreplayer.collectible.Playtime;
import de.ericzones.bungeesystem.collectives.plugindata.object.DataCorePlayer;
import de.ericzones.bungeesystem.collectives.server.ICoreServer;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.chatmessage.ChatMessageType;
import de.ericzones.bungeesystem.global.messaging.disconnectmessage.DisconnectMessageType;
import de.ericzones.bungeesystem.global.permission.PermissionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.text.SimpleDateFormat;
import java.util.*;

public class CorePlayer implements ICorePlayer {

    private String username, skinValue;
    private UUID uuid;
    private String connectedCoreServer;
    private PlayerConnectionInfo connectionInfo;
    private Long creationTimeMillis;
    private Playtime playtime;
    private Coins coins;
    private Language language;

    public CorePlayer(String username, UUID uuid, PlayerConnectionInfo connectionInfo, String skinValue, Long creationTimeMillis, Long previousPlaytime, int previousCoins, String language) {
        this.username = username;
        this.skinValue = skinValue;
        this.uuid = uuid;
        this.connectionInfo = connectionInfo;
        this.creationTimeMillis = creationTimeMillis;
        this.playtime = new Playtime(previousPlaytime);
        this.coins = new Coins(previousCoins);
        if(!language.equals("null"))
            this.language = Language.valueOf(language);
        else
            this.language = null;
    }

    @Override
    public ICoreServer getConnectedCoreServer() {
        return BungeeSystem.getInstance().getCoreServerManager().getCoreServer(this.connectedCoreServer);
    }

    @Override
    public void setConnectedCoreServer(ICoreServer coreServer) {
        if(coreServer != null)
            this.connectedCoreServer = coreServer.getServerName();
        else
            this.connectedCoreServer = null;
    }

    @Override
    public ICloudPlayer getCloudPlayer() {
        return CloudNetDriver.getInstance().getServicesRegistry().getFirstService(IPlayerManager.class).getOnlinePlayer(uuid);
    }

    @Override
    public Player getProxyPlayer() {
        Player player = null;
        if(BungeeSystem.getInstance().getProxyServer().getPlayer(uuid).isPresent())
            player = BungeeSystem.getInstance().getProxyServer().getPlayer(uuid).get();
        return player;
    }

    @Override
    public String getUsername() {
        return this.username;
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
    public PlayerConnectionInfo getConnectionInfo() {
        return connectionInfo;
    }

    @Override
    public boolean hasPermission(String permission) {
        return CloudNetDriver.getInstance().getPermissionManagement().hasPermission(getPermissionUser(), permission);
    }

    @Override
    public void sendMessage(String message) {
        getProxyPlayer().sendMessage(Component.text(message));
    }

    @Override
    public void sendMessage(TextComponent textComponent) {
        getProxyPlayer().sendMessage(textComponent);
    }

    @Override
    public void disconnect(String reason) {
        getProxyPlayer().disconnect(Component.text(reason));
    }

    @Override
    public CorePlayerSwitchResult sendToServer(ICoreServer coreServer) {
        if(getConnectedCoreServer().equals(coreServer))
            return CorePlayerSwitchResult.ALREADYCONNECTED;
        if(coreServer.isInMaintenance() && !hasPermission(BungeeSystem.getInstance().getPermissionHandler().getPermission(PermissionType.MAINTENANCESERVERJOIN)))
            return CorePlayerSwitchResult.SERVERMAINTENANCE;
        if(coreServer.isRestricted() && !coreServer.getWhitelist().containsUUID(uuid) && !hasPermission(BungeeSystem.getInstance().getPermissionHandler().getPermission(PermissionType.RESTRICTEDSERVERJOIN)))
            return CorePlayerSwitchResult.SERVERRESTRICTED;

        if(coreServer.isFull() && !canJoinFullServer())
            return CorePlayerSwitchResult.SERVERFULL;
        else if(coreServer.isFull() && canJoinFullServer()) {
            ICorePlayer corePlayer = coreServer.getLowerJoinPowerPlayer(getFullServerJoinPower());
            if(corePlayer == null)
                return CorePlayerSwitchResult.NOPLAYERKICKED;
            else {
                corePlayer.sendMessage(BungeeSystem.getInstance().getChatMessageHandler().getChatMessage(ChatMessageType.KICKEDBYPREMIUM, getLanguage()));
                corePlayer.sendToLobby();
            }
        }

        getProxyPlayer().createConnectionRequest(coreServer.getProxyServer()).connect();
        return CorePlayerSwitchResult.SUCCESS;
    }

    @Override
    public CorePlayerSwitchResult sendToServerAsAdmin(ICoreServer coreServer) {
        if(getConnectedCoreServer().equals(coreServer))
            return CorePlayerSwitchResult.ALREADYCONNECTED;
        if(coreServer.isFull()) {
            ICorePlayer corePlayer = coreServer.getLowerJoinPowerPlayer(4);
            if(corePlayer == null)
                return CorePlayerSwitchResult.NOPLAYERKICKED;
            else {
                corePlayer.sendMessage(BungeeSystem.getInstance().getChatMessageHandler().getChatMessage(ChatMessageType.KICKEDBYPREMIUM, getLanguage()));
                corePlayer.sendToLobby();
            }
        }
        getProxyPlayer().createConnectionRequest(coreServer.getProxyServer()).connect();
        return CorePlayerSwitchResult.SUCCESS;
    }

    @Override
    public void sendToLobby() {
        LanguageHandler languageHandler = BungeeSystem.getInstance().getLanguageHandler();
        if(!BungeeSystem.getInstance().getCoreServerManager().areLobbiesOnline()) {
            disconnect(BungeeSystem.getInstance().getDisconnectMessageHandler().getDisconnectMessage(DisconnectMessageType.DISCONNECTED,
                    languageHandler.getTranslatedMessage(Message.LOBBIES_NOT_REACHABLE, getLanguage()), "", getLanguage()));
            return;
        }
        ICoreServer coreServer = BungeeSystem.getInstance().getCoreServerManager().getFreeLobbyServer();
        if(coreServer.isFull() && !canJoinFullServer()) {
            disconnect(BungeeSystem.getInstance().getDisconnectMessageHandler().getDisconnectMessage(DisconnectMessageType.LOBBIESFULL,
                    languageHandler.getTranslatedMessage(Message.LOBBIES_FULL_PREMIUMNEEDED, getLanguage()), "", getLanguage()));
            return;
        }
        if(coreServer.isFull() && canJoinFullServer()) {
            ICorePlayer corePlayer = coreServer.getLowerJoinPowerPlayer(getFullServerJoinPower());
            if(corePlayer == null) {
                disconnect(BungeeSystem.getInstance().getDisconnectMessageHandler().getDisconnectMessage(DisconnectMessageType.LOBBIESFULL,
                        languageHandler.getTranslatedMessage(Message.LOBBIES_FULL_NOBODYKICKED, getLanguage()),"", getLanguage()));
                return;
            } else
                corePlayer.disconnect(BungeeSystem.getInstance().getDisconnectMessageHandler().getDisconnectMessage(DisconnectMessageType.DISCONNECTED,
                        languageHandler.getTranslatedMessage(Message.LOBBIES_FULL_KICKEDBYPREMIUM, getLanguage()), "", getLanguage()));
        }
        getProxyPlayer().createConnectionRequest(coreServer.getProxyServer()).connect();
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
    public Playtime getPlaytime() {
        return this.playtime;
    }

    @Override
    public void resetPlaytime() {
        this.playtime = new Playtime(0L);
        if(getConnectedCoreServer().isPlaytimeEnabled())
            getPlaytime().setActive();
        else
            getPlaytime().setIdle();
    }

    @Override
    public Coins getCoins() {
        return this.coins;
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

    @Override
    public String getPing() {
        String result;
        int ping = (int) getProxyPlayer().getPing();
        if(ping >= 100)
            result = "§c"+ping+"ms";
        else if(ping >= 50)
            result = "§6"+ping+"ms";
        else
            result = "§a"+ping+"ms";
        return result;
    }

    @Override
    public DataCorePlayer getDataCorePlayer() {
        return new DataCorePlayer(this.username, this.uuid, this.connectionInfo.getIpAddress(), this.connectionInfo.getVersion(), this.skinValue, this.creationTimeMillis, this.playtime.getTotalPlaytime(), this.coins.getCurrentCoins(), this.language, this.connectedCoreServer, getRankName(), getRankPrefix(), getPing());
    }

}
