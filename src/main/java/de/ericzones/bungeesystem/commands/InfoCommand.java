// Created by Eric B. 12.04.2021 13:36
package de.ericzones.bungeesystem.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.connection.PlayerVersion;
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

import java.util.UUID;

public class InfoCommand implements SimpleCommand {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;
    private final ChatMessageHandler chatMessageHandler;
    private final PermissionHandler permissionHandler;

    public InfoCommand(BungeeSystem instance) {
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

        if(!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_INFO))) {
            corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
            return;
        }

        if(args.length != 1) {
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                    .replace("%REPLACE%", "proxy help"));
            return;
        }

        ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[0]);
        if(targetPlayer == null) {
            IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[0]);
            if(offlineCorePlayer == null) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                return;
            }
            String targetUsername = offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername();
            UUID targetUuid = offlineCorePlayer.getUniqueID();
            String targetIpAddress = offlineCorePlayer.getIpAddress();
            Language targetLanguage = offlineCorePlayer.getLanguage();
            PlayerVersion targetVersion = offlineCorePlayer.getVersion();
            String targetRank = offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getRankName();
            String targetSkinValue = offlineCorePlayer.getSkinValue();
            int targetCoins = offlineCorePlayer.getCoins();
            String targetPlaytime = offlineCorePlayer.getPlaytimeName();
            String targetLogout = offlineCorePlayer.getLogoutDate();
            String targetCreationTime = offlineCorePlayer.getCreationTimeDate();

            corePlayer.sendMessage(" ");
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                    +languageHandler.getTranslatedMessage(Message.SPY_STATUS, language)+" §8● §c"+languageHandler.getTranslatedMessage(Message.SPY_STATUS_OFFLINE, language));
            TextBuilder copyButton = new TextBuilder("§a"+languageHandler.getTranslatedMessage(Message.SPY_COPY_BUTTON, language))
                    .setHoverText("§a"+languageHandler.getTranslatedMessage(Message.SPY_COPY_BUTTON_HOVER, language));
            corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
            +languageHandler.getTranslatedMessage(Message.SPY_USERNAME, language)+" §8● "+targetUsername+" §8● ")
            .setClickEvent(TextBuilder.Action.COPYTEXT, offlineCorePlayer.getUsername()).build());

            TextComponent text = new TextBuilder("§b"+languageHandler.getTranslatedMessage(Message.SPY_UUID_SHOW, language))
                    .setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7UUID §8● ")
                    .setPostText(" §8● ").setHoverText("§b"+targetUuid.toString()).build();
            corePlayer.sendMessage(text.append(copyButton.setClickEvent(TextBuilder.Action.COPYTEXT, String.valueOf(targetUuid)).setPreText(null).build()));

            corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
            +languageHandler.getTranslatedMessage(Message.SPY_IPADDRESS, language)+" §8● §b"+targetIpAddress+" §8● ")
            .setClickEvent(TextBuilder.Action.COPYTEXT, targetIpAddress).build());
            corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
            +languageHandler.getTranslatedMessage(Message.SPY_LANGUAGE, language)+" §8● §b"+targetLanguage.toString()+" §8● ")
            .setClickEvent(TextBuilder.Action.COPYTEXT, targetLanguage.toString()).build());
            corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
            +languageHandler.getTranslatedMessage(Message.SPY_VERSION, language)+" §8● §b"+targetVersion.getVersionName()+" §8● ")
            .setClickEvent(TextBuilder.Action.COPYTEXT, targetVersion.getVersionName()).build());
            corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
            +languageHandler.getTranslatedMessage(Message.SPY_RANK, language)+" §8● "+targetRank+" §8● ")
            .setClickEvent(TextBuilder.Action.COPYTEXT, offlineCorePlayer.getRankName()).build());

            text = new TextBuilder("§b"+languageHandler.getTranslatedMessage(Message.SPY_SKINVALUE_BUTTON, language))
                    .setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SPY_SKINVALUE, language)
                    +" §8● ").setPostText(" §8● ").setClickEvent(TextBuilder.Action.OPENLINK, targetSkinValue).build();
            corePlayer.sendMessage(text.append(copyButton.setClickEvent(TextBuilder.Action.COPYTEXT, targetSkinValue).setPreText(null).build()));

            corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
            +"Coins §8● §e"+targetCoins+" §8● ").setClickEvent(TextBuilder.Action.COPYTEXT, String.valueOf(targetCoins)).build());
            corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
            +languageHandler.getTranslatedMessage(Message.SPY_PLAYTIME, language)+" §8● §b"+targetPlaytime+" §8● ")
            .setClickEvent(TextBuilder.Action.COPYTEXT, targetPlaytime).build());
            corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
            +languageHandler.getTranslatedMessage(Message.SPY_LOGOUT, language)+" §8● §b"+targetLogout+" §8● ")
            .setClickEvent(TextBuilder.Action.COPYTEXT, targetLogout).build());
            corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
            +languageHandler.getTranslatedMessage(Message.SPY_CREATIONTIME, language)+" §8● §b"+targetCreationTime+" §8● ")
            .setClickEvent(TextBuilder.Action.COPYTEXT, targetCreationTime).build());
            return;
        }
        String targetUsername = targetPlayer.getRankPrefix()+targetPlayer.getUsername();
        UUID targetUuid = targetPlayer.getUniqueID();
        String targetIpAddress = targetPlayer.getConnectionInfo().getIpAddress();
        Language targetLanguage = targetPlayer.getLanguage();
        PlayerVersion targetVersion = targetPlayer.getConnectionInfo().getVersion();
        String targetRank = targetPlayer.getRankPrefix()+targetPlayer.getRankName();
        String targetSkinValue = targetPlayer.getSkinValue();
        int targetCoins = targetPlayer.getCoins().getCurrentCoins();
        String targetPlaytime = targetPlayer.getPlaytime().getTotalPlaytimeName();
        String targetCreationTime = targetPlayer.getCreationTimeDate();
        String targetConnectedServer = targetPlayer.getConnectedCoreServer().getServerName();
        String targetPing = targetPlayer.getPing();

        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                +languageHandler.getTranslatedMessage(Message.SPY_STATUS, language)+" §8● §a"+languageHandler.getTranslatedMessage(Message.SPY_STATUS_ONLINE, language));
        TextBuilder copyButton = new TextBuilder("§a"+languageHandler.getTranslatedMessage(Message.SPY_COPY_BUTTON, language))
                .setHoverText("§a"+languageHandler.getTranslatedMessage(Message.SPY_COPY_BUTTON_HOVER, language));
        corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                +languageHandler.getTranslatedMessage(Message.SPY_USERNAME, language)+" §8● "+targetUsername+" §8● ")
                .setClickEvent(TextBuilder.Action.COPYTEXT, targetPlayer.getUsername()).build());

        TextComponent text = new TextBuilder("§b"+languageHandler.getTranslatedMessage(Message.SPY_UUID_SHOW, language))
                .setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7UUID §8● ")
                .setPostText(" §8● ").setHoverText("§b"+targetUuid.toString()).build();
        corePlayer.sendMessage(text.append(copyButton.setClickEvent(TextBuilder.Action.COPYTEXT, String.valueOf(targetUuid)).setPreText(null).build()));

        corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                +languageHandler.getTranslatedMessage(Message.SPY_IPADDRESS, language)+" §8● §b"+targetIpAddress+" §8● ")
                .setClickEvent(TextBuilder.Action.COPYTEXT, targetIpAddress).build());
        corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                +languageHandler.getTranslatedMessage(Message.SPY_LANGUAGE, language)+" §8● §b"+targetLanguage.toString()+" §8● ")
                .setClickEvent(TextBuilder.Action.COPYTEXT, targetLanguage.toString()).build());
        corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                +languageHandler.getTranslatedMessage(Message.SPY_VERSION, language)+" §8● §b"+targetVersion.getVersionName()+" §8● ")
                .setClickEvent(TextBuilder.Action.COPYTEXT, targetVersion.getVersionName()).build());
        corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                +languageHandler.getTranslatedMessage(Message.SPY_RANK, language)+" §8● "+targetRank+" §8● ")
                .setClickEvent(TextBuilder.Action.COPYTEXT, targetPlayer.getRankName()).build());

        text = new TextBuilder("§b"+languageHandler.getTranslatedMessage(Message.SPY_SKINVALUE_BUTTON, language))
                .setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SPY_SKINVALUE, language)
                        +" §8● ").setPostText(" §8● ").setClickEvent(TextBuilder.Action.OPENLINK, targetSkinValue).build();
        corePlayer.sendMessage(text.append(copyButton.setClickEvent(TextBuilder.Action.COPYTEXT, targetSkinValue).setPreText(null).build()));

        corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                +"Coins §8● §e"+targetCoins+" §8● ").setClickEvent(TextBuilder.Action.COPYTEXT, String.valueOf(targetCoins)).build());
        corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                +languageHandler.getTranslatedMessage(Message.SPY_PLAYTIME, language)+" §8● §b"+targetPlaytime+" §8● ")
                .setClickEvent(TextBuilder.Action.COPYTEXT, targetPlaytime).build());
        corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                +languageHandler.getTranslatedMessage(Message.SPY_CREATIONTIME, language)+" §8● §b"+targetCreationTime+" §8● ")
                .setClickEvent(TextBuilder.Action.COPYTEXT, targetCreationTime).build());
        corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                +languageHandler.getTranslatedMessage(Message.SPY_CURRENTSERVER, language)+" §8● §b"+targetConnectedServer+" §8● ")
                .setClickEvent(TextBuilder.Action.COPYTEXT, targetConnectedServer).build());
        corePlayer.sendMessage(copyButton.setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"
                +"Ping §8● "+targetPing+" §8● ").setClickEvent(TextBuilder.Action.COPYTEXT, String.valueOf(targetPlayer.getProxyPlayer().getPing())).build());
    }

}
