// Created by Eric B. 15.02.2021 15:52
package de.ericzones.bungeesystem.collectives.report.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.report.IReportManager;

public class ReviewerDisconnectEvent {

    private final BungeeSystem instance;

    public ReviewerDisconnectEvent(BungeeSystem instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onReviewerDisconnect(DisconnectEvent e) {
        IReportManager reportManager = instance.getReportManager();
        if(!reportManager.isReviewer(e.getPlayer().getUniqueId())) return;
        reportManager.removeReviewer(e.getPlayer().getUniqueId());
    }

}
