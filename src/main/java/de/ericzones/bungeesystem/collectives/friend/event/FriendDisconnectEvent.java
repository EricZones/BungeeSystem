// Created by Eric B. 18.02.2021 18:07
package de.ericzones.bungeesystem.collectives.friend.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.collectives.friend.FriendPlayer;
import de.ericzones.bungeesystem.collectives.friend.FriendProperty;
import de.ericzones.bungeesystem.collectives.friend.IFriendManager;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixHandler;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;

import java.util.Map;
import java.util.UUID;

public class FriendDisconnectEvent {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;

    public FriendDisconnectEvent(BungeeSystem instance) {
        this.instance = instance;
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
        this.languageHandler = instance.getLanguageHandler();
    }

    @Subscribe
    public void onFriendDisconnect(DisconnectEvent e) {
        IFriendManager friendManager = instance.getFriendManager();
        IOfflineCorePlayer targetPlayer = instance.getCorePlayerManager().getOfflineCorePlayer(e.getPlayer().getUniqueId());
        FriendPlayer friendPlayer = friendManager.getFriendPlayer(e.getPlayer().getUniqueId());
        if(friendPlayer == null || targetPlayer == null) return;
        Map<UUID, Long> friends = friendPlayer.getOnlineFriends();
        if(friends.size() == 0) return;

        for(UUID current : friends.keySet()) {
            ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(current);
            if(corePlayer == null) continue;
            if(friendManager.getFriendPlayer(current).getPropertySetting(FriendProperty.FRIENDSTATUS) == FriendProperty.Setting.DISABLED) continue;
            Language language = corePlayer.getLanguage();
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+ languageHandler.getTranslatedMessage(Message.FRIEND_NOWOFFLINE, language)
                    .replace("%REPLACE%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()));
        }

        friendManager.getFriendPlayers().remove(friendPlayer);
    }

}
