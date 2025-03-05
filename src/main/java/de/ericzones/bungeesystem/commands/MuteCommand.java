// Created by Eric B. 08.05.2021 19:44
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
import de.ericzones.bungeesystem.collectives.punish.IPunishManager;
import de.ericzones.bungeesystem.collectives.punish.ban.Ban;
import de.ericzones.bungeesystem.collectives.punish.ban.BanReason;
import de.ericzones.bungeesystem.collectives.punish.mute.Mute;
import de.ericzones.bungeesystem.collectives.punish.mute.MuteReason;
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

public class MuteCommand implements SimpleCommand {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;
    private final ChatMessageHandler chatMessageHandler;
    private final PermissionHandler permissionHandler;

    private CommandWindow commandWindow;
    
    public MuteCommand(BungeeSystem instance) {
        this.instance = instance;
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
        this.languageHandler = instance.getLanguageHandler();
        this.chatMessageHandler = instance.getChatMessageHandler();
        this.permissionHandler = instance.getPermissionHandler();
        registerCommandWindow();
    }

    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();
        String alias = invocation.alias();
        String[] args = invocation.arguments();

        if(!(source instanceof Player)) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.NOCONSOLE, Language.ENGLISH)));
            return;
        }

        IPunishManager punishManager = instance.getPunishManager();
        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        ICorePlayer corePlayer = corePlayerManager.getCorePlayer(((Player) source).getUniqueId());
        if(corePlayer == null) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.ERROR_COMMAND, Language.ENGLISH)));
            return;
        }
        Language language = corePlayer.getLanguage();

        if(args.length == 0) {
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                    .replace("%REPLACE%", "mute help"));
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
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                    return;
                }
            } catch (NumberFormatException e) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                return;
            }
            commandWindow.sendWindow(corePlayer, pageNumber);
            return;
        }

        if(alias.equalsIgnoreCase("mute")) {
            if (args[0].equalsIgnoreCase("list")) {
                if (!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_MUTE_LIST))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                if (args.length == 1) {
                    sendInfoWindow(corePlayer, 1);
                    return;
                }
                int pageNumber;
                try {
                    pageNumber = Integer.parseInt(args[1]);
                    if (pageNumber <= 0) {
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM) + "§7" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                        return;
                    }
                } catch (NumberFormatException e) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM) + "§7" + languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                    return;
                }
                sendInfoWindow(corePlayer, pageNumber);
                return;
            }
            if(args.length == 2) {
                if(args[0].equalsIgnoreCase("check")) {
                    if (!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_MUTE_CHECK))) {
                        corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                        return;
                    }
                    IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[1]);
                    if(offlineCorePlayer == null) {
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                        return;
                    }
                    if(!punishManager.isMuted(offlineCorePlayer.getUniqueID()) && !punishManager.hasMuteHistory(offlineCorePlayer.getUniqueID())) {
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.MUTE_CHECK_NOMUTES, language));
                        return;
                    }
                    TextComponent muteText = Component.text("§c"+languageHandler.getTranslatedMessage(Message.MUTE_CHECK_NOTMUTED, language));
                    if(punishManager.isMuted(offlineCorePlayer.getUniqueID())) {
                        Mute mute = punishManager.getMute(offlineCorePlayer.getUniqueID());
                        muteText = new TextBuilder("§c*Info*").setHoverText("§7"+languageHandler.getTranslatedMessage(Message.MUTE_USERNAME, language)+" §8● "
                                +mute.getOfflineCorePlayer().getRankPrefix()+mute.getOfflineCorePlayer().getUsername()+"\n§7"
                                +languageHandler.getTranslatedMessage(Message.MUTE_REASON, language)+" §8● §c"+mute.getReason().getName(language)+"\n§7"
                                +languageHandler.getTranslatedMessage(Message.MUTE_REMAININGTIME, language)+" §8● §c"+mute.getExpiryCurrentName(language)+"\n§7"
                                +languageHandler.getTranslatedMessage(Message.MUTE_TOTALTIME, language)+" §8● §c"+mute.getExpiryTotalName(language)+"\n§7"
                                +languageHandler.getTranslatedMessage(Message.MUTE_CREATOR, language)+" §8● "+mute.getCreatorOfflineCorePlayer().getRankPrefix()+mute.getCreatorOfflineCorePlayer().getUsername())
                                .setPreText("§c"+mute.getReason().getName(language)+" §8× §c"+mute.getExpiryCurrentName(language)+" §8● ").build();
                    }

                    corePlayer.sendMessage(" ");
                    corePlayer.sendMessage(" ");
                    corePlayer.sendMessage(" ");
                    corePlayer.sendMessage("§8§m------------§r §7● §cMute Check §7● §8§m------------");
                    corePlayer.sendMessage(" ");
                    corePlayer.sendMessage(" §8× §7"+languageHandler.getTranslatedMessage(Message.MUTE_USERNAME, language)+" §8● "+offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername());
                    corePlayer.sendMessage(Component.text(" §8× §7"+languageHandler.getTranslatedMessage(Message.MUTE_CHECK_STATUS, language)+" §8× ").append(muteText));

                    if(punishManager.hasMuteHistory(offlineCorePlayer.getUniqueID())) {
                        corePlayer.sendMessage(" ");
                        for(Mute current : punishManager.getMuteHistory(offlineCorePlayer.getUniqueID()).getMutes()) {
                            corePlayer.sendMessage(new TextBuilder("§c*Info*").setHoverText("§7"+languageHandler.getTranslatedMessage(Message.MUTE_USERNAME, language)+" §8● "
                                    +current.getOfflineCorePlayer().getRankPrefix()+current.getOfflineCorePlayer().getUsername()+"\n§7"
                                    +languageHandler.getTranslatedMessage(Message.MUTE_REASON, language)+" §8● §c"+current.getReason().getName(language)+"\n§7"
                                    +languageHandler.getTranslatedMessage(Message.MUTE_TOTALTIME, language)+" §8● §c"+current.getExpiryTotalName(language)+"\n§7"
                                    +languageHandler.getTranslatedMessage(Message.MUTE_CREATOR, language)+" §8● "+current.getCreatorOfflineCorePlayer().getRankPrefix()+current.getCreatorOfflineCorePlayer().getUsername())
                                    .setPreText(" §8× §c"+current.getReason().getName(language)+" §8× §c"+current.getExpiryTotalName(language)+" §8● ").build());
                        }
                    }
                    return;
                }
                if (!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_MUTE))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                IOfflineCorePlayer targetPlayer = corePlayerManager.getOfflineCorePlayer(args[0]);
                if(targetPlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                MuteReason reason = MuteReason.getReasonByString(args[1], language);
                if(reason == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.MUTE_REASON_NOTFOUND, language));
                    return;
                }
                if(targetPlayer.getUniqueID() == corePlayer.getUniqueID()) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.MUTE_SELF, language));
                    return;
                }
                if(targetPlayer.hasPermission(permissionHandler.getPermission(PermissionType.PUNISH_IGNOREMUTES))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.MUTE_TARGET_IGNORED, language));
                    return;
                }
                if(punishManager.isMuted(targetPlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.MUTE_ALREADY_MUTED, language));
                    return;
                }
                Mute mute = punishManager.initialMute(targetPlayer, reason, corePlayer.getUniqueID());
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.MUTE_MUTED, language)
                        .replace("%REPLACE%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()).replace("%REASON%", reason.getName(language)).replace("%DURATION%", mute.getExpiryTotalName(language)));
                return;
            }
        } else if(alias.equalsIgnoreCase("unmute")) {
            if(args.length == 1) {
                if (!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_UNMUTE))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[0]);
                if(offlineCorePlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                if(!punishManager.isMuted(offlineCorePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.MUTE_NOT_MUTED, language));
                    return;
                }
                punishManager.removeMute(punishManager.getMute(offlineCorePlayer.getUniqueID()));
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.MUTE_UNMUTED, language)
                        .replace("%REPLACE%", offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername()));
                return;
            }
        }

        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                .replace("%REPLACE%", "mute help"));
    }

    private void registerCommandWindow() {
        commandWindow = new CommandWindow("Mute Help", "§c", pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM), "mute help");

        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.MUTE_HELP_PLAYER, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.MUTE_HELP_PLAYER_HOVER, Language.GERMAN), "mute <Spieler> <Grund>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.MUTE_HELP_LIST, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.MUTE_HELP_LIST_HOVER, Language.GERMAN), "mute list");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.MUTE_HELP_CHECK, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.MUTE_HELP_CHECK_HOVER, Language.GERMAN), "mute check <Spieler>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.MUTE_HELP_UNMUTE, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.MUTE_HELP_UNMUTE_HOVER, Language.GERMAN), "unmute <Spieler>");

        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.MUTE_HELP_PLAYER, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.MUTE_HELP_PLAYER_HOVER, Language.ENGLISH), "mute <Player> <Reason>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.MUTE_HELP_LIST, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.MUTE_HELP_LIST_HOVER, Language.ENGLISH), "mute list");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.MUTE_HELP_CHECK, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.MUTE_HELP_CHECK_HOVER, Language.ENGLISH), "mute check <Player>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.MUTE_HELP_UNMUTE, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.MUTE_HELP_UNMUTE_HOVER, Language.ENGLISH), "unmute <Player>");

        commandWindow.initializeCommands();
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
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.MUTE_LIST_NONE, language));
            else
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
            return;
        }
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage("§8§m------------§r §7● §cMutelist §7● §8§m------------");
        corePlayer.sendMessage(" ");
        for(TextComponent current : infoCommands)
            corePlayer.sendMessage(current);
        corePlayer.sendMessage(" ");
        if(loadInfoWindow(language,(((page+1)-1)*5)+1, (page+1)*5).size() != 0) {
            if(page == 1) {
                TextComponent nextButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page+1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "mute list "+(page+1)).build();
                TextComponent text = Component.text("§7« §8[§c§l✘§r§8] §7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language)+" §8§l┃ ").append(nextButton).append(Component.text(" §8[§c" + (page + 1) + "§8] §7»"));
                corePlayer.sendMessage(text);
            } else {
                TextComponent nextButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page+1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "mute list "+(page+1)).build();
                TextComponent previousButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language))
                        .setHoverText("§8"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page-1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "mute list "+(page-1)).build();
                TextComponent text = Component.text("§7« §8[§c" + (page-1) + "§8] ").append(previousButton).append(Component.text(" §8§l┃ ")).append(nextButton).append(Component.text(" §8[§c" + (page+1) + "§8] §7»"));
                corePlayer.sendMessage(text);
            }
        } else {
            if(page == 1)
                corePlayer.sendMessage("§7« §8[§c§l✘§r§8] §7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language)
                        +" §8§l┃ §7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language)+" §8[§c§l✘§r§8] §7»");
            else {
                TextComponent previousButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language))
                        .setHoverText("§8"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page-1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "mute list "+(page-1)).build();
                TextComponent text = Component.text("§7« §8[§c" + (page - 1) + "§8] ").append(previousButton).append(Component.text(" §8§l┃ §7"
                        +languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language)+" §8[§c§l✘§r§8] §7»"));
                corePlayer.sendMessage(text);
            }
        }
    }

    private List<TextComponent> loadInfoWindow(Language language, int from, int to) {
        List<TextComponent> list = new ArrayList<>();
        List<TextComponent> mutes = getCurrentMutes(language);
        for(int i = from; i < to+1 && i < mutes.size(); i++)
            list.add(mutes.get(i));
        return list;
    }

    private List<TextComponent> getCurrentMutes(Language language) {
        List<TextComponent> currentMutes = new ArrayList<>();
        List<Mute> mutes = instance.getPunishManager().getGlobalMutes();

        String buttonTemplate = "§c*Info*";
        for(Mute current : mutes) {
            TextComponent button = new TextBuilder("§c"+languageHandler.getTranslatedMessage(Message.MUTE_EXPAND_BUTTON, language))
                    .setHoverText("§c"+languageHandler.getTranslatedMessage(Message.MUTE_EXPAND_BUTTON_HOVER, language))
                    .setClickEvent(TextBuilder.Action.COMMAND, "mute check "+current.getOfflineCorePlayer().getUsername()).build();
            TextComponent text = new TextBuilder(buttonTemplate).setHoverText("§7"+languageHandler.getTranslatedMessage(Message.MUTE_USERNAME, language)+" §8● "
                    +current.getOfflineCorePlayer().getRankPrefix()+current.getOfflineCorePlayer().getUsername()+"\n§7"
                    +languageHandler.getTranslatedMessage(Message.MUTE_REASON, language)+" §8● §c"+current.getReason().getName(language)+"\n§7"
                    +languageHandler.getTranslatedMessage(Message.MUTE_REMAININGTIME, language)+" §8● §c"+current.getExpiryCurrentName(language)+"\n§7"
                    +languageHandler.getTranslatedMessage(Message.MUTE_TOTALTIME, language)+" §8● §c"+current.getExpiryTotalName(language)+"\n§7"
                    +languageHandler.getTranslatedMessage(Message.MUTE_CREATOR, language)+" §8● "+current.getCreatorOfflineCorePlayer().getRankPrefix()+current.getCreatorOfflineCorePlayer().getUsername())
                    .setPreText(" §8× "+current.getOfflineCorePlayer().getRankPrefix()+current.getOfflineCorePlayer().getUsername()+" §8× §c"+current.getReason().getName(language)
                            +" §8● ").setPostText(" §8● ").build();
            currentMutes.add(text.append(button));
        }
        return currentMutes;
    }
    
}
