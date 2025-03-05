// Created by Eric B. 15.02.2021 15:55
package de.ericzones.bungeesystem.collectives.report.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.report.IReportManager;
import de.ericzones.bungeesystem.collectives.report.Report;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixHandler;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;

import java.util.List;

public class TargetDisconnectEvent {

    private final BungeeSystem instance;
    private final LanguageHandler languageHandler;
    private final PluginPrefixHandler pluginPrefixHandler;

    public TargetDisconnectEvent(BungeeSystem instance) {
        this.instance = instance;
        this.languageHandler = instance.getLanguageHandler();
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
    }

    @Subscribe
    public void onTargetDisconnect(DisconnectEvent e) {
        IReportManager reportManager = instance.getReportManager();
        if(!reportManager.isReviewed(e.getPlayer().getUniqueId())) return;
        List<Report> reports = reportManager.getReportsByReviewedTarget(e.getPlayer().getUniqueId());
        for(Report current : reports) {
            ICorePlayer corePlayer = current.getReviewerCorePlayer();
            Language language = corePlayer.getLanguage();
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.REPORTSYSTEM)+"§7"
                    +languageHandler.getTranslatedMessage(Message.REPORT_TARGETDISCONNECTED, language));
            reportManager.stopReviewingReport(current);
        }
    }

}
