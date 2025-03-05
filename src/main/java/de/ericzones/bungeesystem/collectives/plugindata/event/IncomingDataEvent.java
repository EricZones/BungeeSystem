// Created by Eric B. 14.05.2021 13:45
package de.ericzones.bungeesystem.collectives.plugindata.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteArrayDataInput;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.friend.FriendPlayer;
import de.ericzones.bungeesystem.collectives.friend.FriendProperty;
import de.ericzones.bungeesystem.collectives.friend.IFriendManager;
import de.ericzones.bungeesystem.collectives.plugindata.PluginDataManager;
import de.ericzones.bungeesystem.collectives.plugindata.object.DataCorePlayer;
import de.ericzones.bungeesystem.collectives.plugindata.object.DataFriendPlayer;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.messaging.disconnectmessage.DisconnectMessageType;

import java.util.UUID;

public class IncomingDataEvent {

    private final BungeeSystem instance;

    public IncomingDataEvent(BungeeSystem instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onIncomingData(PluginMessageEvent e) throws JsonProcessingException {
        if(!e.getIdentifier().getId().equalsIgnoreCase("Bungeesystem")) return;
        if(!(e.getSource() instanceof ServerConnection)) return;
        Player player = ((ServerConnection) e.getSource()).getPlayer();
        PluginDataManager pluginDataManager = instance.getPluginDataManager();
        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        ICorePlayer corePlayer = corePlayerManager.getCorePlayer(player.getUniqueId());
        ByteArrayDataInput data = e.dataAsDataStream();
        String channel = data.readUTF();

        if(channel.equalsIgnoreCase("coreplayer")) {
            String method = data.readUTF();

            if(method.equalsIgnoreCase("get")) {
                DataCorePlayer dataCorePlayer = corePlayer.getDataCorePlayer();
                pluginDataManager.sendPluginData(corePlayer.getConnectedCoreServer().getProxyServer(), "coreplayer", "get", serializeObject(dataCorePlayer));
            } else if(method.equalsIgnoreCase("removeCoins"))
                corePlayer.getCoins().removeCoins(data.readInt());
            else if(method.equalsIgnoreCase("setCoins"))
                corePlayer.getCoins().setCoins(data.readInt());
            else if(method.equalsIgnoreCase("addCoins"))
                corePlayer.getCoins().addCoins(data.readInt());
            else if(method.equalsIgnoreCase("resetCoins"))
                corePlayer.getCoins().resetCoins();
            else if(method.equalsIgnoreCase("setLanguage"))
                corePlayer.setLanguage(Language.valueOf(data.readUTF()));
            else if(method.equalsIgnoreCase("disconnect"))
                corePlayer.disconnect(instance.getDisconnectMessageHandler().getDisconnectMessage(DisconnectMessageType.DISCONNECTED, data.readUTF(), "", corePlayer.getLanguage()));

        } else if(channel.equalsIgnoreCase("friend")) {
            IFriendManager friendManager = instance.getFriendManager();
            FriendPlayer friendPlayer = friendManager.getFriendPlayer(player.getUniqueId());
            String method = data.readUTF();

            if(method.equalsIgnoreCase("get")) {
                DataFriendPlayer dataFriendPlayer = friendPlayer.getDataFriendPlayer();
                pluginDataManager.sendPluginData(corePlayer.getConnectedCoreServer().getProxyServer(), "friend", "get", serializeObject(dataFriendPlayer));
            } else if(method.equalsIgnoreCase("acceptRequest"))
                friendManager.acceptFriendRequest(friendPlayer, friendManager.getFriendPlayer(UUID.fromString(data.readUTF())));
            else if(method.equalsIgnoreCase("denyRequest"))
                friendManager.denyFriendRequest(friendPlayer, friendManager.getFriendPlayer(UUID.fromString(data.readUTF())));
            else if(method.equalsIgnoreCase("acceptAllRequests"))
                friendManager.acceptAllFriendRequests(friendPlayer);
            else if(method.equalsIgnoreCase("denyAllRequests"))
                friendManager.denyAllFriendRequests(friendPlayer);
            else if(method.equalsIgnoreCase("removeFriend"))
                friendManager.removeFriend(friendPlayer, friendManager.getFriendPlayer(UUID.fromString(data.readUTF())));
            else if(method.equalsIgnoreCase("removeAllFriends"))
                friendManager.removeAllFriends(friendPlayer);
            else if(method.equalsIgnoreCase("setProperty"))
                friendPlayer.setProperty(FriendProperty.valueOf(data.readUTF()), FriendProperty.Setting.valueOf(data.readUTF()));

        } else if(channel.equalsIgnoreCase("joinme")) {

        } else if(channel.equalsIgnoreCase("party")) {

        } else if(channel.equalsIgnoreCase("punish")) {

        } else if(channel.equalsIgnoreCase("report")) {

        } else if(channel.equalsIgnoreCase("server")) {

        }
    }

    private String serializeObject(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

}
