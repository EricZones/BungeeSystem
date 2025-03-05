// Created by Eric B. 13.04.2021 12:12
package de.ericzones.bungeesystem.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
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

public class ChatclearCommand implements SimpleCommand {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;
    private final ChatMessageHandler chatMessageHandler;
    private final PermissionHandler permissionHandler;

    public ChatclearCommand(BungeeSystem instance) {
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

        if(!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_CHATCLEAR))) {
            corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
            return;
        }

        if(args.length != 1) {
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                    .replace("%REPLACE%", "proxy help"));
            return;
        }

        if(args[0].equalsIgnoreCase("me")) {
            for(int i = 0; i <= 150; i++)
                corePlayer.sendMessage(" ");
            return;
        }
        if(args[0].equalsIgnoreCase("server")) {
            for(ICorePlayer current : corePlayer.getConnectedCoreServer().getCorePlayers()) {
                for(int i = 0; i <= 150; i++)
                    current.sendMessage(" ");
                current.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.CHAT)+"§7"+languageHandler.getTranslatedMessage(Message.CHATCLEAR_MESSAGE, language)
                .replace("%REPLACE%", corePlayer.getRankPrefix()+corePlayer.getUsername()));
            }
            return;
        }
        if(args[0].equalsIgnoreCase("proxy")) {
            if(!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_CHATCLEAR_PROXY))) {
                corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                return;
            }
            for(ICorePlayer current : corePlayerManager.getCorePlayers()) {
                for(int i = 0; i <= 150; i++)
                    current.sendMessage(" ");
                current.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.CHAT)+"§7"+languageHandler.getTranslatedMessage(Message.CHATCLEAR_MESSAGE, language)
                        .replace("%REPLACE%", corePlayer.getRankPrefix()+corePlayer.getUsername()));
            }
            return;
        }

        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                .replace("%REPLACE%", "proxy help"));
    }

}
