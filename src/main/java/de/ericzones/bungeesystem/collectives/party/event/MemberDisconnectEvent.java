// Created by Eric B. 02.04.2021 20:20
package de.ericzones.bungeesystem.collectives.party.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.party.IPartyManager;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;

public class MemberDisconnectEvent {

    private final BungeeSystem instance;

    public MemberDisconnectEvent(BungeeSystem instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onMemberDisconnect(DisconnectEvent e) {
        IPartyManager partyManager = instance.getPartyManager();
        if(!partyManager.isInParty(e.getPlayer().getUniqueId())) return;
        partyManager.leaveParty(e.getPlayer().getUniqueId());
        return;
    }

}
