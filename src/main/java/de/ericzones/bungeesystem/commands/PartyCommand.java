// Created by Eric B. 19.04.2021 13:57
package de.ericzones.bungeesystem.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.CorePlayerSwitchResult;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.friend.FriendPlayer;
import de.ericzones.bungeesystem.collectives.friend.FriendProperty;
import de.ericzones.bungeesystem.collectives.friend.IFriendManager;
import de.ericzones.bungeesystem.collectives.object.CommandWindow;
import de.ericzones.bungeesystem.collectives.object.TextBuilder;
import de.ericzones.bungeesystem.collectives.party.IPartyManager;
import de.ericzones.bungeesystem.collectives.party.Party;
import de.ericzones.bungeesystem.collectives.party.PartyManager;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyCommand implements SimpleCommand {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;
    private final ChatMessageHandler chatMessageHandler;
    private final PermissionHandler permissionHandler;

    private CommandWindow commandWindow;
    
    public PartyCommand(BungeeSystem instance) {
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
        IPartyManager partyManager = instance.getPartyManager();
        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        ICorePlayer corePlayer = corePlayerManager.getCorePlayer(((Player) source).getUniqueId());
        if(corePlayer == null) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.ERROR_COMMAND, Language.ENGLISH)));
            return;
        }
        Language language = corePlayer.getLanguage();

        if(args.length == 0) {
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                    .replace("%REPLACE%", "party help"));
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
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                    return;
                }
            } catch (NumberFormatException e) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                return;
            }
            commandWindow.sendWindow(corePlayer, pageNumber);
            return;
        }

        if(args[0].equalsIgnoreCase("list")) {
            if(!partyManager.isInParty(corePlayer.getUniqueID())) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+
                        languageHandler.getTranslatedMessage(Message.PARTY_NOTFOUND, language));
                return;
            }
            Party party = partyManager.getPartyFromPlayer(corePlayer.getUniqueID());

            if(args.length == 1) {
                sendPartyMembersWindow(corePlayer, party, 1);
                return;
            }
            int pageNumber;
            try {
                pageNumber = Integer.parseInt(args[1]);
                if(pageNumber <= 0) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                    return;
                }
            } catch (NumberFormatException e) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                return;
            }
            sendPartyMembersWindow(corePlayer, party, pageNumber);
            return;
        }

        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("create")) {
                if(partyManager.isInParty(corePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+
                            languageHandler.getTranslatedMessage(Message.PARTY_ALREADY_MEMBER, language));
                    return;
                }
                partyManager.initialParty(corePlayer.getUniqueID());
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_CREATED, language));
                return;
            }
            if(args[0].equalsIgnoreCase("delete")) {
                if(!partyManager.isInParty(corePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+
                            languageHandler.getTranslatedMessage(Message.PARTY_NOTFOUND, language));
                    return;
                }
                Party party = partyManager.getPartyFromPlayer(corePlayer.getUniqueID());
                if(party.getLeaderUniqueId() != corePlayer.getUniqueID()) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+
                            languageHandler.getTranslatedMessage(Message.PARTY_NOT_LEADER, language));
                    return;
                }
                partyManager.removeParty(party, PartyManager.PartyDeleteReason.DISBANDED);
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+
                        languageHandler.getTranslatedMessage(Message.PARTY_DELETED, language));
                return;
            }
            if(args[0].equalsIgnoreCase("leave")) {
                if(!partyManager.isInParty(corePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+
                            languageHandler.getTranslatedMessage(Message.PARTY_NOTFOUND, language));
                    return;
                }
                partyManager.leaveParty(corePlayer.getUniqueID());
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_LEFT, language));
                return;
            }
            if(args[0].equalsIgnoreCase("jump")) {
                if(!partyManager.isInParty(corePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+
                            languageHandler.getTranslatedMessage(Message.PARTY_NOTFOUND, language));
                    return;
                }
                Party party = partyManager.getPartyFromPlayer(corePlayer.getUniqueID());
                if(party.getLeaderUniqueId() == corePlayer.getUniqueID()) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_JUMP_SELF, language));
                    return;
                }
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(party.getLeaderUniqueId());
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

        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("invite")) {
                if(!partyManager.isInParty(corePlayer.getUniqueID())) {
                    ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
                    if(targetPlayer == null) {
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                        return;
                    }
                    if(targetPlayer.getUniqueID() == corePlayer.getUniqueID()) {
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_INVITE_SELF, language));
                        return;
                    }
                    FriendPlayer friendPlayer = friendManager.getFriendPlayer(corePlayer.getUniqueID());
                    FriendPlayer targetFriend = friendManager.getFriendPlayer(targetPlayer.getUniqueID());
                    if(targetFriend.getPropertySetting(FriendProperty.PARTYINVITES) == FriendProperty.Setting.DISABLED &&
                            !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_IGNORESETTINGS))) {
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_INVITE_DISABLED_ALL, language));
                        return;
                    }
                    if(targetFriend.getPropertySetting(FriendProperty.PARTYINVITES) == FriendProperty.Setting.FRIENDS && !friendPlayer.isFriend(targetFriend.getUniqueId()) &&
                            !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_IGNORESETTINGS))) {
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_INVITE_DISABLED_FRIENDS, language));
                        return;
                    }
                    Party party = partyManager.initialParty(corePlayer.getUniqueID());
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_CREATED, language));
                    partyManager.invitePlayer(party, targetPlayer.getUniqueID());
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_INVITE_SENT, language)
                            .replace("%REPLACE%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()));
                    return;
                }
                Party party = partyManager.getPartyFromPlayer(corePlayer.getUniqueID());
                if(party.getLeaderUniqueId() != corePlayer.getUniqueID()) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+
                            languageHandler.getTranslatedMessage(Message.PARTY_NOT_LEADER, language));
                    return;
                }
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
                if(targetPlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                if(targetPlayer.getUniqueID() == corePlayer.getUniqueID()) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_INVITE_SELF, language));
                    return;
                }
                if(party.isMember(targetPlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_INVITE_ALREADY_MEMBER, language));
                    return;
                }
                if(party.isInvited(targetPlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_INVITE_ALREADY_INVITED, language));
                    return;
                }
                int memberAmount = party.getMembers().size();
                if(memberAmount >= partyManager.getStandardMemberAmount() && !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.PARTY_COUNT_PREMIUM)) &&
                        !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.PARTY_COUNT_UNLIMITED))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_FULL_STANDARD, language));
                    return;
                }
                if(memberAmount >= partyManager.getPremiumMemberAmount() && !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.PARTY_COUNT_UNLIMITED))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_FULL_PREMIUM, language));
                    return;
                }
                FriendPlayer friendPlayer = friendManager.getFriendPlayer(corePlayer.getUniqueID());
                FriendPlayer targetFriend = friendManager.getFriendPlayer(targetPlayer.getUniqueID());
                if(targetFriend.getPropertySetting(FriendProperty.PARTYINVITES) == FriendProperty.Setting.DISABLED &&
                        !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_IGNORESETTINGS))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_INVITE_DISABLED_ALL, language));
                    return;
                }
                if(targetFriend.getPropertySetting(FriendProperty.PARTYINVITES) == FriendProperty.Setting.FRIENDS && !friendPlayer.isFriend(targetFriend.getUniqueId()) &&
                        !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.FRIEND_IGNORESETTINGS))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_INVITE_DISABLED_FRIENDS, language));
                    return;
                }
                partyManager.invitePlayer(party, targetPlayer.getUniqueID());
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_INVITE_SENT, language)
                .replace("%REPLACE%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()));
                return;
            }
            if(args[0].equalsIgnoreCase("kick")) {
                if(!partyManager.isInParty(corePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+
                            languageHandler.getTranslatedMessage(Message.PARTY_NOTFOUND, language));
                    return;
                }
                Party party = partyManager.getPartyFromPlayer(corePlayer.getUniqueID());
                if(party.getLeaderUniqueId() != corePlayer.getUniqueID()) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+
                            languageHandler.getTranslatedMessage(Message.PARTY_NOT_LEADER, language));
                    return;
                }
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
                if(targetPlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                if(targetPlayer.getUniqueID() == corePlayer.getUniqueID()) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_KICK_SELF, language));
                    return;
                }
                if(!party.isMember(targetPlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_TARGET_NOTMEMBER, language));
                    return;
                }
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_KICKED, language)
                .replace("%REPLACE%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()));
                partyManager.kickPlayer(targetPlayer.getUniqueID());
                return;
            }
            if(args[0].equalsIgnoreCase("accept")) {
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
                if(targetPlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                if(!partyManager.isInParty(targetPlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_ACCEPT_NOPARTY, language));
                    return;
                }
                Party party = partyManager.getPartyFromPlayer(targetPlayer.getUniqueID());
                if(!party.isInvited(corePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_ACCEPT_NOINVITE, language));
                    return;
                }
                int memberAmount = party.getMembers().size();
                if(memberAmount >= partyManager.getStandardMemberAmount() && !targetPlayer.hasPermission(permissionHandler.getPermission(PermissionType.PARTY_COUNT_PREMIUM)) &&
                        !targetPlayer.hasPermission(permissionHandler.getPermission(PermissionType.PARTY_COUNT_UNLIMITED))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_TARGET_FULL, language));
                    return;
                }
                if(memberAmount >= partyManager.getPremiumMemberAmount() && !targetPlayer.hasPermission(permissionHandler.getPermission(PermissionType.PARTY_COUNT_UNLIMITED))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_TARGET_FULL, language));
                    return;
                }
                partyManager.acceptInvite(party, corePlayer.getUniqueID());
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_ACCEPTED, language)
                .replace("%REPLACE%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()));
                return;
            }
            if(args[0].equalsIgnoreCase("deny")) {
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
                if(targetPlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                if(!partyManager.isInParty(targetPlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_ACCEPT_NOPARTY, language));
                    return;
                }
                Party party = partyManager.getPartyFromPlayer(targetPlayer.getUniqueID());
                if(!party.isInvited(corePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_ACCEPT_NOINVITE, language));
                    return;
                }
                party.removeInvite(corePlayer.getUniqueID());
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_DENIED, language)
                .replace("%REPLACE%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()));
                return;
            }
            if(args[0].equalsIgnoreCase("promote")) {
                if(!partyManager.isInParty(corePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+
                            languageHandler.getTranslatedMessage(Message.PARTY_NOTFOUND, language));
                    return;
                }
                Party party = partyManager.getPartyFromPlayer(corePlayer.getUniqueID());
                if(party.getLeaderUniqueId() != corePlayer.getUniqueID()) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+
                            languageHandler.getTranslatedMessage(Message.PARTY_NOT_LEADER, language));
                    return;
                }
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
                if(targetPlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                if(targetPlayer.getUniqueID() == corePlayer.getUniqueID()) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_PROMOTE_SELF, language));
                    return;
                }
                if(!party.isMember(targetPlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_TARGET_NOTMEMBER, language));
                    return;
                }
                partyManager.setPartyLeader(party, targetPlayer.getUniqueID());
                party.addMember(corePlayer.getUniqueID());
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_PROMOTED, language)
                .replace("%REPLACE%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()));
                return;
            }
        }

        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                .replace("%REPLACE%", "party help"));
    }

    private void registerCommandWindow() {
        commandWindow = new CommandWindow("Party Help", "§b", pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY), "party help");

        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PARTY_HELP_LIST, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_LIST_HOVER, Language.GERMAN), "party list");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PARTY_HELP_CREATE, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_CREATE_HOVER, Language.GERMAN), "party create");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PARTY_HELP_DELETE, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_DELETE_HOVER, Language.GERMAN), "party delete");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PARTY_HELP_INVITE, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_INVITE_HOVER, Language.GERMAN), "party invite <Spieler>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PARTY_HELP_KICK, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_KICK_HOVER, Language.GERMAN), "party kick <Spieler>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PARTY_HELP_LEAVE, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_LEAVE_HOVER, Language.GERMAN), "party leave");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PARTY_HELP_PROMOTE, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_PROMOTE_HOVER, Language.GERMAN), "party promote <Spieler>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PARTY_HELP_JUMP, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_JUMP_HOVER, Language.GERMAN), "party jump");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PARTY_HELP_ACCEPT, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_ACCEPT_HOVER, Language.GERMAN), "party accept <Spieler>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PARTY_HELP_DENY, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_DENY_HOVER, Language.GERMAN), "party deny <Spieler>");

        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PARTY_HELP_LIST, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_LIST_HOVER, Language.ENGLISH), "party list");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PARTY_HELP_CREATE, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_CREATE_HOVER, Language.ENGLISH), "party create");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PARTY_HELP_DELETE, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_DELETE_HOVER, Language.ENGLISH), "party delete");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PARTY_HELP_INVITE, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_INVITE_HOVER, Language.ENGLISH), "party invite <Player>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PARTY_HELP_KICK, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_KICK_HOVER, Language.ENGLISH), "party kick <Player>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PARTY_HELP_LEAVE, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_LEAVE_HOVER, Language.ENGLISH), "party leave");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PARTY_HELP_PROMOTE, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_PROMOTE_HOVER, Language.ENGLISH), "party promote <Player>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PARTY_HELP_JUMP, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_JUMP_HOVER, Language.ENGLISH), "party jump");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PARTY_HELP_ACCEPT, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_ACCEPT_HOVER, Language.ENGLISH), "party accept <Player>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PARTY_HELP_DENY, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PARTY_HELP_DENY_HOVER, Language.ENGLISH), "party deny <Player>");
        
        commandWindow.initializeCommands();
    }

    private void sendPartyMembersWindow(ICorePlayer corePlayer, Party party, int page) {
        Language language = corePlayer.getLanguage();
        boolean isLeader = party.getLeaderUniqueId() == corePlayer.getUniqueID();
        
        List<TextComponent> members;
        if(page == 1)
            members = loadCurrentMembers(language, party, isLeader, 0, 5);
        else
            members = loadCurrentMembers(language, party, isLeader, ((page-1)*5)+1, page*5);
        if(members.size() == 0) {
            if(page == 1)
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.FRIEND_REQUEST_NONE, language));
            else
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
            return;
        }
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage("§8§m------------§r §7● §bPartylist §7● §8§m------------");
        corePlayer.sendMessage(" ");
        for(TextComponent current : members)
            corePlayer.sendMessage(current);
        corePlayer.sendMessage(" ");
        if(loadCurrentMembers(language, party, isLeader, (((page+1)-1)*5)+1, (page+1)*5).size() != 0) {
            if(page == 1) {
                TextComponent nextButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page+1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "party list "+(page+1)).build();
                TextComponent text = Component.text("§7« §8[§c§l✘§r§8] §7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language)+" §8§l┃ ").append(nextButton).append(Component.text(" §8[§b" + (page + 1) + "§8] §7»"));
                corePlayer.sendMessage(text);
            } else {
                TextComponent nextButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page+1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "party list "+(page+1)).build();
                TextComponent previousButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language))
                        .setHoverText("§8"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page-1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "party list "+(page-1)).build();
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
                        .setClickEvent(TextBuilder.Action.COMMAND, "party list "+(page-1)).build();
                TextComponent text = Component.text("§7« §8[§b" + (page - 1) + "§8] ").append(previousButton).append(Component.text(" §8§l┃ §7"
                        +languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language)+" §8[§c§l✘§r§8] §7»"));
                corePlayer.sendMessage(text);
            }
        }
    }
    
    private List<TextComponent> loadCurrentMembers(Language language, Party party, boolean leader, int from, int to) {
        List<TextComponent> list = new ArrayList<>();
        List<TextComponent> members = getCurrentMembers(language, party, leader);
        for(int i = from; i < to+1 && i < members.size(); i++)
            list.add(members.get(i));
        return list;
    }
    
    private List<TextComponent> getCurrentMembers(Language language, Party party, boolean leader) {
        List<TextComponent> currentMembers = new ArrayList<>();

        ICorePlayer leaderPlayer = instance.getCorePlayerManager().getCorePlayer(party.getLeaderUniqueId());
        List<UUID> members = new ArrayList<>(party.getMembers());
        
        if(leader) {
            currentMembers.add(Component.text(" §8× "+leaderPlayer.getRankPrefix()+leaderPlayer.getUsername()));
            for(UUID current : members) {
                ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(current);
                if(corePlayer == null) continue;

                TextComponent kickButton = new TextBuilder("§c"+languageHandler.getTranslatedMessage(Message.PARTY_BUTTON_KICK, language))
                        .setHoverText("§c"+languageHandler.getTranslatedMessage(Message.PARTY_BUTTON_KICK_HOVER, language))
                        .setClickEvent(TextBuilder.Action.COMMAND, "party kick "+corePlayer.getUsername()).build();
                TextComponent promoteButton = new TextBuilder("§a"+languageHandler.getTranslatedMessage(Message.PARTY_BUTTON_PROMOTE, language))
                        .setHoverText("§a"+languageHandler.getTranslatedMessage(Message.PARTY_BUTTON_PROMOTE_HOVER, language))
                        .setClickEvent(TextBuilder.Action.COMMAND, "party promote "+corePlayer.getUsername()).build();

                TextComponent text = Component.text(" §8× "+corePlayer.getRankPrefix()+corePlayer.getUsername()+" §8● ")
                        .append(promoteButton).append(Component.text(" §8● ")).append(kickButton);
                currentMembers.add(text);
            }
        } else {
            currentMembers.add(Component.text(" §8× "+leaderPlayer.getRankPrefix()+leaderPlayer.getUsername()+" §8● §c"+
                    languageHandler.getTranslatedMessage(Message.PARTY_LEADER, language)));
            for(UUID current : members) {
                ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(current);
                if(corePlayer == null) continue;

                TextComponent text = Component.text(" §8× "+corePlayer.getRankPrefix()+corePlayer.getUsername()+" §8● §b"+
                        languageHandler.getTranslatedMessage(Message.PARTY_MEMBER, language));
                currentMembers.add(text);
            }
        }
        
        return currentMembers;
    }

}
