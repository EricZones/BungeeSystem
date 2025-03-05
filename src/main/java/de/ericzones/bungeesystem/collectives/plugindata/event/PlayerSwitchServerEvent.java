// Created by Eric B. 20.05.2021 17:10
package de.ericzones.bungeesystem.collectives.plugindata.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.friend.IFriendManager;
import de.ericzones.bungeesystem.collectives.plugindata.PluginDataManager;
import de.ericzones.bungeesystem.collectives.plugindata.object.DataCorePlayer;
import de.ericzones.bungeesystem.collectives.plugindata.object.DataFriendPlayer;

import java.util.Optional;

public class PlayerSwitchServerEvent {

    private final BungeeSystem instance;

    public PlayerSwitchServerEvent(BungeeSystem instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onPlayerSwitchServer(ServerPreConnectEvent e) throws JsonProcessingException {
        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        PluginDataManager pluginDataManager = instance.getPluginDataManager();
        Optional<RegisteredServer> server = e.getResult().getServer();
        if(corePlayerManager.getCorePlayer(e.getPlayer().getUniqueId()) != null && server.isPresent()) {
            DataCorePlayer dataCorePlayer = corePlayerManager.getCorePlayer(e.getPlayer().getUniqueId()).getDataCorePlayer();
            pluginDataManager.sendPluginData(server.get(), "coreplayer", "get", serializeObject(dataCorePlayer));
        }
        IFriendManager friendManager = instance.getFriendManager();
        if(friendManager.getFriendPlayer(e.getPlayer().getUniqueId()) != null && server.isPresent()) {
            DataFriendPlayer dataFriendPlayer = friendManager.getFriendPlayer(e.getPlayer().getUniqueId()).getDataFriendPlayer();
            pluginDataManager.sendPluginData(server.get(), "friend", "get", serializeObject(dataFriendPlayer));
        }
    }

    private String serializeObject(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }

}
