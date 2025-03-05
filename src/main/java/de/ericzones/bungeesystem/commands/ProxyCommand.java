// Created by Eric B. 06.04.2021 16:21
package de.ericzones.bungeesystem.commands;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.CorePlayerSwitchResult;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.collectives.object.CommandWindow;
import de.ericzones.bungeesystem.collectives.object.TextBuilder;
import de.ericzones.bungeesystem.collectives.server.ICoreServer;
import de.ericzones.bungeesystem.collectives.server.ICoreServerManager;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.chatmessage.ChatMessageHandler;
import de.ericzones.bungeesystem.global.messaging.chatmessage.ChatMessageType;
import de.ericzones.bungeesystem.global.messaging.disconnectmessage.DisconnectMessageHandler;
import de.ericzones.bungeesystem.global.messaging.disconnectmessage.DisconnectMessageType;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixHandler;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;
import de.ericzones.bungeesystem.global.permission.PermissionHandler;
import de.ericzones.bungeesystem.global.permission.PermissionType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProxyCommand implements SimpleCommand {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;
    private final ChatMessageHandler chatMessageHandler;
    private final PermissionHandler permissionHandler;
    private final DisconnectMessageHandler disconnectMessageHandler;

    private CommandWindow commandWindow;

    public ProxyCommand(BungeeSystem instance) {
        this.instance = instance;
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
        this.languageHandler = instance.getLanguageHandler();
        this.chatMessageHandler = instance.getChatMessageHandler();
        this.permissionHandler = instance.getPermissionHandler();
        this.disconnectMessageHandler = instance.getDisconnectMessageHandler();
        registerCommandWindow();
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if(!(source instanceof Player)) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.NOCONSOLE, Language.ENGLISH)));
            return;
        }

        ICoreServerManager coreServerManager = instance.getCoreServerManager();
        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        ICorePlayer corePlayer = corePlayerManager.getCorePlayer(((Player) source).getUniqueId());
        if(corePlayer == null) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.ERROR_COMMAND, Language.ENGLISH)));
            return;
        }
        Language language = corePlayer.getLanguage();

        if(!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_PROXY))) {
            corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
            return;
        }

        if(args.length == 0) {
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7Version §8● §bZonesProxy "+instance.getVersion());
            return;
        }

        if(args[0].equalsIgnoreCase("target")) {
            if(args.length == 3) {
                ICoreServer coreServer = coreServerManager.getCoreServer(args[1]);
                if (coreServer == null) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOSERVER, language));
                    return;
                }
                if(args[2].equalsIgnoreCase("stop")) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_STOPPED, language)
                    .replace("%REPLACE%", coreServer.getServerName()));
                    coreServer.stopServer(null);
                    return;
                } else if(args[2].equalsIgnoreCase("info")) {
                    ServiceInfoSnapshot serviceInfo = coreServer.getServiceInfoSnapshot();
                    if(serviceInfo == null) {
                        corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.ERROR_COMMAND, language));
                        return;
                    }
                    int cpuUsage = (int) serviceInfo.getProcessSnapshot().getCpuUsage();
                    int memoryUsage = (int) ((serviceInfo.getProcessSnapshot().getHeapUsageMemory()*100) / serviceInfo.getProcessSnapshot().getMaxHeapMemory());
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_INFO_USAGE, language)
                            +" §b"+coreServer.getServerName()+" §8● §7CPU §b"+cpuUsage+"% §8● §7RAM §b"+memoryUsage+"%");
                    return;
                }
            }
            if (args.length == 4) {
                ICoreServer coreServer = coreServerManager.getCoreServer(args[1]);
                if (coreServer == null) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOSERVER, language));
                    return;
                }
                String field = args[2];
                if (field.equalsIgnoreCase("whitelist")) {
                    if (args[3].equalsIgnoreCase("on")) {
                        if (coreServer.isRestricted()) {
                            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY) + "§7" + languageHandler.getTranslatedMessage(Message.SERVER_ALREADY_RESTRICTED, language));
                            return;
                        }
                        if (coreServer.isInMaintenance()) {
                            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY) + "§7" + languageHandler.getTranslatedMessage(Message.SERVER_CURRENTLY_MAINTENANCE, language));
                            return;
                        }
                        coreServer.setRestricted(true);
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY) + "§7" + languageHandler.getTranslatedMessage(Message.SERVER_RESTRICTED_ACTIVATED, language)
                                .replace("%REPLACE%", coreServer.getServerName()));
                        return;
                    } else if (args[3].equalsIgnoreCase("off")) {
                        if (!coreServer.isRestricted()) {
                            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY) + "§7" + languageHandler.getTranslatedMessage(Message.SERVER_NOT_RESTRICTED, language));
                            return;
                        }
                        coreServer.setRestricted(false);
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY) + "§7" + languageHandler.getTranslatedMessage(Message.SERVER_RESTRICTED_DEACTIVATED, language)
                                .replace("%REPLACE%", coreServer.getServerName()));
                        return;
                    }
                }
                if (field.equalsIgnoreCase("maintenance")) {
                    if (args[3].equalsIgnoreCase("on")) {
                        if (coreServer.isInMaintenance()) {
                            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY) + "§7" + languageHandler.getTranslatedMessage(Message.SERVER_ALREADY_MAINTENANCE, language));
                            return;
                        }
                        coreServer.setMaintenance(true);
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY) + "§7" + languageHandler.getTranslatedMessage(Message.SERVER_MAINTENANCE_ACTIVATED, language)
                                .replace("%REPLACE%", coreServer.getServerName()));
                        return;
                    } else if (args[3].equalsIgnoreCase("off")) {
                        if (!coreServer.isInMaintenance()) {
                            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY) + "§7" + languageHandler.getTranslatedMessage(Message.SERVER_NOT_MAINTENANCE, language));
                            return;
                        }
                        coreServer.setMaintenance(false);
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY) + "§7" + languageHandler.getTranslatedMessage(Message.SERVER_MAINTENANCE_DEACTIVATED, language)
                                .replace("%REPLACE%", coreServer.getServerName()));
                        return;
                    }
                }
            }
            if(args.length == 5) {
                ICoreServer coreServer = coreServerManager.getCoreServer(args[1]);
                if (coreServer == null) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOSERVER, language));
                    return;
                }
                String field = args[2];
                if(field.equalsIgnoreCase("whitelist")) {
                    IOfflineCorePlayer targetPlayer = corePlayerManager.getOfflineCorePlayer(args[4]);
                    if(targetPlayer == null) {
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                        return;
                    }
                    if(!coreServer.isRestricted()) {
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_NOT_RESTRICTED, language));
                        return;
                    }
                    if(args[3].equalsIgnoreCase("add")) {
                        if(coreServer.getWhitelist().containsUUID(targetPlayer.getUniqueID())) {
                            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_TARGET_ALREADY_WHITELISTED, language));
                            return;
                        }
                        coreServer.getWhitelist().addToWhitelist(targetPlayer.getUniqueID());
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_TARGET_WHITELISTED, language)
                                .replace("%PLAYER%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()).replace("%REPLACE%", coreServer.getServerName()));
                        return;
                    } else if(args[3].equalsIgnoreCase("remove")) {
                        if(!coreServer.getWhitelist().containsUUID(targetPlayer.getUniqueID())) {
                            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_TARGET_NOT_WHITELISTED, language));
                            return;
                        }
                        coreServer.getWhitelist().removeFromWhitelist(targetPlayer.getUniqueID());
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_TARGET_UNWHITELISTED, language)
                                .replace("%PLAYER%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()).replace("%REPLACE%", coreServer.getServerName()));
                        return;
                    }
                }
            }
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                    .replace("%REPLACE%", "proxy help"));
            return;
        }
        if(args[0].equalsIgnoreCase("server")) {
            List<String> serverNames = new ArrayList<>();
            for(ICoreServer current : coreServerManager.getCoreServers())
                serverNames.add(current.getServerName());
            Collections.sort(serverNames);

            List<TextComponent> serverText = new ArrayList<>();
            for(int i = 0; i < serverNames.size(); i++) {
                TextComponent text = new TextBuilder("§a"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_BUTTON, language))
                        .setHoverText("§a"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_BUTTON_HOVER, language))
                        .setClickEvent(TextBuilder.Action.COMMAND, "proxy connect "+serverNames.get(i))
                        .setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§b"+serverNames.get(i)+
                                " §8● §a"+coreServerManager.getCoreServer(serverNames.get(i)).getCorePlayerCount()+"§7/"+
                                coreServerManager.getCoreServer(serverNames.get(i)).getMaxPlayerCount()+" §8● ").build();
                serverText.add(text);
            }

            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY));
            serverText.forEach(corePlayer::sendMessage);
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY));
            return;
        }
        if(args[0].equalsIgnoreCase("stop")) {
            for(ICorePlayer current : corePlayerManager.getCorePlayers())
                current.disconnect(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.DISCONNECTED,
                        languageHandler.getTranslatedMessage(Message.PROXY_RESTARTING, current.getLanguage()), "", current.getLanguage()));
            instance.getProxyServer().shutdown();
            return;
        }
        if(args[0].equalsIgnoreCase("broadcast") || args[0].equalsIgnoreCase("bc")) {
            if(args.length < 2) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                .replace("%REPLACE%", "proxy help"));
                return;
            }
            String message = "";
            for(int i = 1; i < args.length; i++)
                message += args[i] + " ";
            message = message.trim();
            message = translateColorCodes(message);
            for(ICorePlayer current : corePlayerManager.getCorePlayers()) {
                current.sendMessage(" ");
                current.sendMessage(" §8[§c!§8] §4§l"+languageHandler.getTranslatedMessage(Message.PROXY_BROADCAST, current.getLanguage())+"§r §8● §7"+message);
                current.sendMessage(" ");
            }
            return;
        }
        if(args[0].equalsIgnoreCase("connect")) {
            if(args.length != 2) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                        .replace("%REPLACE%", "proxy help"));
                return;
            }
            ICoreServer coreServer = coreServerManager.getCoreServer(args[1]);
            if(coreServer == null) {
                corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOSERVER, language));
                return;
            }
            if(coreServer.containsCorePlayer(corePlayer)) {
                corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.ALREADYCONNECTED, language));
                return;
            }
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_CONNECTING, language)
            .replace("%REPLACE%", coreServer.getServerName()));
            CorePlayerSwitchResult result = corePlayer.sendToServer(coreServer);
            if(result == CorePlayerSwitchResult.NOPLAYERKICKED)
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_NOPLAYERKICKED, language));
            return;
        }
        if(args[0].equalsIgnoreCase("send")) {
            if(args.length != 3) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                        .replace("%REPLACE%", "proxy help"));
                return;
            }
            if(corePlayerManager.getCorePlayer(args[1]) == null && coreServerManager.getCoreServer(args[1]) == null
                    && !args[1].equalsIgnoreCase("@a") && !args[1].equalsIgnoreCase("@all")) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                return;
            }
            ICoreServer coreServer = coreServerManager.getCoreServer(args[2]);
            if(coreServer == null) {
                corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOSERVER, language));
                return;
            }
            ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
            ICoreServer targetServer = coreServerManager.getCoreServer(args[1]);

            if(args[1].equalsIgnoreCase("@all") || args[1].equalsIgnoreCase("@a")) {
                List<ICorePlayer> corePlayers = corePlayerManager.getCorePlayers();
                int count = 0;
                for(ICorePlayer current : corePlayers) {
                    if(current.getConnectedCoreServer() != coreServer) {
                        if(current.sendToServer(coreServer) == CorePlayerSwitchResult.SUCCESS);
                            count++;
                    }
                }
                if(count == 1)
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_SENT_SINGLE, language)
                            .replace("%REPLACE%", coreServer.getServerName()));
                else
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_SENT_MULTIPLE, language)
                            .replace("%REPLACE%", coreServer.getServerName()).replace("%COUNT%", String.valueOf(count)));
                return;
            }
            if(targetPlayer != null) {
                if(targetPlayer.getConnectedCoreServer() == coreServer) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.TARGETALREADYCONNECTED, language));
                    return;
                }
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_CONNECTING_OTHER, language)
                .replace("%PLAYER%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()).replace("%REPLACE%", coreServer.getServerName()));
                targetPlayer.sendToServerAsAdmin(coreServer);
                return;
            }
            if(targetServer != null) {
                if(targetServer == coreServer) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+
                            languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_SENT_ALREADYCONNECTED, language));
                    return;
                }
                int count = 0;
                for(ICorePlayer current : targetServer.getCorePlayers()) {
                    if(current.sendToServer(coreServer) == CorePlayerSwitchResult.SUCCESS);
                        count++;
                }
                if(count == 1)
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_SENT_SINGLE, language)
                            .replace("%REPLACE%", coreServer.getServerName()));
                else
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_SENT_MULTIPLE, language)
                            .replace("%REPLACE%", coreServer.getServerName()).replace("%COUNT%", String.valueOf(count)));
                return;
            }
            return;
        }
        if(args[0].equalsIgnoreCase("jump") || args[0].equalsIgnoreCase("jumpto")) {
            if(args.length != 2) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                        .replace("%REPLACE%", "proxy help"));
                return;
            }
            ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
            if(targetPlayer == null) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                return;
            }
            if(targetPlayer.getConnectedCoreServer() == corePlayer.getConnectedCoreServer()) {
                corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.ALREADYCONNECTED, language));
                return;
            }
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_CONNECTING, language)
                    .replace("%REPLACE%", targetPlayer.getConnectedCoreServer().getServerName()));
            CorePlayerSwitchResult result = corePlayer.sendToServer(targetPlayer.getConnectedCoreServer());
            if(result == CorePlayerSwitchResult.NOPLAYERKICKED)
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_NOPLAYERKICKED, language));
            return;
        }
        if(args[0].equalsIgnoreCase("me")) {
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_CURRENTCONNECTED, language)
            +" §8● §b"+corePlayer.getConnectedCoreServer().getServerName());
            return;
        }
        if(args[0].equalsIgnoreCase("search") || args[0].equalsIgnoreCase("find")) {
            if(args.length != 2) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                        .replace("%REPLACE%", "proxy help"));
                return;
            }
            ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
            if(targetPlayer == null) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                return;
            }
            TextComponent text = new TextBuilder("§a"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_BUTTON, language))
                    .setHoverText("§a"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_BUTTON_HOVER, language))
                    .setClickEvent(TextBuilder.Action.COMMAND, "proxy connect "+targetPlayer.getConnectedCoreServer().getServerName())
                    .setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§8'"+targetPlayer.getRankPrefix()+targetPlayer.getUsername()+
                            "§8' §8● §b"+targetPlayer.getConnectedCoreServer().getServerName()+" §8● ").build();
            corePlayer.sendMessage(text);
            return;
        }
        if(args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("online")) {
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_INFO_PLAYER, language)
            +" §8● §a"+corePlayerManager.getOnlinePlayerAmount()+" §8● §7"+
                    languageHandler.getTranslatedMessage(Message.PROXY_INFO_SERVER, language)+" §8● §b"+coreServerManager.getCoreServerAmount());
            return;
        }
        if(args[0].equalsIgnoreCase("info")) {
            ServiceInfoSnapshot serviceInfo = CloudNetDriver.getInstance().getCloudServiceProvider().getCloudServiceByName("Proxy-1");
            if(serviceInfo == null) {
                corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.ERROR_COMMAND, language));
                return;
            }
            int cpuUsage = (int) serviceInfo.getProcessSnapshot().getCpuUsage();
            int memoryUsage = (int) ((serviceInfo.getProcessSnapshot().getHeapUsageMemory()*100) / serviceInfo.getProcessSnapshot().getMaxHeapMemory());
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_INFO_USAGE, language)
            +" §8● §7CPU §b"+cpuUsage+"% §8● §7RAM §b"+memoryUsage+"%");
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
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                    return;
                }
            } catch (NumberFormatException e) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                return;
            }
            commandWindow.sendWindow(corePlayer, pageNumber);
            return;
        }

        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                .replace("%REPLACE%", "proxy help"));
    }

    private void registerCommandWindow() {
        commandWindow = new CommandWindow("Proxy Help", "§b", pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY), "proxy help");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PROXY_HELP_SERVER, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_SERVER_HOVER, Language.GERMAN), "proxy server");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PROXY_HELP_CONNECT, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_CONNECT_HOVER, Language.GERMAN), "proxy connect <Server>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PROXY_HELP_SEND, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_SEND_HOVER, Language.GERMAN), "proxy send <Spieler> <Server>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PROXY_HELP_BROADCAST, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_BROADCAST_HOVER, Language.GERMAN), "proxy broadcast <Nachricht>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PROXY_HELP_ME, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_ME_HOVER, Language.GERMAN), "proxy me");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PROXY_HELP_FIND, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_FIND_HOVER, Language.GERMAN), "proxy find <Spieler>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PROXY_HELP_JUMP, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_JUMP_HOVER, Language.GERMAN), "proxy jump <Spieler>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PROXY_HELP_LIST, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_LIST_HOVER, Language.GERMAN), "proxy list");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PROXY_HELP_INFO, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_INFO_HOVER, Language.GERMAN), "proxy info");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PROXY_HELP_STOP, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_STOP_HOVER, Language.GERMAN), "proxy stop");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.PROXY_HELP_TARGET, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_TARGET_HOVER, Language.GERMAN), "proxy target <Server> <Bereich> <Aktion>");

        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PROXY_HELP_SERVER, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_SERVER_HOVER, Language.ENGLISH), "proxy server");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PROXY_HELP_CONNECT, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_CONNECT_HOVER, Language.ENGLISH), "proxy connect <Server>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PROXY_HELP_SEND, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_SEND_HOVER, Language.ENGLISH), "proxy send <Player> <Server>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PROXY_HELP_BROADCAST, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_BROADCAST_HOVER, Language.ENGLISH), "proxy broadcast <Message>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PROXY_HELP_ME, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_ME_HOVER, Language.ENGLISH), "proxy me");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PROXY_HELP_FIND, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_FIND_HOVER, Language.ENGLISH), "proxy find <Player>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PROXY_HELP_JUMP, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_JUMP_HOVER, Language.ENGLISH), "proxy jump <Player>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PROXY_HELP_LIST, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_LIST_HOVER, Language.ENGLISH), "proxy list");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PROXY_HELP_INFO, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_INFO_HOVER, Language.ENGLISH), "proxy info");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PROXY_HELP_STOP, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_STOP_HOVER, Language.ENGLISH), "proxy stop");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.PROXY_HELP_TARGET, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.PROXY_HELP_TARGET_HOVER, Language.ENGLISH), "proxy target <Server> <Field> <Action>");

        commandWindow.initializeCommands();
    }

    private String translateColorCodes(String message) {
        String msg = message;
        msg = msg.replace("&0", "§0");
        msg = msg.replace("&1", "§1");
        msg = msg.replace("&2", "§2");
        msg = msg.replace("&3", "§3");
        msg = msg.replace("&4", "§4");
        msg = msg.replace("&5", "§5");
        msg = msg.replace("&6", "§6");
        msg = msg.replace("&7", "§7");
        msg = msg.replace("&8", "§8");
        msg = msg.replace("&9", "§9");
        msg = msg.replace("&a", "§a");
        msg = msg.replace("&b", "§b");
        msg = msg.replace("&c", "§c");
        msg = msg.replace("&d", "§d");
        msg = msg.replace("&e", "§e");
        msg = msg.replace("&f", "§f");
        msg = msg.replace("&r", "§r");
        msg = msg.replace("&k", "§k");
        msg = msg.replace("&m", "§m");
        msg = msg.replace("&n", "§n");
        msg = msg.replace("&l", "§l");
        msg = msg.replace("&o", "§o");
        return msg;
    }

}
