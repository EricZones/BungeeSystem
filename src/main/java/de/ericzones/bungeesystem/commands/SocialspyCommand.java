// Created by Eric B. 01.05.2021 15:22
package de.ericzones.bungeesystem.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
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

public class SocialspyCommand implements SimpleCommand {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;
    private final ChatMessageHandler chatMessageHandler;
    private final PermissionHandler permissionHandler;

    public SocialspyCommand(BungeeSystem instance) {
        this.instance = instance;
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
        this.languageHandler = instance.getLanguageHandler();
        this.chatMessageHandler = instance.getChatMessageHandler();
        this.permissionHandler = instance.getPermissionHandler();
    }

    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();

        if(!(source instanceof Player)) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.NOCONSOLE, Language.ENGLISH)));
            return;
        }

        MsgManager msgManager = instance.getMsgManager();
        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        ICorePlayer corePlayer = corePlayerManager.getCorePlayer(((Player) source).getUniqueId());
        if(corePlayer == null) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.ERROR_COMMAND, Language.ENGLISH)));
            return;
        }
        Language language = corePlayer.getLanguage();

        if(!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_SOCIALSPY))) {
            corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
            return;
        }

        if(msgManager.getSpyPlayers().contains(corePlayer.getUniqueID())) {
            msgManager.getSpyPlayers().remove(corePlayer.getUniqueID());
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.MESSAGE)+"§7"+languageHandler.getTranslatedMessage(Message.MESSAGE_SOCIALSPY_DEACTIVATED, language));
        } else {
            msgManager.getSpyPlayers().add(corePlayer.getUniqueID());
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.MESSAGE)+"§7"+languageHandler.getTranslatedMessage(Message.MESSAGE_SOCIALSPY_ACTIVATED, language));
        }
    }

}
