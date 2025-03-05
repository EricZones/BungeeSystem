// Created by Eric B. 18.02.2021 18:07
package de.ericzones.bungeesystem.collectives.friend.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.collectives.friend.FriendPlayer;
import de.ericzones.bungeesystem.collectives.friend.FriendProperty;
import de.ericzones.bungeesystem.collectives.friend.IFriendManager;
import de.ericzones.bungeesystem.collectives.object.TextBuilder;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixHandler;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.UUID;

public class FriendConnectEvent {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;

    public FriendConnectEvent(BungeeSystem instance) {
        this.instance = instance;
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
        this.languageHandler = instance.getLanguageHandler();
    }

    @Subscribe
    public void onFriendConnect(PostLoginEvent e) {
        IFriendManager friendManager = instance.getFriendManager();
        FriendPlayer friendPlayer = friendManager.initialFriendPlayer(e.getPlayer().getUniqueId());
        sendFriendMessages(friendManager, friendPlayer, e.getPlayer());
        sendRequestMessage(friendPlayer, e.getPlayer());
    }

    private void sendFriendMessages(IFriendManager friendManager, FriendPlayer friendPlayer, Player player) {
        IOfflineCorePlayer targetPlayer = instance.getCorePlayerManager().getOfflineCorePlayer(friendPlayer.getUniqueId());
        Map<UUID, Long> friends = friendPlayer.getOnlineFriends();
        if(friends.size() == 0) return;
        for(UUID current : friends.keySet()) {
            ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(current);
            if(corePlayer == null) continue;
            if(friendManager.getFriendPlayer(current).getPropertySetting(FriendProperty.FRIENDSTATUS) == FriendProperty.Setting.DISABLED) continue;
            Language language = corePlayer.getLanguage();
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+ languageHandler.getTranslatedMessage(Message.FRIEND_NOWONLNE, language)
                    .replace("%REPLACE%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()));
        }
        IOfflineCorePlayer offlineCorePlayer = friendPlayer.getOfflineCorePlayer();
        if(offlineCorePlayer == null) return;
        Language language = offlineCorePlayer.getLanguage();
        switch (friends.size()) {
            case 0:
                player.sendMessage(Component.text(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+
                        languageHandler.getTranslatedMessage(Message.FRIEND_ONLINE_NOBODY, language)));
                break;
            case 1:
                player.sendMessage(Component.text(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+
                        languageHandler.getTranslatedMessage(Message.FRIEND_ONLINE_SINGLE, language)));
                break;
            default:
                player.sendMessage(Component.text(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+
                        languageHandler.getTranslatedMessage(Message.FRIEND_ONLINE_MULTIPLE, language).replace("%REPLACE%", String.valueOf(friends.size()))));
                break;
        }
    }

    private void sendRequestMessage(FriendPlayer friendPlayer, Player player) {
        Map<UUID, Long> requests = friendPlayer.getRequests();
        IOfflineCorePlayer offlineCorePlayer = friendPlayer.getOfflineCorePlayer();
        if(offlineCorePlayer == null) return;
        Language language = offlineCorePlayer.getLanguage();
        TextBuilder textBuilder = new TextBuilder(null).setHoverText("§e"+languageHandler.getTranslatedMessage(Message.FRIEND_REQUESTS_HOVER, language))
                .setClickEvent(TextBuilder.Action.COMMAND, "friend requests").setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS));

        switch (requests.size()) {
            case 0:
                break;
            case 1:
                player.sendMessage(textBuilder.setText("§7"+languageHandler.getTranslatedMessage(Message.FRIEND_REQUESTS_SINGLE, language)).build());
                break;
            default:
                player.sendMessage(textBuilder.setText("§7"+languageHandler.getTranslatedMessage(Message.FRIEND_REQUESTS_MULTIPLE, language)
                        .replace("%REPLACE%", String.valueOf(requests.size()))).build());
                break;
        }
    }

}
