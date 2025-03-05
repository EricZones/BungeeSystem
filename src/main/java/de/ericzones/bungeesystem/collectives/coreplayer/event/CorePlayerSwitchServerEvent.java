// Created by Eric B. 31.01.2021 16:30
package de.ericzones.bungeesystem.collectives.coreplayer.event;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.util.GameProfile;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.coreplayer.SqlCorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.connection.PlayerConnectionInfo;
import de.ericzones.bungeesystem.collectives.database.ISqlAdapter;
import de.ericzones.bungeesystem.collectives.server.ICoreServer;
import de.ericzones.bungeesystem.collectives.server.ICoreServerManager;

import java.util.Base64;
import java.util.Iterator;
import java.util.UUID;

public class CorePlayerSwitchServerEvent extends SqlCorePlayer {

    private final BungeeSystem instance;

    public CorePlayerSwitchServerEvent(BungeeSystem instance, ISqlAdapter sqlAdapter) {
        super(sqlAdapter);
        this.instance = instance;
    }

    @Subscribe
    public void onCorePlayerSwitchServer(ServerPostConnectEvent e) {
        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        ICoreServerManager coreServerManager = instance.getCoreServerManager();

        if(e.getPreviousServer() == null && corePlayerManager.getCorePlayer(e.getPlayer().getUniqueId()) == null) {
            Player player = e.getPlayer();
            UUID uuid = player.getUniqueId();
            String username = player.getUsername();
            String ipAddress = player.getRemoteAddress().getAddress().getHostAddress();

            RegisteredServer registeredServer = null;
            if(player.getCurrentServer().isPresent())
                registeredServer = player.getCurrentServer().get().getServer();

            String skinValue = getSkinValue(player);
            ICoreServer coreServer = coreServerManager.getCoreServer(registeredServer);
            PlayerConnectionInfo connectionInfo = corePlayerManager.createConnectionInfo(uuid, ipAddress);

            ICorePlayer corePlayer = corePlayerManager.initialCorePlayer(uuid, username, skinValue, connectionInfo, coreServer);

            if(coreServer.isPlaytimeEnabled())
                corePlayer.getPlaytime().setActive();
            else
                corePlayer.getPlaytime().setIdle();
            return;
        }
        UUID uuid = e.getPlayer().getUniqueId();
        ICorePlayer corePlayer = corePlayerManager.getCorePlayer(uuid);

        RegisteredServer registeredServer = null;
        if(e.getPlayer().getCurrentServer().isPresent())
            registeredServer = e.getPlayer().getCurrentServer().get().getServer();

        ICoreServer coreServer = coreServerManager.getCoreServer(registeredServer);
        corePlayerManager.serverSwitchCorePlayer(corePlayer, coreServer);

        if(coreServer.isPlaytimeEnabled())
           corePlayer.getPlaytime().setActive();
        else
            corePlayer.getPlaytime().setIdle();
    }

    private String getSkinValue(Player player) {
        String skinURL = null;
        GameProfile gameProfile = player.getGameProfile();

        Iterator<GameProfile.Property> propertyIterator = gameProfile.getProperties().iterator();
        while (propertyIterator.hasNext()) {
            GameProfile.Property current = propertyIterator.next();
            if (current.getName().equals("textures")) {
                Base64.Decoder decoder = Base64.getDecoder();
                JsonObject jsonObject = JsonParser.parseString(new String(decoder.decode(current.getValue().getBytes()))).getAsJsonObject();
                skinURL = jsonObject.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
                break;
            }
        }
        return skinURL;
    }

}
