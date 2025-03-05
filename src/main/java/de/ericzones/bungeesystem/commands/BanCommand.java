// Created by Eric B. 06.05.2021 11:13
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
import de.ericzones.bungeesystem.collectives.report.Report;
import de.ericzones.bungeesystem.collectives.report.ReportReason;
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

public class BanCommand implements SimpleCommand {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;
    private final ChatMessageHandler chatMessageHandler;
    private final PermissionHandler permissionHandler;

    private CommandWindow commandWindow;

    public BanCommand(BungeeSystem instance) {
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
                    .replace("%REPLACE%", "ban help"));
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

        if (alias.equalsIgnoreCase("ban")) {
            if (args[0].equalsIgnoreCase("list")) {
                if (!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_BAN_LIST))) {
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
                    if (!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_BAN_CHECK))) {
                        corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                        return;
                    }
                    IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[1]);
                    if(offlineCorePlayer == null) {
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                        return;
                    }
                    if(!punishManager.isBanned(offlineCorePlayer.getUniqueID()) && !punishManager.hasBanHistory(offlineCorePlayer.getUniqueID())) {
                        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.BAN_CHECK_NOBANS, language));
                        return;
                    }
                    TextComponent banText = Component.text("§c"+languageHandler.getTranslatedMessage(Message.BAN_CHECK_NOTBANNED, language));
                    if(punishManager.isBanned(offlineCorePlayer.getUniqueID())) {
                        Ban ban = punishManager.getBan(offlineCorePlayer.getUniqueID());
                        banText = new TextBuilder("§c*Info*").setHoverText("§7"+languageHandler.getTranslatedMessage(Message.BAN_USERNAME, language)+" §8● "
                                +ban.getOfflineCorePlayer().getRankPrefix()+ban.getOfflineCorePlayer().getUsername()+"\n§7"
                                +languageHandler.getTranslatedMessage(Message.BAN_REASON, language)+" §8● §c"+ban.getReason().getName(language)+"\n§7"
                                +languageHandler.getTranslatedMessage(Message.BAN_REMAININGTIME, language)+" §8● §c"+ban.getExpiryCurrentName(language)+"\n§7"
                                +languageHandler.getTranslatedMessage(Message.BAN_TOTALTIME, language)+" §8● §c"+ban.getExpiryTotalName(language)+"\n§7"
                                +languageHandler.getTranslatedMessage(Message.BAN_CREATOR, language)+" §8● "+ban.getCreatorOfflineCorePlayer().getRankPrefix()+ban.getCreatorOfflineCorePlayer().getUsername())
                                .setPreText("§c"+ban.getReason().getName(language)+" §8× §c"+ban.getExpiryCurrentName(language)+" §8● ").build();
                    }

                    corePlayer.sendMessage(" ");
                    corePlayer.sendMessage(" ");
                    corePlayer.sendMessage(" ");
                    corePlayer.sendMessage("§8§m------------§r §7● §cBan Check §7● §8§m------------");
                    corePlayer.sendMessage(" ");
                    corePlayer.sendMessage(" §8× §7"+languageHandler.getTranslatedMessage(Message.BAN_USERNAME, language)+" §8● "+offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername());
                    corePlayer.sendMessage(Component.text(" §8× §7"+languageHandler.getTranslatedMessage(Message.BAN_CHECK_STATUS, language)+" §8× ").append(banText));

                    if(punishManager.hasBanHistory(offlineCorePlayer.getUniqueID())) {
                        corePlayer.sendMessage(" ");
                        for(Ban current : punishManager.getBanHistory(offlineCorePlayer.getUniqueID()).getBans()) {
                            corePlayer.sendMessage(new TextBuilder("§c*Info*").setHoverText("§7"+languageHandler.getTranslatedMessage(Message.BAN_USERNAME, language)+" §8● "
                                    +current.getOfflineCorePlayer().getRankPrefix()+current.getOfflineCorePlayer().getUsername()+"\n§7"
                                    +languageHandler.getTranslatedMessage(Message.BAN_REASON, language)+" §8● §c"+current.getReason().getName(language)+"\n§7"
                                    +languageHandler.getTranslatedMessage(Message.BAN_TOTALTIME, language)+" §8● §c"+current.getExpiryTotalName(language)+"\n§7"
                                    +languageHandler.getTranslatedMessage(Message.BAN_CREATOR, language)+" §8● "+current.getCreatorOfflineCorePlayer().getRankPrefix()+current.getCreatorOfflineCorePlayer().getUsername())
                                    .setPreText(" §8× §c"+current.getReason().getName(language)+" §8× §c"+current.getExpiryTotalName(language)+" §8● ").build());
                        }
                    }
                    return;
                }
                if (!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_BAN))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                IOfflineCorePlayer targetPlayer = corePlayerManager.getOfflineCorePlayer(args[0]);
                if(targetPlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                BanReason reason = BanReason.getReasonByString(args[1], language);
                if(reason == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.BAN_REASON_NOTFOUND, language));
                    return;
                }
                if(targetPlayer.getUniqueID() == corePlayer.getUniqueID()) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.BAN_SELF, language));
                    return;
                }
                if(targetPlayer.hasPermission(permissionHandler.getPermission(PermissionType.PUNISH_IGNOREBANS))) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.BAN_TARGET_IGNORED, language));
                    return;
                }
                if(punishManager.isBanned(targetPlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.BAN_ALREADY_BANNED, language));
                    return;
                }
                Ban ban = punishManager.initialBan(targetPlayer, reason, corePlayer.getUniqueID());
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.BAN_BANNED, language)
                .replace("%REPLACE%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()).replace("%REASON%", reason.getName(language)).replace("%DURATION%", ban.getExpiryTotalName(language)));
                return;
            }
        } else if(alias.equalsIgnoreCase("unban")) {
            if(args.length == 1) {
                if (!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.COMMAND_UNBAN))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                IOfflineCorePlayer offlineCorePlayer = corePlayerManager.getOfflineCorePlayer(args[0]);
                if(offlineCorePlayer == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                if(!punishManager.isBanned(offlineCorePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.BAN_NOT_BANNED, language));
                    return;
                }
                punishManager.removeBan(punishManager.getBan(offlineCorePlayer.getUniqueID()));
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.BAN_UNBANNED, language)
                .replace("%REPLACE%", offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername()));
                return;
            }
        }

        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                .replace("%REPLACE%", "ban help"));
    }

    private void registerCommandWindow() {
        commandWindow = new CommandWindow("Ban Help", "§c", pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM), "ban help");

        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.BAN_HELP_PLAYER, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.BAN_HELP_PLAYER_HOVER, Language.GERMAN), "ban <Spieler> <Grund>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.BAN_HELP_LIST, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.BAN_HELP_LIST_HOVER, Language.GERMAN), "ban list");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.BAN_HELP_CHECK, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.BAN_HELP_CHECK_HOVER, Language.GERMAN), "ban check <Spieler>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.BAN_HELP_UNBAN, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.BAN_HELP_UNBAN_HOVER, Language.GERMAN), "unban <Spieler>");

        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.BAN_HELP_PLAYER, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.BAN_HELP_PLAYER_HOVER, Language.ENGLISH), "ban <Player> <Reason>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.BAN_HELP_LIST, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.BAN_HELP_LIST_HOVER, Language.ENGLISH), "ban list");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.BAN_HELP_CHECK, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.BAN_HELP_CHECK_HOVER, Language.ENGLISH), "ban check <Player>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.BAN_HELP_UNBAN, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.BAN_HELP_UNBAN_HOVER, Language.ENGLISH), "unban <Player>");

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
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.BAN_LIST_NONE, language));
            else
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
            return;
        }
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage("§8§m------------§r §7● §cBanlist §7● §8§m------------");
        corePlayer.sendMessage(" ");
        for(TextComponent current : infoCommands)
            corePlayer.sendMessage(current);
        corePlayer.sendMessage(" ");
        if(loadInfoWindow(language,(((page+1)-1)*5)+1, (page+1)*5).size() != 0) {
            if(page == 1) {
                TextComponent nextButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page+1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "ban list "+(page+1)).build();
                TextComponent text = Component.text("§7« §8[§c§l✘§r§8] §7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language)+" §8§l┃ ").append(nextButton).append(Component.text(" §8[§c" + (page + 1) + "§8] §7»"));
                corePlayer.sendMessage(text);
            } else {
                TextComponent nextButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page+1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "ban list "+(page+1)).build();
                TextComponent previousButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language))
                        .setHoverText("§8"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page-1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "ban list "+(page-1)).build();
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
                        .setClickEvent(TextBuilder.Action.COMMAND, "ban list "+(page-1)).build();
                TextComponent text = Component.text("§7« §8[§c" + (page - 1) + "§8] ").append(previousButton).append(Component.text(" §8§l┃ §7"
                        +languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language)+" §8[§c§l✘§r§8] §7»"));
                corePlayer.sendMessage(text);
            }
        }
    }

    private List<TextComponent> loadInfoWindow(Language language, int from, int to) {
        List<TextComponent> list = new ArrayList<>();
        List<TextComponent> bans = getCurrentBans(language);
        for(int i = from; i < to+1 && i < bans.size(); i++)
            list.add(bans.get(i));
        return list;
    }

    private List<TextComponent> getCurrentBans(Language language) {
        List<TextComponent> currentBans = new ArrayList<>();
        List<Ban> bans = instance.getPunishManager().getGlobalBans();

        String buttonTemplate = "§c*Info*";
        for(Ban current : bans) {
            TextComponent button = new TextBuilder("§c"+languageHandler.getTranslatedMessage(Message.BAN_EXPAND_BUTTON, language))
                    .setHoverText("§c"+languageHandler.getTranslatedMessage(Message.BAN_EXPAND_BUTTON_HOVER, language))
                    .setClickEvent(TextBuilder.Action.COMMAND, "ban check "+current.getOfflineCorePlayer().getUsername()).build();
            TextComponent text = new TextBuilder(buttonTemplate).setHoverText("§7"+languageHandler.getTranslatedMessage(Message.BAN_USERNAME, language)+" §8● "
                    +current.getOfflineCorePlayer().getRankPrefix()+current.getOfflineCorePlayer().getUsername()+"\n§7"
                    +languageHandler.getTranslatedMessage(Message.BAN_REASON, language)+" §8● §c"+current.getReason().getName(language)+"\n§7"
                    +languageHandler.getTranslatedMessage(Message.BAN_REMAININGTIME, language)+" §8● §c"+current.getExpiryCurrentName(language)+"\n§7"
                    +languageHandler.getTranslatedMessage(Message.BAN_TOTALTIME, language)+" §8● §c"+current.getExpiryTotalName(language)+"\n§7"
                    +languageHandler.getTranslatedMessage(Message.BAN_CREATOR, language)+" §8● "+current.getCreatorOfflineCorePlayer().getRankPrefix()+current.getCreatorOfflineCorePlayer().getUsername())
                    .setPreText(" §8× "+current.getOfflineCorePlayer().getRankPrefix()+current.getOfflineCorePlayer().getUsername()+" §8× §c"+current.getReason().getName(language)
                            +" §8● ").setPostText(" §8● ").build();
            currentBans.add(text.append(button));
        }
        return currentBans;
    }

}
