// Created by Eric B. 13.04.2021 13:53
package de.ericzones.bungeesystem.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
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

import java.util.List;

public class OntimeCommand implements SimpleCommand {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;
    private final ChatMessageHandler chatMessageHandler;
    private final PermissionHandler permissionHandler;

    private final int topPlayersAmount = 5;

    public OntimeCommand(BungeeSystem instance) {
        this.instance = instance;
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
        this.languageHandler = instance.getLanguageHandler();
        this.chatMessageHandler = instance.getChatMessageHandler();
        this.permissionHandler = instance.getPermissionHandler();
    }

    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if(!(source instanceof Player)) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.NOCONSOLE, Language.ENGLISH)));
            return;
        }

        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        ICorePlayer corePlayer = corePlayerManager.getCorePlayer(((Player) source).getUniqueId());
        if(corePlayer == null) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.ERROR_COMMAND, Language.ENGLISH)));
            return;
        }
        Language language = corePlayer.getLanguage();

        if(args.length == 0) {
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.ONTIME_PLAYTIME_OWN, language)
            +" §8● §b"+corePlayer.getPlaytime().getTotalPlaytimeName());
            return;
        }

        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("top")) {
                if(!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_ONTIME_TOP))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                List<IOfflineCorePlayer> topPlaytimePlayers = corePlayerManager.getTopPlaytimePlayers(topPlayersAmount);
                int count = 1;
                corePlayer.sendMessage(" ");
                corePlayer.sendMessage(" ");
                corePlayer.sendMessage(" ");
                corePlayer.sendMessage("§8§m----------§r §7● §b"+languageHandler.getTranslatedMessage(Message.ONTIME_TOP_HEADER, language)
                        .replace("%REPLACE%", String.valueOf(topPlayersAmount))+" §7● §8§m----------");
                corePlayer.sendMessage(" ");

                for(IOfflineCorePlayer current : topPlaytimePlayers) {
                    switch (count) {
                        case 1:
                            corePlayer.sendMessage(" §8× §6➊ "+current.getRankPrefix()+current.getUsername()+" §8● §b"+current.getPlaytimeName());
                            break;
                        case 2:
                            corePlayer.sendMessage(" §8× §7➋ "+current.getRankPrefix()+current.getUsername()+" §8● §b"+current.getPlaytimeName());
                            break;
                        case 3:
                            corePlayer.sendMessage(" §8× §c➌ "+current.getRankPrefix()+current.getUsername()+" §8● §b"+current.getPlaytimeName());
                            break;
                        case 4:
                            corePlayer.sendMessage(" §8× §b➍ "+current.getRankPrefix()+current.getUsername()+" §8● §b"+current.getPlaytimeName());
                            break;
                        case 5:
                            corePlayer.sendMessage(" §8× §b➎ "+current.getRankPrefix()+current.getUsername()+" §8● §b"+current.getPlaytimeName());
                            break;
                        case 6:
                            corePlayer.sendMessage(" §8× §b➏ "+current.getRankPrefix()+current.getUsername()+" §8● §b"+current.getPlaytimeName());
                            break;
                        case 7:
                            corePlayer.sendMessage(" §8× §b➐ "+current.getRankPrefix()+current.getUsername()+" §8● §b"+current.getPlaytimeName());
                            break;
                        case 8:
                            corePlayer.sendMessage(" §8× §b➑ "+current.getRankPrefix()+current.getUsername()+" §8● §b"+current.getPlaytimeName());
                            break;
                        case 9:
                            corePlayer.sendMessage(" §8× §b➒ "+current.getRankPrefix()+current.getUsername()+" §8● §b"+current.getPlaytimeName());
                            break;
                        case 10:
                            corePlayer.sendMessage(" §8× §b➓ "+current.getRankPrefix()+current.getUsername()+" §8● §b"+current.getPlaytimeName());
                            break;
                        default:
                            break;
                    }
                    count++;
                }
                return;
            }
        }
        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("reset")) {
                if(!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_ONTIME_RESET))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
                if(targetPlayer != null) {
                    targetPlayer.resetPlaytime();
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.ONTIME_RESET, language)
                    .replace("%REPLACE%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()));
                    return;
                }
                IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[1]);
                if(offlineCorePlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                offlineCorePlayer.resetPlaytime();
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.ONTIME_RESET, language)
                .replace("%REPLACE%", offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername()));
                return;
            }
            if(args[0].equalsIgnoreCase("get")) {
                if(!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_ONTIME_GET))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
                if(targetPlayer != null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.ONTIME_PLAYTIME, language)
                    +" §8'"+targetPlayer.getRankPrefix()+targetPlayer.getUsername()+"§8' §8● §b"+targetPlayer.getPlaytime().getTotalPlaytimeName());
                    return;
                }
                IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[1]);
                if(offlineCorePlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.ONTIME_PLAYTIME, language)
                +" §8'"+offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername()+"§8' §8● §b"+offlineCorePlayer.getPlaytimeName());
                return;
            }
        }

        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                .replace("%REPLACE%", "proxy help"));
    }

}
