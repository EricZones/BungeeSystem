// Created by Eric B. 15.02.2021 12:07
package de.ericzones.bungeesystem.collectives.report;

import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Report {

    private final UUID creator;
    private final UUID target;
    private final ReportReason reason;
    private final String creationTimeName;
    private final Long creationTime;

    private UUID reviewer;

    public Report(UUID creator, UUID target, ReportReason reason, String creationTimeName, Long creationTime) {
        this.creator = creator;
        this.target = target;
        this.reason = reason;
        this.creationTimeName = creationTimeName;
        this.creationTime = creationTime;
    }

    public Report(UUID creator, UUID target, ReportReason reason) {
        this.creator = creator;
        this.target = target;
        this.reason = reason;
        this.creationTimeName = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").format(LocalDateTime.now(ZoneId.of("UTC+"+BungeeSystem.getInstance().getTimezoneNumber())));
        this.creationTime = System.currentTimeMillis();
    }

    public UUID getCreatorUniqueId() {
        return this.creator;
    }

    public ICorePlayer getCreatorCorePlayer() {
        return BungeeSystem.getInstance().getCorePlayerManager().getCorePlayer(this.creator);
    }

    public IOfflineCorePlayer getCreatorOfflineCorePlayer() {
        return BungeeSystem.getInstance().getCorePlayerManager().getOfflineCorePlayer(this.creator);
    }

    public UUID getTargetUniqueId() {
        return this.target;
    }

    public ICorePlayer getTargetCorePlayer() {
        return BungeeSystem.getInstance().getCorePlayerManager().getCorePlayer(target);
    }

    public IOfflineCorePlayer getTargetOfflineCorePlayer() {
        return BungeeSystem.getInstance().getCorePlayerManager().getOfflineCorePlayer(target);
    }

    public ReportReason getReason() {
        return this.reason;
    }

    public Long getCreationTime() {
        return this.creationTime;
    }

    public String getCreationTimeName() {
        return this.creationTimeName;
    }

    public UUID getReviewerUniqueId() {
        return this.reviewer;
    }

    public ICorePlayer getReviewerCorePlayer() {
        if(this.reviewer == null) return null;
        return BungeeSystem.getInstance().getCorePlayerManager().getCorePlayer(this.reviewer);
    }

    public IOfflineCorePlayer getReviewerOfflineCorePlayer() {
        if(this.reviewer == null) return null;
        return BungeeSystem.getInstance().getCorePlayerManager().getOfflineCorePlayer(this.reviewer);
    }

    public void setReviewer(UUID reviewer) {
        this.reviewer = reviewer;
    }

    public void removeReviewer() {
        this.reviewer = null;
    }

}
