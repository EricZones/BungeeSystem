// Created by Eric B. 15.04.2021 11:51
package de.ericzones.bungeesystem.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.CorePlayerSwitchResult;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.joinme.Joinme;
import de.ericzones.bungeesystem.collectives.joinme.JoinmeManager;
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
import java.util.UUID;

public class JoinmeCommand implements SimpleCommand {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;
    private final ChatMessageHandler chatMessageHandler;
    private final PermissionHandler permissionHandler;

    public JoinmeCommand(BungeeSystem instance) {
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

        JoinmeManager joinmeManager = instance.getJoinmeManager();
        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        ICorePlayer corePlayer = corePlayerManager.getCorePlayer(((Player) source).getUniqueId());
        if(corePlayer == null) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.ERROR_COMMAND, Language.ENGLISH)));
            return;
        }
        Language language = corePlayer.getLanguage();

        if(args.length == 0) {
            if(!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_JOINME))) {
                corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                return;
            }
            if(joinmeManager.joinmeExists(corePlayer)) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.JOINME_PLAYER_EXISTS, language));
                return;
            }
            if(joinmeManager.joinmeExists(corePlayer.getConnectedCoreServer())) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.JOINME_SERVER_EXISTS, language));
                return;
            }
            joinmeManager.initialJoinme(corePlayer);
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.JOINME_CREATED, language));
            return;
        }

        if(args[0].equalsIgnoreCase("list")) {
            if(args.length == 1) {
                sendInfoWindow(corePlayer, 1);
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
            sendInfoWindow(corePlayer, pageNumber);
            return;
        }

        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("join")) {
                UUID uniqueId;
                try {
                    uniqueId = UUID.fromString(args[1]);
                } catch (IllegalArgumentException e) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.JOINME_NOTEXIST, language));
                    return;
                }

                if(!joinmeManager.joinmeExists(uniqueId)) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.JOINME_NOTFOUND, language));
                    return;
                }
                Joinme joinme = joinmeManager.getJoinmeById(uniqueId);
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_CONNECTING, language)
                .replace("%REPLACE%", joinme.getServerName()));
                CorePlayerSwitchResult result = corePlayer.sendToServer(joinme.getCoreServer());

                switch (result) {
                    case SERVERFULL:
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+
                                languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_FULL, language));
                        break;
                    case NOPLAYERKICKED:
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+
                                languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_NOPLAYERKICKED, language));
                        break;
                    case ALREADYCONNECTED:
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+
                                languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_ALREADYCONNECTED, language));
                        break;
                    case SERVERRESTRICTED:
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+
                                languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_RESTRICTED, language));
                        break;
                    case SERVERMAINTENANCE:
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+
                                languageHandler.getTranslatedMessage(Message.SERVER_SWITCHED_MAINTENANCE, language));
                        break;
                }
                return;
            }
        }

        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                .replace("%REPLACE%", "proxy help"));
    }

    private void sendInfoWindow(ICorePlayer corePlayer, int page) {
        Language language = corePlayer.getLanguage();
        List<TextComponent> infoCommands;
        if(page == 1)
            infoCommands = loadInfoWindow(language, 0, 5);
        else
            infoCommands = loadInfoWindow(language, ((page-1)*5)+1, page*5);
        if(infoCommands.size() == 0) {
            if(page == 1)
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.JOINME_LIST_NONE, language));
            else
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
            return;
        }
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage("§8§m------------§r §7● §bJoinmelist §7● §8§m------------");
        corePlayer.sendMessage(" ");
        for(TextComponent current : infoCommands)
            corePlayer.sendMessage(current);
        corePlayer.sendMessage(" ");
        if(loadInfoWindow(language,(((page+1)-1)*5)+1, (page+1)*5).size() != 0) {
            if(page == 1) {
                TextComponent nextButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page+1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "joinme list "+(page+1)).build();
                TextComponent text = Component.text("§7« §8[§c§l✘§r§8] §7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language)+" §8§l┃ ").append(nextButton).append(Component.text(" §8[§b" + (page + 1) + "§8] §7»"));
                corePlayer.sendMessage(text);
            } else {
                TextComponent nextButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page+1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "joinme list "+(page+1)).build();
                TextComponent previousButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language))
                        .setHoverText("§8"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page-1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "joinme list "+(page-1)).build();
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
                        .setClickEvent(TextBuilder.Action.COMMAND, "joinme list "+(page-1)).build();
                TextComponent text = Component.text("§7« §8[§b" + (page - 1) + "§8] ").append(previousButton).append(Component.text(" §8§l┃ §7"
                        +languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language)+" §8[§c§l✘§r§8] §7»"));
                corePlayer.sendMessage(text);
            }
        }
    }

    private List<TextComponent> loadInfoWindow(Language language, int from, int to) {
        List<TextComponent> list = new ArrayList<>();
        List<TextComponent> joinmes = getCurrentJoinmes(language);
        for(int i = from; i < to+1 && i < joinmes.size(); i++)
            list.add(joinmes.get(i));
        return list;
    }

    private List<TextComponent> getCurrentJoinmes(Language language) {
        List<TextComponent> currentJoinmes = new ArrayList<>();
        List<Joinme> joinmes = instance.getJoinmeManager().getActiveJoinmes();

        String buttonTemplate = "§b*Info*";
        for(Joinme current : joinmes) {
            TextComponent button = new TextBuilder("§a"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_BUTTON, language))
                    .setHoverText("§a"+languageHandler.getTranslatedMessage(Message.SERVER_SWITCH_BUTTON_HOVER, language))
                    .setClickEvent(TextBuilder.Action.COMMAND, "joinme join "+current.getUniqueId()).build();
            TextComponent text = new TextBuilder(buttonTemplate).setHoverText("§7"+languageHandler.getTranslatedMessage(Message.JOINME_USERNAME, language)+" §8● "
                    +current.getOfflineCorePlayerCreator().getRankPrefix()+current.getOfflineCorePlayerCreator().getUsername()+"\n§7"
                    +languageHandler.getTranslatedMessage(Message.JOINME_SERVER, language)+" §8● §b"+current.getServerName()+"\n§7"
                    +languageHandler.getTranslatedMessage(Message.JOINME_CREATIONTIME, language)+" §8● §b"+current.getCreationTimeName())
                    .setPreText(" §8× "+current.getOfflineCorePlayerCreator().getRankPrefix()+current.getOfflineCorePlayerCreator().getUsername()+" §8● ").setPostText(" §8● ").build();
            currentJoinmes.add(text.append(button));
        }
        return currentJoinmes;
    }

}
