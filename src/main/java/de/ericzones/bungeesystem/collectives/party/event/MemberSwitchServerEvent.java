// Created by Eric B. 02.04.2021 20:21
package de.ericzones.bungeesystem.collectives.party.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.CorePlayerSwitchResult;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.party.IPartyManager;
import de.ericzones.bungeesystem.collectives.server.CoreServerType;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixHandler;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;

import java.util.UUID;

public class MemberSwitchServerEvent {

    private final BungeeSystem instance;
    private final LanguageHandler languageHandler;
    private final PluginPrefixHandler pluginPrefixHandler;

    public MemberSwitchServerEvent(BungeeSystem instance) {
        this.instance = instance;
        this.languageHandler = instance.getLanguageHandler();
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
    }

    @Subscribe
    public void onMemberSwitchServer(ServerPostConnectEvent e) {
        IPartyManager partyManager = instance.getPartyManager();
        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        if(!partyManager.isInParty(e.getPlayer().getUniqueId())) return;
        if(partyManager.getPartyFromLeader(e.getPlayer().getUniqueId()) == null) return;
        ICorePlayer corePlayer = corePlayerManager.getCorePlayer(e.getPlayer().getUniqueId());
        if(corePlayer.getConnectedCoreServer().getServerType() != CoreServerType.GAMESERVER) return;

        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+
                languageHandler.getTranslatedMessage(Message.PARTY_SERVER_SWITCHED, corePlayer.getLanguage()).replace("%REPLACE%", corePlayer.getConnectedCoreServer().getServerName()));

        for(UUID current : partyManager.getPartyFromLeader(e.getPlayer().getUniqueId()).getMembers()) {
            ICorePlayer currentCorePlayer = corePlayerManager.getCorePlayer(current);
            if(currentCorePlayer == null) continue;
            CorePlayerSwitchResult result = currentCorePlayer.sendToServer(corePlayer.getConnectedCoreServer());
            currentCorePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"
                    +languageHandler.getTranslatedMessage(Message.PARTY_SERVER_SWITCHED, corePlayer.getLanguage()).replace("%REPLACE%", corePlayer.getConnectedCoreServer().getServerName()));

            switch (result) {
                case SERVERFULL:
                    currentCorePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                            +languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_FULL, currentCorePlayer.getLanguage()));
                    break;
                case NOPLAYERKICKED:
                    currentCorePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                            +languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_NOPLAYERKICKED, currentCorePlayer.getLanguage()));
                    break;
                case SERVERMAINTENANCE:
                    currentCorePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                            +languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_MAINTENANCE, currentCorePlayer.getLanguage()));
                    break;
                case SERVERRESTRICTED:
                    currentCorePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                            +languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_RESTRICTED, currentCorePlayer.getLanguage()));
                    break;
                case ALREADYCONNECTED:
                    currentCorePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                            +languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_ALREADYCONNECTED, currentCorePlayer.getLanguage()));
                    break;
            }
        }
    }

}
