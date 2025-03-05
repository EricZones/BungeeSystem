// Created by Eric B. 05.05.2021 18:02
package de.ericzones.bungeesystem.collectives.report.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.report.IReportManager;
import de.ericzones.bungeesystem.collectives.report.Report;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixHandler;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;

public class ReviewerSwitchedServerEvent {

    private final BungeeSystem instance;
    private final LanguageHandler languageHandler;
    private final PluginPrefixHandler pluginPrefixHandler;

    public ReviewerSwitchedServerEvent(BungeeSystem instance) {
        this.instance = instance;
        this.languageHandler = instance.getLanguageHandler();
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
    }

    @Subscribe
    public void onTargetSwitchedServer(ServerPostConnectEvent e) {
        IReportManager reportManager = instance.getReportManager();
        if(!reportManager.isReviewingReport(e.getPlayer().getUniqueId())) return;
        ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(e.getPlayer().getUniqueId());
        if(corePlayer == null) return;
        Report report = reportManager.getReportByReviewer(e.getPlayer().getUniqueId());
        reportManager.stopReviewingReport(report);
        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.REPORT_REVIEWER_SWITCHED, corePlayer.getLanguage()));
    }

}
