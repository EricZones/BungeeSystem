// Created by Eric B. 29.04.2021 11:49
package de.ericzones.bungeesystem.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.friend.FriendPlayer;
import de.ericzones.bungeesystem.collectives.friend.FriendProperty;
import de.ericzones.bungeesystem.collectives.friend.IFriendManager;
import de.ericzones.bungeesystem.collectives.friend.MsgManager;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.chatmessage.ChatMessageHandler;
import de.ericzones.bungeesystem.global.messaging.chatmessage.ChatMessageType;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixHandler;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;
import de.ericzones.bungeesystem.global.permission.PermissionHandler;
import de.ericzones.bungeesystem.global.permission.PermissionType;
import net.kyori.adventure.text.Component;

public class MsgCommand implements SimpleCommand {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;
    private final ChatMessageHandler chatMessageHandler;
    private final PermissionHandler permissionHandler;

    public MsgCommand(BungeeSystem instance) {
        this.instance = instance;
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
        this.languageHandler = instance.getLanguageHandler();
        this.chatMessageHandler = instance.getChatMessageHandler();
        this.permissionHandler = instance.getPermissionHandler();
    }

    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String alias = invocation.alias();
        String[] args = invocation.arguments();

        if(!(source instanceof Player)) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.NOCONSOLE, Language.ENGLISH)));
            return;
        }

        IFriendManager friendManager = instance.getFriendManager();
        MsgManager msgManager = instance.getMsgManager();
        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        ICorePlayer corePlayer = corePlayerManager.getCorePlayer(((Player) source).getUniqueId());
        FriendPlayer friendPlayer = friendManager.getFriendPlayer(((Player) source).getUniqueId());
        if(corePlayer == null || friendPlayer == null) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.ERROR_COMMAND, Language.ENGLISH)));
            return;
        }
        Language language = corePlayer.getLanguage();

        if(alias.equalsIgnoreCase("msg")) {
            if(args.length >= 2) {
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[0]);
                if(targetPlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.MESSAGE)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                if(corePlayer.getUniqueID() == targetPlayer.getUniqueID()) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.MESSAGE)+"§7"+languageHandler.getTranslatedMessage(Message.MESSAGE_SELF, language));
                    return;
                }
                FriendPlayer targetFriend = friendManager.getFriendPlayer(targetPlayer.getUniqueID());
                if(targetFriend.getPropertySetting(FriendProperty.MESSAGES) == FriendProperty.Setting.DISABLED &&
                        !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_IGNORESETTINGS))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.MESSAGE)+"§7"+languageHandler.getTranslatedMessage(Message.MESSAGE_DISABLED_ALL, language));
                    return;
                }
                if(targetFriend.getPropertySetting(FriendProperty.MESSAGES) == FriendProperty.Setting.FRIENDS && !friendPlayer.isFriend(targetFriend.getUniqueId()) &&
                        !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_IGNORESETTINGS))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.MESSAGE)+"§7"+languageHandler.getTranslatedMessage(Message.MESSAGE_DISABLED_FRIENDS, language));
                    return;
                }
                String message = "";
                for (int i = 1; i < args.length; i++) {
                    message += args[i] + " ";
                }
                message = message.trim();
                message = message.replace("<3", "❤");

                msgManager.sendMessage(corePlayer, targetPlayer, message);
                return;
            }
        } else if(alias.equalsIgnoreCase("reply") || alias.equalsIgnoreCase("r")) {
            if(args.length >= 1) {
                if(!msgManager.canReplyMessage(corePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.MESSAGE)+"§7"+languageHandler.getTranslatedMessage(Message.MESSAGE_REPLY_NOTFOUND, language));
                    return;
                }
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(msgManager.getReplyPlayer(corePlayer.getUniqueID()));
                if(targetPlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.MESSAGE)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                String message = "";
                for (int i = 0; i < args.length; i++) {
                    message += args[i] + " ";
                }
                message = message.trim();
                message = message.replace("<3", "❤");

                msgManager.replyToMessage(corePlayer, message);
                return;
            }
        }

        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                .replace("%REPLACE%", "friend help"));
    }

}
