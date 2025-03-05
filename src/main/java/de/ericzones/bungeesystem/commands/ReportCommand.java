// Created by Eric B. 02.05.2021 12:03
package de.ericzones.bungeesystem.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.object.CommandWindow;
import de.ericzones.bungeesystem.collectives.object.TextBuilder;
import de.ericzones.bungeesystem.collectives.report.IReportManager;
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

public class ReportCommand implements SimpleCommand {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;
    private final ChatMessageHandler chatMessageHandler;
    private final PermissionHandler permissionHandler;

    private CommandWindow commandWindow;

    public ReportCommand(BungeeSystem instance) {
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

        if(!(source instanceof Player)) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.NOCONSOLE, Language.ENGLISH)));
            return;
        }

        IReportManager reportManager = instance.getReportManager();
        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        ICorePlayer corePlayer = corePlayerManager.getCorePlayer(((Player) source).getUniqueId());
        if(corePlayer == null) {
            source.sendMessage(Component.text(chatMessageHandler.getChatMessage(ChatMessageType.ERROR_COMMAND, Language.ENGLISH)));
            return;
        }
        Language language = corePlayer.getLanguage();

        if(args.length == 0) {
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                    .replace("%REPLACE%", "report help"));
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
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                    return;
                }
            } catch (NumberFormatException e) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                return;
            }
            commandWindow.sendWindow(corePlayer, pageNumber);
            return;
        }

        if(args[0].equalsIgnoreCase("list")) {
            if(!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.MANAGEREPORTS))) {
                corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                return;
            }
            if(args.length == 1) {
                sendInfoWindow(corePlayer, 1);
                return;
            }
            int pageNumber;
            try {
                pageNumber = Integer.parseInt(args[1]);
                if(pageNumber <= 0) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                    return;
                }
            } catch (NumberFormatException e) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
                return;
            }
            sendInfoWindow(corePlayer, pageNumber);
            return;
        }

        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("toggle")) {
                if(!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.MANAGEREPORTS))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                if(reportManager.isReviewer(corePlayer.getUniqueID())) {
                    reportManager.removeReviewer(corePlayer.getUniqueID());
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_TOGGLE_DEACTIVATED, language));
                } else {
                    reportManager.addReviewer(corePlayer.getUniqueID());
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_TOGGLE_ACTIVATED, language));
                }
                return;
            }
            if(args[0].equalsIgnoreCase("close")) {
                if(!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.MANAGEREPORTS))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                if(!reportManager.isReviewer(corePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_NOT_REVIEWER, language));
                    return;
                }
                if(!reportManager.isReviewingReport(corePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_NOT_REVIEWING, language));
                    return;
                }
                Report report = reportManager.getReportByReviewer(corePlayer.getUniqueID());
                reportManager.closeReport(report);
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_CLOSED, language)
                .replace("%REPLACE%", report.getTargetOfflineCorePlayer().getRankPrefix()+report.getTargetOfflineCorePlayer().getUsername()));
                return;
            }
        }

        if(args.length == 2) {
            if(args[0].equalsIgnoreCase("check")) {
                if(!corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.MANAGEREPORTS))) {
                    corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOPERMS, language));
                    return;
                }
                long reportTime;
                try {
                    reportTime = Long.parseLong(args[1]);
                } catch (NumberFormatException e) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_NOTFOUND, language));
                    return;
                }
                Report report = reportManager.getReportByCreationTime(reportTime);
                if(report == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_NOTFOUND, language));
                    return;
                }
                if(!reportManager.isReviewer(corePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_NOT_REVIEWER, language));
                    return;
                }
                if(reportManager.isReviewingReport(corePlayer.getUniqueID())) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_ALREADY_REVIEWING, language));
                    return;
                }
                if(report.getReviewerUniqueId() != null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_TARGET_REVIEWED, language));
                    return;
                }
                if(report.getTargetCorePlayer() == null) {
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                    return;
                }
                reportManager.reviewReport(report, corePlayer);
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_REVIEWING, language)
                .replace("%REPLACE%", report.getTargetCorePlayer().getRankPrefix()+report.getTargetCorePlayer().getUsername()).replace("%REASON%", report.getReason().getName(language)));
                return;
            }
            ICorePlayer targetPlayer = corePlayerManager.getCorePlayer(args[0]);
            if(targetPlayer == null) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+chatMessageHandler.getChatMessage(ChatMessageType.NOTONLINE, language));
                return;
            }
            ReportReason reason = ReportReason.getReasonByString(args[1], language);
            if(reason == null) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_REASON_NOTFOUND, language));
                return;
            }
            if(targetPlayer.getUniqueID() == corePlayer.getUniqueID()) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_SELF, language));
                return;
            }
            if(targetPlayer.hasPermission(permissionHandler.getPermission(PermissionType.MANAGEREPORTS))) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_TARGET_IGNORED, language));
                return;
            }
            if(reportManager.hasReportDelay(corePlayer)) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_IS_DELAYED, language));
                return;
            }
            if(reportManager.hasReported(corePlayer, targetPlayer)) {
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_ALREADY_REPORTED, language));
                return;
            }
            if(!reportManager.isReported(targetPlayer))
                reportManager.initialReport(corePlayer, targetPlayer, reason);

            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_REPORTED, language)
            .replace("%REPLACE%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()).replace("%REASON%", reason.getName(language)));
            return;
        }

        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_SYNTAX, language)
                .replace("%REPLACE%", "report help"));
    }

    private void registerCommandWindow() {
        commandWindow = new CommandWindow("Report Help", "§c", pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM), "report help");

        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.REPORT_HELP_PLAYER, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.REPORT_HELP_PLAYER_HOVER, Language.GERMAN), "report <Spieler> <Grund>");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.REPORT_HELP_LIST, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.REPORT_HELP_LIST_HOVER, Language.GERMAN), "report list");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.REPORT_HELP_TOGGLE, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.REPORT_HELP_TOGGLE_HOVER, Language.GERMAN), "report toggle");
        commandWindow.registerCommand(Language.GERMAN, languageHandler.getTranslatedMessage(Message.REPORT_HELP_CLOSE, Language.GERMAN),
                languageHandler.getTranslatedMessage(Message.REPORT_HELP_CLOSE_HOVER, Language.GERMAN), "report close");

        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.REPORT_HELP_PLAYER, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.REPORT_HELP_PLAYER_HOVER, Language.ENGLISH), "report <Player> <Reason>");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.REPORT_HELP_LIST, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.REPORT_HELP_LIST_HOVER, Language.ENGLISH), "report list");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.REPORT_HELP_TOGGLE, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.REPORT_HELP_TOGGLE_HOVER, Language.ENGLISH), "report toggle");
        commandWindow.registerCommand(Language.ENGLISH, languageHandler.getTranslatedMessage(Message.REPORT_HELP_CLOSE, Language.ENGLISH),
                languageHandler.getTranslatedMessage(Message.REPORT_HELP_CLOSE_HOVER, Language.ENGLISH), "report close");

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
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_LIST_NONE, language));
            else
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PAGENOTFOUND, language));
            return;
        }
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage("§8§m------------§r §7● §cReportlist §7● §8§m------------");
        corePlayer.sendMessage(" ");
        for(TextComponent current : infoCommands)
            corePlayer.sendMessage(current);
        corePlayer.sendMessage(" ");
        if(loadInfoWindow(language,(((page+1)-1)*5)+1, (page+1)*5).size() != 0) {
            if(page == 1) {
                TextComponent nextButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page+1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "report list "+(page+1)).build();
                TextComponent text = Component.text("§7« §8[§c§l✘§r§8] §7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language)+" §8§l┃ ").append(nextButton).append(Component.text(" §8[§c" + (page + 1) + "§8] §7»"));
                corePlayer.sendMessage(text);
            } else {
                TextComponent nextButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language))
                        .setHoverText("§7"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page+1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "report list "+(page+1)).build();
                TextComponent previousButton = new TextBuilder("§7§n"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE, language))
                        .setHoverText("§8"+languageHandler.getTranslatedMessage(Message.PROXY_HELP_PREVIOUSPAGE_HOVER, language).replace("%REPLACE%", String.valueOf(page-1)))
                        .setClickEvent(TextBuilder.Action.COMMAND, "report list "+(page-1)).build();
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
                        .setClickEvent(TextBuilder.Action.COMMAND, "report list "+(page-1)).build();
                TextComponent text = Component.text("§7« §8[§c" + (page - 1) + "§8] ").append(previousButton).append(Component.text(" §8§l┃ §7"
                        +languageHandler.getTranslatedMessage(Message.PROXY_HELP_NEXTPAGE, language)+" §8[§c§l✘§r§8] §7»"));
                corePlayer.sendMessage(text);
            }
        }
    }

    private List<TextComponent> loadInfoWindow(Language language, int from, int to) {
        List<TextComponent> list = new ArrayList<>();
        List<TextComponent> reports = getCurrentReports(language);
        for(int i = from; i < to+1 && i < reports.size(); i++)
            list.add(reports.get(i));
        return list;
    }

    private List<TextComponent> getCurrentReports(Language language) {
        List<TextComponent> currentReports = new ArrayList<>();
        List<Report> reports = instance.getReportManager().getOpenReports();

        String buttonTemplate = "§c*Info*";
        for(Report current : reports) {
            TextComponent button = new TextBuilder("§c"+languageHandler.getTranslatedMessage(Message.REPORT_CHECK_BUTTON, language))
                    .setHoverText("§c"+languageHandler.getTranslatedMessage(Message.REPORT_CHECK_BUTTON_HOVER, language))
                    .setClickEvent(TextBuilder.Action.COMMAND, "report check "+current.getCreationTime()).build();
            TextComponent text = new TextBuilder(buttonTemplate).setHoverText("§7"+languageHandler.getTranslatedMessage(Message.REPORT_USERNAME, language)+" §8● "
                    +current.getTargetOfflineCorePlayer().getRankPrefix()+current.getTargetOfflineCorePlayer().getUsername()+"\n§7"
                    +languageHandler.getTranslatedMessage(Message.REPORT_REASON, language)+" §8● §c"+current.getReason().getName(language)+"\n§7"
                    +languageHandler.getTranslatedMessage(Message.REPORT_CREATOR, language)+" §8● "+current.getCreatorOfflineCorePlayer().getRankPrefix()+current.getCreatorOfflineCorePlayer().getUsername()+"\n§7"
                    +languageHandler.getTranslatedMessage(Message.REPORT_CREATIONTIME, language)+" §8● §c"+current.getCreationTimeName())
                    .setPreText(" §8× "+current.getTargetOfflineCorePlayer().getRankPrefix()+current.getTargetOfflineCorePlayer().getUsername()+" §8× §c"+current.getReason().getName(language)
                            +" §8● ").setPostText(" §8● ").build();
            currentReports.add(text.append(button));
        }
        return currentReports;
    }

}
