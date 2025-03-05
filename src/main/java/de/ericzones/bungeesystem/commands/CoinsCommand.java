// Created by Eric B. 14.04.2021 18:56
package de.ericzones.bungeesystem.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.collectives.object.CommandWindow;
import de.ericzones.bungeesystem.collectives.object.TextBuilder;
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

public class CoinsCommand implements SimpleCommand {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;
    private final ChatMessageHandler chatMessageHandler;
    private final PermissionHandler permissionHandler;

    private CommandWindow commandWindow;

    public CoinsCommand(BungeeSystem instance) {
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

        if (!(source instanceof Player)) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.NOCONSOLE, Language.ENGLISH)));
            return;
        }

        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        ICorePlayer corePlayer = corePlayerManager.getCorePlayer(((Player) source).getUniqueId());
        if (corePlayer == null) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.ERROR_COMMAND, Language.ENGLISH)));
            return;
        }
        Language language = corePlayer.getLanguage();

        if (args.length == 0) {
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7" + languageHandler.getTranslatedMessage(Message.COINS_OWN, language)
                    + " §8● §e" + corePlayer.getCoins().getCurrentCoins());
            return;
        }

        if (args[0].equalsIgnoreCase("help")) {
            if (args.length == 1) {
                commandWindow.sendWindow(corePlayer, 1);
                return;
            }
            int pageNumber;
            try {
                pageNumber = Integer.parseInt(args[1]);
                if (pageNumber <= 0) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                    return;
                }
            } catch (NumberFormatException e) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                return;
            }
            commandWindow.sendWindow(corePlayer, pageNumber);
            return;
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("get")) {
                if (!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_COINS_GET))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
                if (targetPlayer != null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7Coins"
                            + " §8'" + targetPlayer.getRankPrefix() + targetPlayer.getUsername() + "§8' §8● §e" + targetPlayer.getCoins().getCurrentCoins());
                    return;
                }
                IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[1]);
                if (offlineCorePlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7Coins"
                        + " §8'" + offlineCorePlayer.getRankPrefix() + offlineCorePlayer.getUsername() + "§8' §8● §e" + offlineCorePlayer.getCoins());
                return;
            }
            if (args[0].equalsIgnoreCase("reset")) {
                if (!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_COINS_ADMIN))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
                if (targetPlayer != null) {
                    targetPlayer.getCoins().resetCoins();
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7" + languageHandler.getTranslatedMessage(Message.COINS_RESET, language)
                            .replace("%REPLACE%", targetPlayer.getRankPrefix() + targetPlayer.getUsername()));
                    return;
                }
                IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[1]);
                if (offlineCorePlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                offlineCorePlayer.resetCoins();
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7" + languageHandler.getTranslatedMessage(Message.COINS_RESET, language)
                        .replace("%REPLACE%", offlineCorePlayer.getRankPrefix() + offlineCorePlayer.getUsername()));
                return;
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("set")) {
                if (!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_COINS_ADMIN))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                int coins;
                try {
                    coins = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7"
                            + languageHandler.getTranslatedMessage(Message.COINS_ERROR_NUMBER, language));
                    return;
                }
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
                if (targetPlayer != null) {
                    targetPlayer.getCoins().setCoins(coins);
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7" + languageHandler.getTranslatedMessage(Message.COINS_SET, language)
                            .replace("%REPLACE%", targetPlayer.getRankPrefix() + targetPlayer.getUsername()).replace("%COINS%", String.valueOf(coins)));
                    return;
                }
                IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[1]);
                if (offlineCorePlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                offlineCorePlayer.setCoins(coins);
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7" + languageHandler.getTranslatedMessage(Message.COINS_SET, language)
                        .replace("%REPLACE%", offlineCorePlayer.getRankPrefix() + offlineCorePlayer.getUsername()).replace("%COINS%", String.valueOf(coins)));
                return;
            }
            if (args[0].equalsIgnoreCase("add")) {
                if (!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_COINS_ADMIN))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                int coins;
                try {
                    coins = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7"
                            + languageHandler.getTranslatedMessage(Message.COINS_ERROR_NUMBER, language));
                    return;
                }
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
                if (targetPlayer != null) {
                    targetPlayer.getCoins().addCoins(coins);
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7" + languageHandler.getTranslatedMessage(Message.COINS_ADD, language)
                            .replace("%REPLACE%", targetPlayer.getRankPrefix() + targetPlayer.getUsername()).replace("%COINS%", String.valueOf(coins)));
                    return;
                }
                IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[1]);
                if (offlineCorePlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                offlineCorePlayer.addCoins(coins);
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7" + languageHandler.getTranslatedMessage(Message.COINS_ADD, language)
                        .replace("%REPLACE%", offlineCorePlayer.getRankPrefix() + offlineCorePlayer.getUsername()).replace("%COINS%", String.valueOf(coins)));
                return;
            }
            if (args[0].equalsIgnoreCase("remove")) {
                if (!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_COINS_ADMIN))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                int coins;
                try {
                    coins = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7"
                            + languageHandler.getTranslatedMessage(Message.COINS_ERROR_NUMBER, language));
                    return;
                }
                ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[1]);
                if (targetPlayer != null) {
                    targetPlayer.getCoins().removeCoins(coins);
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7" + languageHandler.getTranslatedMessage(Message.COINS_REMOVE, language)
                            .replace("%REPLACE%", targetPlayer.getRankPrefix() + targetPlayer.getUsername()).replace("%COINS%", String.valueOf(coins)));
                    return;
                }
                IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[1]);
                if (offlineCorePlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                offlineCorePlayer.removeCoins(coins);
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7" + languageHandler.getTranslatedMessage(Message.COINS_REMOVE, language)
                        .replace("%REPLACE%", offlineCorePlayer.getRankPrefix() + offlineCorePlayer.getUsername()).replace("%COINS%", String.valueOf(coins)));
                return;
            }
        }

        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM) + "§7" + languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                .replace("%REPLACE%", "coins help"));
    }
    
    private void registerCommandWindow() {
        commandWindow = new CommandWindow("Coins Help", "§e", pluginPrefixHandler.getPluginPrefix(PluginPrefixType.COINSYSTEM), "coins help");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.COINS_HELP_GET, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.COINS_HELP_GET_HOVER, Language.GERMAN), "coins get <Spieler>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.COINS_HELP_RESET, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.COINS_HELP_RESET_HOVER, Language.GERMAN), "coins reset <Spieler>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.COINS_HELP_SET, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.COINS_HELP_SET_HOVER, Language.GERMAN), "coins set <Spieler> <Anzahl>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.COINS_HELP_ADD, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.COINS_HELP_ADD_HOVER, Language.GERMAN), "coins add <Spieler> <Anzahl>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.COINS_HELP_REMOVE, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.COINS_HELP_REMOVE_HOVER, Language.GERMAN), "coins remove <Spieler> <Anzahl>");

        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.COINS_HELP_GET, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.COINS_HELP_GET_HOVER, Language.ENGLISH), "coins get <Player>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.COINS_HELP_RESET, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.COINS_HELP_RESET_HOVER, Language.ENGLISH), "coins reset <Player>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.COINS_HELP_SET, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.COINS_HELP_SET_HOVER, Language.ENGLISH), "coins set <Player> <Number>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.COINS_HELP_ADD, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.COINS_HELP_ADD_HOVER, Language.ENGLISH), "coins add <Player> <Number>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.COINS_HELP_REMOVE, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.COINS_HELP_REMOVE_HOVER, Language.ENGLISH), "coins remove <Player> <Number>");

        commandWindow.initializeCommands();
    }

}
