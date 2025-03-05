// Created by Eric B. 14.05.2021 13:42
package de.ericzones.bungeesystem.collectives.plugindata;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.proxy.messages.LegacyChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.plugindata.event.IncomingDataEvent;
import de.ericzones.bungeesystem.collectives.plugindata.event.PlayerSwitchServerEvent;

public class PluginDataManager {

    public final BungeeSystem instance;

    public PluginDataManager(BungeeSystem instance) {
        this.instance = instance;
        EventManager eventManager = instance.getProxyServer().getEventManager();
        eventManager.register(instance, new IncomingDataEvent(instance));
        eventManager.register(instance, new PlayerSwitchServerEvent(instance));
        instance.getProxyServer().getChannelRegistrar().register(new LegacyChannelIdentifier("Bungeesystem"));
    }

    public void sendPluginData(RegisteredServer server, String channel, String method) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(channel);
        out.writeUTF(method);
        server.sendPluginMessage(new LegacyChannelIdentifier("Bungeesystem"), out.toByteArray());
    }

    public void sendPluginData(RegisteredServer server, String channel, String method, String serializedObject) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(channel);
        out.writeUTF(method);
        out.writeUTF(serializedObject);
        server.sendPluginMessage(new LegacyChannelIdentifier("Bungeesystem"), out.toByteArray());
    }

}
