// Created by Eric B. 15.02.2021 15:53
package de.ericzones.bungeesystem.collectives.report.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;;
import de.ericzones.bungeesystem.collectives.object.TextBuilder;
import de.ericzones.bungeesystem.collectives.report.IReportManager;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixHandler;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;
import de.ericzones.bungeesystem.global.permission.PermissionHandler;
import de.ericzones.bungeesystem.global.permission.PermissionType;
import net.kyori.adventure.text.TextComponent;

public class ReviewerConnectEvent {

    private final BungeeSystem instance;
    private final PermissionHandler permissionHandler;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;

    public ReviewerConnectEvent(BungeeSystem instance) {
        this.instance = instance;
        this.permissionHandler = instance.getPermissionHandler();
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
        this.languageHandler = instance.getLanguageHandler();
    }

    @Subscribe
    public void onReviewerConnect(PostLoginEvent e) {
        IOfflineCorePlayer offlineCorePlayer = instance.getCorePlayerManager().getOfflineCorePlayer(e.getPlayer().getUniqueId());
        if(offlineCorePlayer == null) return;
        if(!offlineCorePlayer.hasPermission(permissionHandler.getPermission(PermissionType.MANAGEREPORTS))) return;
        IReportManager reportManager = instance.getReportManager();
        Language language = offlineCorePlayer.getLanguage();
        int openReports = reportManager.getOpenReportsCount();

        reportManager.addReviewer(offlineCorePlayer.getUniqueID());
        if(openReports > 0)
            sendOpenReportsMessage(e.getPlayer(), openReports, language);
    }

    private void sendOpenReportsMessage(Player player, int openReports, Language language) {
        String mainText;
        if(openReports == 1)
            mainText = "§7"+languageHandler.getTranslatedMessage(Message.REPORT_REVIEWERJOIN_SINGLE, language);
        else
            mainText = "§7"+languageHandler.getTranslatedMessage(Message.REPORT_REVIEWERJOIN_MULTIPLE, language).replace("%REPLACE%", String.valueOf(openReports));
        TextComponent text = new TextBuilder(mainText).setHoverText("§c"+languageHandler.getTranslatedMessage(Message.REPORT_REVIEWERJOIN_HOVER, language))
                .setClickEvent(TextBuilder.Action.COMMAND, "reportlist").setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)).build();
        player.sendMessage(text);
    }

}
