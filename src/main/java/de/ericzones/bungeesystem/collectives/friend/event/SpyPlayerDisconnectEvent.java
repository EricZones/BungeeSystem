// Created by Eric B. 01.05.2021 15:18
package de.ericzones.bungeesystem.collectives.friend.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.friend.MsgManager;

public class SpyPlayerDisconnectEvent {

    private final BungeeSystem instance;

    public SpyPlayerDisconnectEvent(BungeeSystem instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onSpyPlayerDisconnect(DisconnectEvent e) {
        MsgManager msgManager = instance.getMsgManager();
        msgManager.getSpyPlayers().remove(e.getPlayer().getUniqueId());
    }

}
