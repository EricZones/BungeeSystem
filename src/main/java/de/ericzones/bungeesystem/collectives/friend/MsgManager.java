// Created by Eric B. 30.04.2021 18:57
package de.ericzones.bungeesystem.collectives.friend;

import com.velocitypowered.api.event.EventManager;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.friend.event.SpyPlayerDisconnectEvent;
import de.ericzones.bungeesystem.collectives.object.Pair;
import de.ericzones.bungeesystem.collectives.party.Party;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixHandler;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;

import java.util.*;

public class MsgManager {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;

    private final Map<UUID, Pair<Long, UUID>> messageCache = new HashMap<>();
    private final List<UUID> spyPlayers = new ArrayList<>();

    public MsgManager(BungeeSystem instance) {
        this.instance = instance;
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
        this.languageHandler = instance.getLanguageHandler();
        EventManager eventManager = instance.getProxyServer().getEventManager();
        eventManager.register(instance, new SpyPlayerDisconnectEvent(instance));
    }

    public void sendMessage(ICorePlayer player, ICorePlayer target, String message) {
        player.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.MESSAGE)+"§7"+languageHandler.getTranslatedMessage(Message.MESSAGE_FROM_YOU, player.getLanguage())
                +" §8➟ "+target.getRankPrefix()+target.getUsername()+" §8» §e"+message);
        target.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.MESSAGE)+"§7"+player.getRankPrefix()+player.getUsername()
                +" §8➟ §7"+languageHandler.getTranslatedMessage(Message.MESSAGE_TO_YOU, target.getLanguage())+" §8» §e"+message);

        sendSpyMessage(player, target, message);

        this.messageCache.put(player.getUniqueID(), new Pair<>(System.currentTimeMillis()+5*60*1000, target.getUniqueID()));
        this.messageCache.put(target.getUniqueID(), new Pair<>(System.currentTimeMillis()+5*60*1000, player.getUniqueID()));
    }

    public void replyToMessage(ICorePlayer player, String message) {
        ICorePlayer target = this.instance.getCorePlayerManager().getCorePlayer(getReplyPlayer(player.getUniqueID()));
        player.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.MESSAGE)+"§7"+languageHandler.getTranslatedMessage(Message.MESSAGE_FROM_YOU, player.getLanguage())
                +" §8➟ "+target.getRankPrefix()+target.getUsername()+" §8» §e"+message);
        target.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.MESSAGE)+"§7"+player.getRankPrefix()+player.getUsername()
                +" §8➟ §7"+languageHandler.getTranslatedMessage(Message.MESSAGE_TO_YOU, target.getLanguage())+" §8» §e"+message);

        sendSpyMessage(player, target, message);

        this.messageCache.put(player.getUniqueID(), new Pair<>(System.currentTimeMillis()+5*60*1000, target.getUniqueID()));
        this.messageCache.put(target.getUniqueID(), new Pair<>(System.currentTimeMillis()+5*60*1000, player.getUniqueID()));
    }

    public void sendPartyMessage(ICorePlayer player, Party party, String message) {
        List<UUID> members = new ArrayList<>(party.getMembers()); members.add(party.getLeaderUniqueId());
        for(UUID current : members) {
            ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(current);
            if(corePlayer == null) continue;
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+player.getRankPrefix()+player.getUsername()+" §8» §7"+message);
        }
    }

    private void sendSpyMessage(ICorePlayer player, ICorePlayer target, String message) {
        for(UUID current : this.spyPlayers) {
            ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(current);
            if(corePlayer == null) continue;
            if(corePlayer.getUniqueID() == player.getUniqueID() || corePlayer.getUniqueID() == target.getUniqueID()) continue;
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.MESSAGE)+"§7"
                    +player.getRankPrefix()+player.getUsername()+" §8➟ §7"+target.getRankPrefix()+target.getUsername()+" §8» §e"+message);
        }
    }

    public UUID getReplyPlayer(UUID uuid) {
        return this.messageCache.get(uuid).getSecondObject();
    }

    public boolean canReplyMessage(UUID uuid) {
        for(UUID current : this.messageCache.keySet()) {
            if (this.messageCache.get(current).getFirstObject() < System.currentTimeMillis()) this.messageCache.remove(current);
        }
        return messageCache.containsKey(uuid);
    }

    public List<UUID> getSpyPlayers() {
        return spyPlayers;
    }
}
