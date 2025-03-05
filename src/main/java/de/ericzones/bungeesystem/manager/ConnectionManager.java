// Created by Eric B. 14.02.2021 12:28
package de.ericzones.bungeesystem.manager;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.connection.PlayerVersion;
import de.ericzones.bungeesystem.collectives.server.ICoreServer;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.disconnectmessage.DisconnectMessageHandler;
import de.ericzones.bungeesystem.global.messaging.disconnectmessage.DisconnectMessageType;
import de.ericzones.bungeesystem.global.permission.PermissionHandler;
import de.ericzones.bungeesystem.global.permission.PermissionType;
import de.ericzones.bungeesystem.manager.connection.Firewall;
import net.kyori.adventure.text.Component;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class ConnectionManager {

    private final BungeeSystem instance;
    private final DisconnectMessageHandler disconnectMessageHandler;
    private final PermissionHandler permissionHandler;
    private final LanguageHandler languageHandler;

    private final Map<String, Long> connectionDelayCache = new HashMap<>();

    public ConnectionManager(BungeeSystem instance) {
        this.instance = instance;
        this.disconnectMessageHandler = instance.getDisconnectMessageHandler();
        this.languageHandler = instance.getLanguageHandler();
        this.permissionHandler = instance.getPermissionHandler();
        EventManager eventManager = instance.getProxyServer().getEventManager();
        eventManager.register(instance, this);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerPreLogin(PreLoginEvent e) {
        InetAddress ipAddress = e.getConnection().getRemoteAddress().getAddress();

        //checkFirewall(e.getConnection().getRemoteAddress());

        IOfflineCorePlayer offlineCorePlayer = instance.getCorePlayerManager().getOfflineCorePlayer(e.getUsername());
        if(offlineCorePlayer == null) {
            Language language = Language.ENGLISH;
            if(isAlreadyConnected(ipAddress)) {
                e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.REJECTED,
                        languageHandler.getTranslatedMessage(Message.CONNECT_ALREADYCONNECTED, language), "", language))));
                return;
            }
            if(connectedTooFast(ipAddress)) {
                e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.REJECTED,
                        languageHandler.getTranslatedMessage(Message.CONNECT_TOOFASTCONNECTED, language), "", language))));
                return;
            }
            if(usedDisallowedVersion(e.getConnection().getProtocolVersion().getProtocol())) {
                e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.REJECTED,
                        languageHandler.getTranslatedMessage(Message.CONNECT_DISALLOWEDVERSION, language), "", language))));
                return;
            }
            if(!instance.getCoreServerManager().areLobbiesOnline()) {
                e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.REJECTED,
                        languageHandler.getTranslatedMessage(Message.LOBBIES_NOT_REACHABLE, language), "", language))));
                return;
            }
            ICoreServer coreServer = instance.getCoreServerManager().getFreeLobbyServer();
            if(coreServer.isFull()) {
                e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.LOBBIESFULL,
                        languageHandler.getTranslatedMessage(Message.LOBBIES_FULL_PREMIUMNEEDED, language), "", language))));
                return;
            }
            return;
        }
        Language language = offlineCorePlayer.getLanguage();

        if(isAlreadyConnected(ipAddress) || offlineCorePlayer.isOnline()) {
            e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.REJECTED,
                    languageHandler.getTranslatedMessage(Message.CONNECT_ALREADYCONNECTED, language), "", language))));
            return;
        }
        if(connectedTooFast(ipAddress)) {
            e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.REJECTED,
                    languageHandler.getTranslatedMessage(Message.CONNECT_TOOFASTCONNECTED, language), "", language))));
            return;
        }
        if(usedDisallowedVersion(e.getConnection().getProtocolVersion().getProtocol())) {
            e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.REJECTED,
                    languageHandler.getTranslatedMessage(Message.CONNECT_DISALLOWEDVERSION, language), "", language))));
            return;
        }
        if(!instance.getCoreServerManager().areLobbiesOnline()) {
            e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.REJECTED,
                    languageHandler.getTranslatedMessage(Message.LOBBIES_NOT_REACHABLE, language), "", language))));
            return;
        }
        ICoreServer coreServer = instance.getCoreServerManager().getFreeLobbyServer();
        if(coreServer.isFull() && !offlineCorePlayer.canJoinFullServer()) {
            e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.LOBBIESFULL,
                    languageHandler.getTranslatedMessage(Message.LOBBIES_FULL_PREMIUMNEEDED, language), "", language))));
            return;
        }
        if(coreServer.isFull() && offlineCorePlayer.canJoinFullServer()) {
            ICorePlayer corePlayer = coreServer.getLowerJoinPowerPlayer(offlineCorePlayer.getFullServerJoinPower());
            if(corePlayer == null) {
                e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.LOBBIESFULL,
                        languageHandler.getTranslatedMessage(Message.LOBBIES_FULL_NOBODYKICKED, language), "", language))));
                return;
            }
            corePlayer.disconnect(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.DISCONNECTED,
                    languageHandler.getTranslatedMessage(Message.LOBBIES_FULL_KICKEDBYPREMIUM, language), "", language));
        }
    }

//    private void checkFirewall(InetSocketAddress socketAddress) {
//        instance.getProxyServer().getScheduler().buildTask(instance, new Runnable() {
//            @Override
//            public void run() {
//                if(!Firewall.isAllowed(socketAddress))
//                    Firewall.addToBlacklist(socketAddress.getHostName());
//                else if(Firewall.isProxy(socketAddress.getHostName()))
//                    Firewall.addToBlacklist(socketAddress.getHostName());
//            }
//        }).schedule();
//    }

    private boolean usedDisallowedVersion(int versionNumber) {
        PlayerVersion version = PlayerVersion.getPlayerVersionById(versionNumber);
        return version == null;
    }

    private boolean isAlreadyConnected(InetAddress ipAddress) {
        ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(ipAddress);
        if(corePlayer == null) return false;
        if(corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.UNLIMITEDCONNECTIONS))) return false;
        return true;
    }

    private boolean connectedTooFast(InetAddress ipAddress) {
        Map<String, Long> connections = new HashMap<>(this.connectionDelayCache);
        for(String current : connections.keySet()) {
            if (this.connectionDelayCache.get(current) < System.currentTimeMillis()) this.connectionDelayCache.remove(current);
        }

        if(!this.connectionDelayCache.containsKey(ipAddress.getHostAddress())) {
            this.connectionDelayCache.put(ipAddress.getHostAddress(), System.currentTimeMillis()+10*1000);
            return false;
        }
        IOfflineCorePlayer offlineCorePlayer = instance.getCorePlayerManager().getOfflineCorePlayer(ipAddress);
        if(offlineCorePlayer == null) return true;
        if(offlineCorePlayer.hasPermission(permissionHandler.getPermission(PermissionType.NOCONNECTIONDELAY))) return false;
        return true;
    }

}
