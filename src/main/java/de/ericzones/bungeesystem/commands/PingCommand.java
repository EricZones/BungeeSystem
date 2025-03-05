// Created by Eric B. 11.04.2021 22:49
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

public class PingCommand implements SimpleCommand {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;
    private final ChatMessageHandler chatMessageHandler;
    private final PermissionHandler permissionHandler;

    public PingCommand(BungeeSystem instance) {
        this.instance = instance;
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
        this.languageHandler = instance.getLanguageHandler();
        this.chatMessageHandler = instance.getChatMessageHandler();
        this.permissionHandler = instance.getPermissionHandler();
    }

    @Override
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
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_INFO_PING, language)
            +" §8● "+corePlayer.getPing());
            return;
        }

        if(args.length == 1) {
            if(!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_PING_OTHER))) {
                corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                return;
            }
            ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[0]);
            if(targetPlayer == null) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                return;
            }
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7Ping §8'"+targetPlayer.getRankPrefix()+targetPlayer.getUsername()+"§8' §8● "+targetPlayer.getPing());
            return;
        }

        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                .replace("%REPLACE%", "proxy help"));
    }

}
