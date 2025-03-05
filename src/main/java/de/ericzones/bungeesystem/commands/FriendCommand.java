// Created by Eric B. 16.04.2021 13:42
package de.ericzones.bungeesystem.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.CorePlayerSwitchResult;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.collectives.friend.FriendPlayer;
import de.ericzones.bungeesystem.collectives.friend.FriendProperty;
import de.ericzones.bungeesystem.collectives.friend.IFriendManager;
import de.ericzones.bungeesystem.collectives.object.CommandWindow;
import de.ericzones.bungeesystem.collectives.object.TextBuilder;
import de.ericzones.bungeesystem.collectives.server.CoreServerType;
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
import net.kyori.adventure.text.TextComponent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class FriendCommand implements SimpleCommand {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;
    private final ChatMessageHandler chatMessageHandler;
    private final PermissionHandler permissionHandler;

    private CommandWindow commandWindow;

    public FriendCommand(BungeeSystem instance) {
        this.instance = instance;
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
        this.languageHandler = instance.getLanguageHandler();
        this.chatMessageHandler = instance.getChatMessageHandler();
        this.permissionHandler = instance.getPermissionHandler();
        registerCommandWindow();
    }

    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if(!(source instanceof Player)) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.NOCONSOLE, Language.ENGLISH)));
            return;
        }

        IFriendManager friendManager = instance.getFriendManager();
        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        ICorePlayer corePlayer = corePlayerManager.getCorePlayer(((Player) source).getUniqueId());
        FriendPlayer friendPlayer = friendManager.getFriendPlayer(((Player) source).getUniqueId());
        if(corePlayer == null || friendPlayer == null) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.ERROR_COMMAND, Language.ENGLISH)));
            return;
        }
        Language language = corePlayer.getLanguage();

        if(args.length == 0) {
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                    .replace("%REPLACE%", "friend help"));
            return;
        }

        if(args[0].equalsIgnoreCase("help")) {
            if(args.length == 1) {
                commandWindow.sendWindow(corePlayer, 1);
                return;
            }
            int pageNumber;
            try {
                pageNumber = Integer.parseInt(args[1]);
                if(pageNumber <= 0) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                    return;
                }
            } catch (NumberFormatException e) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                return;
            }
            commandWindow.sendWindow(corePlayer, pageNumber);
            return;
        }

        if(args[0].equalsIgnoreCase("list")) {
            if(args.length == 1) {
                sendFriendsWindow(corePlayer, 1);
                return;
            }
            int pageNumber;
            try {
                pageNumber = Integer.parseInt(args[1]);
                if(pageNumber <= 0) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                    return;
                }
            } catch (NumberFormatException e) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                return;
            }
            sendFriendsWindow(corePlayer, pageNumber);
            return;
        }

        if(args[0].equalsIgnoreCase("requests")) {
            if(args.length == 1) {
                sendRequestsWindow(corePlayer, 1);
                return;
            }
            int pageNumber;
            try {
                pageNumber = Integer.parseInt(args[1]);
                if(pageNumber <= 0) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                    return;
                }
            } catch (NumberFormatException e) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                return;
            }
            sendRequestsWindow(corePlayer, pageNumber);
            return;
        }


        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("clear")) {
                if(friendPlayer.getFriends().size() == 0) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_LIST_NONE, language));
                    return;
                }
                friendManager.removeAllFriends(friendPlayer);
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_CLEARED, language));
                return;
            }
            if(args[0].equalsIgnoreCase("acceptall")) {
                if(friendPlayer.getRequests().size() == 0) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_REQUEST_NONE, language));
                    return;
                }
                friendManager.acceptAllFriendRequests(friendPlayer);
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_ACCEPTED_ALL, language));
                return;
            }
            if(args[0].equalsIgnoreCase("denyall")) {
                if(friendPlayer.getRequests().size() == 0) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_REQUEST_NONE, language));
                    return;
                }
                friendManager.denyAllFriendRequests(friendPlayer);
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_DENIED_ALL, language));
                return;
            }
        }

        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("accept")) {
                IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[1]);
                if(offlineCorePlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                if(friendPlayer.isFriend(offlineCorePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_ALREADY_FRIENDS, language));
                    return;
                }
                if(!friendPlayer.isRequested(offlineCorePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_NOT_REQUESTED, language));
                    return;
                }
                int friendsAmount = friendPlayer.getFriends().size();
                if(friendsAmount >= friendManager.getStandardFriendsAmount() && !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_COUNT_PREMIUM)) &&
                        !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_COUNT_UNLIMITED))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_FULL_STANDARD, language));
                    return;
                }
                if(friendsAmount >= friendManager.getPremiumFriendsAmount() && !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_COUNT_UNLIMITED))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_FULL_PREMIUM, language));
                    return;
                }

                FriendPlayer targetFriend = friendManager.getFriendPlayer(offlineCorePlayer.getUniqueID());
                friendsAmount = targetFriend.getFriends().size();
                if(friendsAmount >= friendManager.getStandardFriendsAmount() && !targetFriend.getOfflineCorePlayer().hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_COUNT_PREMIUM)) &&
                        !targetFriend.getOfflineCorePlayer().hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_COUNT_UNLIMITED))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_TARGET_FULL, language));
                    return;
                }
                if(friendsAmount >= friendManager.getPremiumFriendsAmount() && !targetFriend.getOfflineCorePlayer().hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_COUNT_UNLIMITED))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_TARGET_FULL, language));
                    return;
                }
                friendManager.acceptFriendRequest(friendPlayer, targetFriend);
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"
                        +languageHandler.getTranslatedMessage(Message.FRIEND_REQUESTACCEPTED, language).replace("%REPLACE%",
                        targetFriend.getOfflineCorePlayer().getRankPrefix()+targetFriend.getOfflineCorePlayer().getUsername()));
                return;
            }
            if(args[0].equalsIgnoreCase("deny")) {
                IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[1]);
                if(offlineCorePlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                if(friendPlayer.isFriend(offlineCorePlayer.getUniqueID())) {
                    friendManager.removeFriend(friendPlayer, friendManager.getFriendPlayer(offlineCorePlayer.getUniqueID()));
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"
                            +languageHandler.getTranslatedMessage(Message.FRIEND_REMOVEDFRIEND, language).replace("%REPLACE%",
                            offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername()));
                    return;
                }
                if(!friendPlayer.isRequested(offlineCorePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_NOT_REQUESTED, language));
                    return;
                }
                friendManager.denyFriendRequest(friendPlayer, friendManager.getFriendPlayer(offlineCorePlayer.getUniqueID()));
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_DENIED, language)
                .replace("%REPLACE%", offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername()));
                return;
            }
            if(args[0].equalsIgnoreCase("add")) {
                IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[1]);
                if(offlineCorePlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                if(offlineCorePlayer.getUniqueID() == corePlayer.getUniqueID()) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_ADD_SELF, language));
                    return;
                }
                if(friendPlayer.isFriend(offlineCorePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_ALREADY_FRIENDS, language));
                    return;
                }
                FriendPlayer targetFriend = friendManager.getFriendPlayer(offlineCorePlayer.getUniqueID());
                if(friendPlayer.isRequested(offlineCorePlayer.getUniqueID())) {
                    int friendsAmount = friendPlayer.getFriends().size();
                    if(friendsAmount >= friendManager.getStandardFriendsAmount() && !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_COUNT_PREMIUM)) &&
                            !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_COUNT_UNLIMITED))) {
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_FULL_STANDARD, language));
                        return;
                    }
                    if(friendsAmount >= friendManager.getPremiumFriendsAmount() && !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_COUNT_UNLIMITED))) {
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_FULL_PREMIUM, language));
                        return;
                    }

                    friendsAmount = targetFriend.getFriends().size();
                    if(friendsAmount >= friendManager.getStandardFriendsAmount() && !targetFriend.getOfflineCorePlayer().hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_COUNT_PREMIUM)) &&
                            !targetFriend.getOfflineCorePlayer().hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_COUNT_UNLIMITED))) {
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_TARGET_FULL, language));
                        return;
                    }
                    if(friendsAmount >= friendManager.getPremiumFriendsAmount() && !targetFriend.getOfflineCorePlayer().hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_COUNT_UNLIMITED))) {
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_TARGET_FULL, language));
                        return;
                    }
                    friendManager.acceptFriendRequest(friendPlayer, targetFriend);
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"
                            +languageHandler.getTranslatedMessage(Message.FRIEND_REQUESTACCEPTED, language).replace("%REPLACE%",
                            targetFriend.getOfflineCorePlayer().getRankPrefix()+targetFriend.getOfflineCorePlayer().getUsername()));
                    return;
                }
                if(targetFriend.isRequested(corePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_ALREADY_REQUESTED, language));
                    return;
                }
                int friendsAmount = friendPlayer.getFriends().size();
                if(friendsAmount >= friendManager.getStandardFriendsAmount() && !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_COUNT_PREMIUM)) &&
                        !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_COUNT_UNLIMITED))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_FULL_STANDARD, language));
                    return;
                }
                if(friendsAmount >= friendManager.getPremiumFriendsAmount() && !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_COUNT_UNLIMITED))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_FULL_PREMIUM, language));
                    return;
                }
                if(targetFriend.getPropertySetting(FriendProperty.REQUESTS) == FriendProperty.Setting.DISABLED &&
                        !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_IGNORESETTINGS))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_ADD_DISABLED, language));
                    return;
                }
                friendManager.sendFriendRequest(friendPlayer, targetFriend);
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_ADD_SENT, language)
                .replace("%REPLACE%", offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername()));
                return;
            }
            if(args[0].equalsIgnoreCase("remove")) {
                IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[1]);
                if(offlineCorePlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                if(friendPlayer.isRequested(offlineCorePlayer.getUniqueID())) {
                    friendManager.denyFriendRequest(friendPlayer, friendManager.getFriendPlayer(offlineCorePlayer.getUniqueID()));
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_DENIED, language)
                            .replace("%REPLACE%", offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername()));
                    return;
                }
                if(!friendPlayer.isFriend(offlineCorePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_NOT_FRIEND, language));
                    return;
                }
                friendManager.removeFriend(friendPlayer, friendManager.getFriendPlayer(offlineCorePlayer.getUniqueID()));
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"
                        +languageHandler.getTranslatedMessage(Message.FRIEND_REMOVEDFRIEND, language).replace("%REPLACE%",
                        offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername()));
                return;
            }
            if(args[0].equalsIgnoreCase("jump")) {
                IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[1]);
                if(offlineCorePlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                if(!friendPlayer.isFriend(offlineCorePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_NOT_FRIEND, language));
                    return;
                }
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(offlineCorePlayer.getUniqueID());
                if(targetPlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                FriendPlayer targetFriend = friendManager.getFriendPlayer(offlineCorePlayer.getUniqueID());
                if(targetFriend.getPropertySetting(FriendProperty.JUMPING) == FriendProperty.Setting.DISABLED &&
                        !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_IGNORESETTINGS))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS) + "§7" + languageHandler.getTranslatedMessage(Message.FRIEND_JUMP_DISABLED, language));
                    return;
                }
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_CONNECTING, language)
                .replace("%REPLACE%", targetPlayer.getConnectedCoreServer().getServerName()));
                CorePlayerSwitchResult result = corePlayer.sendToServer(targetPlayer.getConnectedCoreServer());

                switch (result) {
                    case ALREADYCONNECTED:
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                                +languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_ALREADYCONNECTED, language));
                        break;
                    case SERVERFULL:
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                                +languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_FULL, language));
                        break;
                    case NOPLAYERKICKED:
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                                +languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_NOPLAYERKICKED, language));
                        break;
                    case SERVERRESTRICTED:
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                                +languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_RESTRICTED, language));
                        break;
                    case SERVERMAINTENANCE:
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                                +languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_MAINTENANCE, language));
                        break;
                    default:
                        break;
                }
                return;
            }
        }

        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                .replace("%REPLACE%", "friend help"));
    }

    private void registerCommandWindow() {
        commandWindow = new CommandWindow("Friend Help", "§b", pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS), "friend help");

        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_LIST, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_LIST_HOVER, Language.GERMAN), "friend list");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_REQUESTS, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_REQUESTS_HOVER, Language.GERMAN), "friend requests");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_DENYALL, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_DENYALL_HOVER, Language.GERMAN), "friend denyall");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_ACCEPTALL, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_ACCEPTALL_HOVER, Language.GERMAN), "friend acceptall");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_CLEAR, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_CLEAR_HOVER, Language.GERMAN), "friend clear");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_ADD, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_ADD_HOVER, Language.GERMAN), "friend add <Spieler>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_REMOVE, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_REMOVE_HOVER, Language.GERMAN), "friend remove <Spieler>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_DENY, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_DENY_HOVER, Language.GERMAN), "friend deny <Spieler>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_ACCEPT, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_ACCEPT_HOVER, Language.GERMAN), "friend accept <Spieler>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_JUMP, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_JUMP_HOVER, Language.GERMAN), "friend jump <Spieler>");

        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_LIST, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_LIST_HOVER, Language.ENGLISH), "friend list");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_REQUESTS, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_REQUESTS_HOVER, Language.ENGLISH), "friend requests");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_DENYALL, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_DENYALL_HOVER, Language.ENGLISH), "friend denyall");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_ACCEPTALL, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_ACCEPTALL_HOVER, Language.ENGLISH), "friend acceptall");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_CLEAR, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_CLEAR_HOVER, Language.ENGLISH), "friend clear");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_ADD, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_ADD_HOVER, Language.ENGLISH), "friend add <Player>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_REMOVE, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_REMOVE_HOVER, Language.ENGLISH), "friend remove <Player>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_DENY, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_DENY_HOVER, Language.ENGLISH), "friend deny <Player>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_ACCEPT, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_ACCEPT_HOVER, Language.ENGLISH), "friend accept <Player>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.FRIEND_HELP_JUMP, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.FRIEND_HELP_JUMP_HOVER, Language.ENGLISH), "friend jump <Player>");

        commandWindow.initializeCommands();
    }

    private void sendFriendsWindow(ICorePlayer corePlayer, int page) {
        Language language = corePlayer.getLanguage();
        FriendPlayer friendPlayer = instance.getFriendManager().getFriendPlayer(corePlayer.getUniqueID());
        List<TextComponent> friends;
        if(page == 1)
            friends = loadCurrentFriends(language, friendPlayer, 0, 5);
        else
            friends = loadCurrentFriends(language, friendPlayer, ((page-1)*5)+1, page*5);
        if(friends.size() == 0) {
            if(page == 1)
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_LIST_NONE, language));
            else
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
            return;
        }
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage("§8§m------------§r §7● §bFriendlist §7● §8§m------------");
        corePlayer.sendMessage(" ");
        for(TextComponent current : friends)
            corePlayer.sendMessage(current);
        corePlayer.sendMessage(" ");
        if(loadCurrentFriends(language, friendPlayer, (((page+1)-1)*5)+1, (page+1)*5).size() != 0) {
            if(page == 1) {
                TextComponent nextButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page+1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "friend list "+(page+1)).build();
                TextComponent text = Component.text("§7« §8[§c§l✘§r§8] §7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language)+" §8§l┃ ").append(nextButton).append(Component.text(" §8[§b" + (page + 1) + "§8] §7»"));
                corePlayer.sendMessage(text);
            } else {
                TextComponent nextButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page+1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "friend list "+(page+1)).build();
                TextComponent previousButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language))
                        .setHoverText("§8"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page-1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "friend list "+(page-1)).build();
                TextComponent text = Component.text("§7« §8[§b" + (page-1) + "§8] ").append(previousButton).append(Component.text(" §8§l┃ ")).append(nextButton).append(Component.text(" §8[§b" + (page+1) + "§8] §7»"));
                corePlayer.sendMessage(text);
            }
        } else {
            if(page == 1)
                corePlayer.sendMessage("§7« §8[§c§l✘§r§8] §7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language)
                        +" §8§l┃ §7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language)+" §8[§c§l✘§r§8] §7»");
            else {
                TextComponent previousButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language))
                        .setHoverText("§8"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page-1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "friend list "+(page-1)).build();
                TextComponent text = Component.text("§7« §8[§b" + (page - 1) + "§8] ").append(previousButton).append(Component.text(" §8§l┃ §7"
                        +languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language)+" §8[§c§l✘§r§8] §7»"));
                corePlayer.sendMessage(text);
            }
        }
    }

    private List<TextComponent> loadCurrentFriends(Language language, FriendPlayer friendPlayer, int from, int to) {
        List<TextComponent> list = new ArrayList<>();
        List<TextComponent> friends = getCurrentFriends(language, friendPlayer);
        for(int i = from; i < to+1 && i < friends.size(); i++)
            list.add(friends.get(i));
        return list;
    }

    private List<TextComponent> getCurrentFriends(Language language, FriendPlayer friendPlayer) {
        List<TextComponent> currentFriends = new ArrayList<>();

        List<UUID> onlineFriends = new ArrayList<>(friendPlayer.getOnlineFriends().keySet());
        List<UUID> offlineFriends = new ArrayList<>(friendPlayer.getFriends().keySet());
        offlineFriends.removeAll(onlineFriends);

        for(UUID current : onlineFriends) {
            ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(current);
            if(corePlayer == null) continue;
            String status = "";
            if(corePlayer.getConnectedCoreServer().getServerType() == CoreServerType.GAMESERVER)
                status = "§a"+languageHandler.getTranslatedMessage(Message.FRIEND_STATUS_INGAME, language).replace("%REPLACE%", corePlayer.getConnectedCoreServer().getServerName());
            else
                status = "§a"+languageHandler.getTranslatedMessage(Message.FRIEND_STATUS_ONLINE, language).replace("%REPLACE%", corePlayer.getConnectedCoreServer().getServerName());

            TextComponent text = new TextBuilder(corePlayer.getRankPrefix()+corePlayer.getUsername()).setPreText(" §8× ").setPostText(" §8● "+status)
                    .setHoverText("§8● §7"+languageHandler.getTranslatedMessage(Message.FRIEND_INFO_CREATIONTIME, language)
                            .replace("%REPLACE%", getDateFromMillis(friendPlayer.getOnlineFriends().get(current)))+" §8●").build();
            currentFriends.add(text);
        }
        for(UUID current : offlineFriends) {
            IOfflineCorePlayer offlineCorePlayer = instance.getCorePlayerManager().getOfflineCorePlayer(current);
            if(offlineCorePlayer == null) continue;
            String status = "§c"+languageHandler.getTranslatedMessage(Message.FRIEND_STATUS_OFFLINE, language).replace("%REPLACE%", offlineCorePlayer.getLogoutDate());

            TextComponent text = new TextBuilder(offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername()).setPreText(" §8× ").setPostText(" §8● "+status)
                    .setHoverText("§8● §7"+languageHandler.getTranslatedMessage(Message.FRIEND_INFO_CREATIONTIME, language)
                            .replace("%REPLACE%", getDateFromMillis(friendPlayer.getFriends().get(current)))+" §8●").build();
            currentFriends.add(text);
        }
        return currentFriends;
    }

    private void sendRequestsWindow(ICorePlayer corePlayer, int page) {
        Language language = corePlayer.getLanguage();
        FriendPlayer friendPlayer = instance.getFriendManager().getFriendPlayer(corePlayer.getUniqueID());
        List<TextComponent> requests;
        if(page == 1)
            requests = loadCurrentRequests(language, friendPlayer, 0, 5);
        else
            requests = loadCurrentRequests(language, friendPlayer, ((page-1)*5)+1, page*5);
        if(requests.size() == 0) {
            if(page == 1)
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_REQUEST_NONE, language));
            else
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
            return;
        }
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage("§8§m------------§r §7● §bRequestlist §7● §8§m------------");
        corePlayer.sendMessage(" ");
        for(TextComponent current : requests)
            corePlayer.sendMessage(current);
        corePlayer.sendMessage(" ");
        if(loadCurrentRequests(language, friendPlayer, (((page+1)-1)*5)+1, (page+1)*5).size() != 0) {
            if(page == 1) {
                TextComponent nextButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page+1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "friend requests "+(page+1)).build();
                TextComponent text = Component.text("§7« §8[§c§l✘§r§8] §7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language)+" §8§l┃ ").append(nextButton).append(Component.text(" §8[§b" + (page + 1) + "§8] §7»"));
                corePlayer.sendMessage(text);
            } else {
                TextComponent nextButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page+1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "friend requests "+(page+1)).build();
                TextComponent previousButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language))
                        .setHoverText("§8"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page-1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "friend requests "+(page-1)).build();
                TextComponent text = Component.text("§7« §8[§b" + (page-1) + "§8] ").append(previousButton).append(Component.text(" §8§l┃ ")).append(nextButton).append(Component.text(" §8[§b" + (page+1) + "§8] §7»"));
                corePlayer.sendMessage(text);
            }
        } else {
            if(page == 1)
                corePlayer.sendMessage("§7« §8[§c§l✘§r§8] §7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language)
                        +" §8§l┃ §7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language)+" §8[§c§l✘§r§8] §7»");
            else {
                TextComponent previousButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language))
                        .setHoverText("§8"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page-1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "friend requests "+(page-1)).build();
                TextComponent text = Component.text("§7« §8[§b" + (page - 1) + "§8] ").append(previousButton).append(Component.text(" §8§l┃ §7"
                        +languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language)+" §8[§c§l✘§r§8] §7»"));
                corePlayer.sendMessage(text);
            }
        }
    }

    private List<TextComponent> loadCurrentRequests(Language language, FriendPlayer friendPlayer, int from, int to) {
        List<TextComponent> list = new ArrayList<>();
        List<TextComponent> requests = getCurrentRequests(language, friendPlayer);
        for(int i = from; i < to+1 && i < requests.size(); i++)
            list.add(requests.get(i));
        return list;
    }

    private List<TextComponent> getCurrentRequests(Language language, FriendPlayer friendPlayer) {
        List<TextComponent> currentRequests = new ArrayList<>();

        List<UUID> onlineRequests = new ArrayList<>(friendPlayer.getOnlineRequests().keySet());
        List<UUID> offlineRequests = new ArrayList<>(friendPlayer.getRequests().keySet());
        offlineRequests.removeAll(onlineRequests);

        for(UUID current : onlineRequests) {
            ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(current);
            if(corePlayer == null) continue;
            String status = "§a"+languageHandler.getTranslatedMessage(Message.SPY_STATUS_ONLINE, language);

            TextComponent text = new TextBuilder(corePlayer.getRankPrefix()+corePlayer.getUsername()).setPreText(" §8× ").setPostText(" §8● "+status)
                    .setHoverText("§8● §7"+languageHandler.getTranslatedMessage(Message.FRIEND_REQUEST_CREATIONTIME, language)
                            .replace("%REPLACE%", getDateFromMillis(friendPlayer.getOnlineRequests().get(current)))+" §8●").build();
            currentRequests.add(text);
        }
        for(UUID current : offlineRequests) {
            IOfflineCorePlayer offlineCorePlayer = instance.getCorePlayerManager().getOfflineCorePlayer(current);
            if(offlineCorePlayer == null) continue;
            String status = "§c"+languageHandler.getTranslatedMessage(Message.SPY_STATUS_OFFLINE, language);

            TextComponent text = new TextBuilder(offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername()).setPreText(" §8× ").setPostText(" §8● "+status)
                    .setHoverText("§8● §7"+languageHandler.getTranslatedMessage(Message.FRIEND_REQUEST_CREATIONTIME, language)
                            .replace("%REPLACE%", getDateFromMillis(friendPlayer.getRequests().get(current)))+" §8●").build();
            currentRequests.add(text);
        }
        return currentRequests;
    }

    private String getDateFromMillis(long millis) {
        Date date = new Date(millis);
        date.setHours(date.getHours()+BungeeSystem.getInstance().getTimezoneNumber());
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
    }

}
