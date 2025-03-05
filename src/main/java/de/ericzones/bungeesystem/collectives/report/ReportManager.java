// Created by Eric B. 15.02.2021 12:07
package de.ericzones.bungeesystem.collectives.report;

import com.velocitypowered.api.event.EventManager;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.collectives.database.ISqlAdapter;
import de.ericzones.bungeesystem.collectives.object.TextBuilder;
import de.ericzones.bungeesystem.collectives.report.event.*;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;
import net.kyori.adventure.text.TextComponent;

import java.util.*;
import java.util.stream.Collectors;

public class ReportManager extends SqlReport implements IReportManager {

    private final BungeeSystem instance;
    private final LanguageHandler languageHandler;

    private final List<Report> openReports = new ArrayList<>();
    private final List<Report> reviewingReports = new ArrayList<>();
    private final List<UUID> availableReviewers = new ArrayList<>();

    private final Map<UUID, Long> reportDelayCache = new HashMap<>();

    public ReportManager(BungeeSystem instance, ISqlAdapter sqlAdapter) {
        super(sqlAdapter);
        this.instance = instance;
        this.languageHandler = instance.getLanguageHandler();
        EventManager eventManager = instance.getProxyServer().getEventManager();
        eventManager.register(instance, new ReviewerConnectEvent(instance));
        eventManager.register(instance, new ReviewerDisconnectEvent(instance));
        eventManager.register(instance, new TargetDisconnectEvent(instance));
        eventManager.register(instance, new TargetSwitchedServerEvent(instance));
        eventManager.register(instance, new ReviewerSwitchedServerEvent(instance));
        registerAllReports();
    }

    @Override
    public List<Report> getOpenReports() {
        List<Report> reports = this.openReports;
        reports.removeIf(this::isReportExpired);
        return reports;
    }

    @Override
    public List<Report> getReviewingReports() {
        return this.reviewingReports;
    }

    @Override
    public int getOpenReportsCount() {
        return getOpenReports().size();
    }

    @Override
    public List<Report> getOpenReportsByReason(ReportReason reason) {
        List<Report> reports = this.openReports.stream().filter(Report -> Report.getReason().equals(reason)).collect(Collectors.toList());
        reports.removeIf(this::isReportExpired);
        return reports;
    }

    @Override
    public List<Report> getReviewingReportsByReason(ReportReason reason) {
        return this.reviewingReports.stream().filter(Report -> Report.getReason().equals(reason)).collect(Collectors.toList());
    }

    @Override
    public List<Report> getReportsByCreator(ICorePlayer corePlayer) {
        List<Report> reports = this.openReports.stream().filter(Report -> Report.getCreatorCorePlayer().equals(corePlayer)).collect(Collectors.toList());
        reports.removeIf(this::isReportExpired);
        reports.addAll(this.reviewingReports.stream().filter(Report -> Report.getCreatorCorePlayer().equals(corePlayer)).collect(Collectors.toList()));
        return reports;
    }

    @Override
    public List<Report> getReportsByCreator(IOfflineCorePlayer offlineCorePlayer) {
        List<Report> reports = this.openReports.stream().filter(Report -> Report.getCreatorOfflineCorePlayer().equals(offlineCorePlayer)).collect(Collectors.toList());
        reports.removeIf(this::isReportExpired);
        reports.addAll(this.reviewingReports.stream().filter(Report -> Report.getCreatorOfflineCorePlayer().equals(offlineCorePlayer)).collect(Collectors.toList()));
        return reports;
    }

    @Override
    public List<Report> getReportsByTarget(ICorePlayer corePlayer) {
        List<Report> reports = this.openReports.stream().filter(Report -> Report.getTargetCorePlayer().equals(corePlayer)).collect(Collectors.toList());
        reports.removeIf(this::isReportExpired);
        reports.addAll(this.reviewingReports.stream().filter(Report -> Report.getTargetCorePlayer().equals(corePlayer)).collect(Collectors.toList()));
        return reports;
    }

    @Override
    public List<Report> getReportsByTarget(IOfflineCorePlayer offlineCorePlayer) {
        List<Report> reports = this.openReports.stream().filter(Report -> Report.getTargetOfflineCorePlayer().equals(offlineCorePlayer)).collect(Collectors.toList());
        reports.removeIf(this::isReportExpired);
        reports.addAll(this.reviewingReports.stream().filter(Report -> Report.getTargetOfflineCorePlayer().equals(offlineCorePlayer)).collect(Collectors.toList()));
        return reports;
    }

    @Override
    public List<Report> getReportsByReviewedTarget(UUID uuid) {
        return this.reviewingReports.stream().filter(Report -> Report.getTargetUniqueId().equals(uuid)).collect(Collectors.toList());
    }

    @Override
    public Report getReportByCreationTime(Long creationTime) {
        return this.openReports.stream().filter(Report -> Report.getCreationTime().equals(creationTime)).findFirst().orElse(
                this.reviewingReports.stream().filter(Report -> Report.getCreationTime().equals(creationTime)).findFirst().orElse(null));
    }

    @Override
    public Report getReportByReviewer(UUID uuid) {
        return this.reviewingReports.stream().filter(Report -> Report.getReviewerUniqueId().equals(uuid)).findFirst().orElse(null);
    }

    @Override
    public Report initialReport(ICorePlayer creator, IOfflineCorePlayer target, ReportReason reason) {
        Report report = new Report(creator.getUniqueID(), target.getUniqueID(), reason);
        this.openReports.add(report);

        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        for(UUID current : this.availableReviewers) {
            ICorePlayer corePlayer = corePlayerManager.getCorePlayer(current);
            if (corePlayer == null) continue;
            Language language = corePlayer.getLanguage();
            TextComponent clickableText = new TextBuilder(languageHandler.getTranslatedMessage(Message.REPORT_INCOMING_INVESTIGATE, language))
                    .setHoverText("§c" + languageHandler.getTranslatedMessage(Message.REPORT_INCOMING_INVESTIGATE_HOVER, language))
                    .setClickEvent(TextBuilder.Action.COMMAND, "report check " + report.getCreationTime())
                    .setPreText(instance.getPluginPrefixHandler().getPluginPrefix(PluginPrefixType.REPORTSYSTEM_BC)
                            + target.getRankPrefix() + target.getUsername() + " §8× §c" + reason.getName(language) + " §8● ")
                    .setPostText(" §8●").build();

            corePlayer.sendMessage("");
            corePlayer.sendMessage(clickableText);
            corePlayer.sendMessage("");
        }
        createReport(creator.getUniqueID(), target.getUniqueID(), reason, report.getCreationTimeName(), report.getCreationTime());
        return report;
    }

    @Override
    public Report initialReport(ICorePlayer creator, ICorePlayer target, ReportReason reason) {
        Report report = new Report(creator.getUniqueID(), target.getUniqueID(), reason);
        this.openReports.add(report);

        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        for(UUID current : this.availableReviewers) {
            ICorePlayer corePlayer = corePlayerManager.getCorePlayer(current);
            if (corePlayer == null) continue;
            Language language = corePlayer.getLanguage();
            TextComponent clickableText = new TextBuilder(languageHandler.getTranslatedMessage(Message.REPORT_INCOMING_INVESTIGATE, language))
                    .setHoverText("§c" + languageHandler.getTranslatedMessage(Message.REPORT_INCOMING_INVESTIGATE_HOVER, language))
                    .setClickEvent(TextBuilder.Action.COMMAND, "report check " + report.getCreationTime())
                    .setPreText(instance.getPluginPrefixHandler().getPluginPrefix(PluginPrefixType.REPORTSYSTEM_BC)
                            + target.getRankPrefix() + target.getUsername() + " §8× §c" + reason.getName(language) + " §8● ")
                    .setPostText(" §8●").build();

            corePlayer.sendMessage("");
            corePlayer.sendMessage(clickableText);
            corePlayer.sendMessage("");
        }
        createReport(creator.getUniqueID(), target.getUniqueID(), reason, report.getCreationTimeName(), report.getCreationTime());
        return report;
    }

    private void initialReport(UUID creator, UUID target, ReportReason reason, String creationTimeName, Long creationTime) {
        Report report = new Report(creator, target, reason, creationTimeName, creationTime);
        this.openReports.add(report);
    }

    @Override
    public boolean isReported(ICorePlayer corePlayer) {
        Report report = this.openReports.stream().filter(Report -> Report.getTargetCorePlayer().equals(corePlayer)).findFirst().orElse(null);
        if(report != null && !isReportExpired(report))
            return true;
        report =  this.reviewingReports.stream().filter(Report -> Report.getTargetCorePlayer().equals(corePlayer)).findFirst().orElse(null);
        return report != null;
    }

    @Override
    public boolean isReported(IOfflineCorePlayer offlineCorePlayer) {
        Report report = this.openReports.stream().filter(Report -> Report.getTargetOfflineCorePlayer().equals(offlineCorePlayer)).findFirst().orElse(null);
        if(report != null && !isReportExpired(report))
            return true;
        report = this.reviewingReports.stream().filter(Report -> Report.getTargetOfflineCorePlayer().equals(offlineCorePlayer)).findFirst().orElse(null);
        return report != null;
    }

    @Override
    public boolean isReviewed(UUID uuid) {
        Report report = this.reviewingReports.stream().filter(Report -> Report.getTargetUniqueId().equals(uuid)).findFirst().orElse(null);
        return report != null;
    }

    @Override
    public boolean hasReported(ICorePlayer corePlayer) {
        Report report = this.openReports.stream().filter(Report -> Report.getCreatorCorePlayer().equals(corePlayer)).findFirst().orElse(null);
        if(report != null && !isReportExpired(report))
            return true;
        report = this.reviewingReports.stream().filter(Report -> Report.getCreatorCorePlayer().equals(corePlayer)).findFirst().orElse(null);
        return report != null;
    }

    @Override
    public boolean hasReported(IOfflineCorePlayer offlineCorePlayer) {
        Report report = this.openReports.stream().filter(Report -> Report.getCreatorOfflineCorePlayer().equals(offlineCorePlayer)).findFirst().orElse(null);
        if(report != null && !isReportExpired(report))
            return true;
        report = this.reviewingReports.stream().filter(Report -> Report.getCreatorOfflineCorePlayer().equals(offlineCorePlayer)).findFirst().orElse(null);
        return report != null;
    }

    @Override
    public boolean hasReported(ICorePlayer creator, IOfflineCorePlayer target) {
        Report report = this.openReports.stream().filter(Report -> Report.getTargetOfflineCorePlayer().equals(target) && Report.getCreatorCorePlayer().equals(creator)).findFirst().orElse(null);
        if(report != null && !isReportExpired(report))
            return true;
        report = this.reviewingReports.stream().filter(Report -> Report.getTargetOfflineCorePlayer().equals(target) && Report.getCreatorCorePlayer().equals(creator)).findFirst().orElse(null);
        return report != null;
    }

    @Override
    public boolean hasReported(ICorePlayer creator, ICorePlayer target) {
        Report report = this.openReports.stream().filter(Report -> Report.getTargetCorePlayer().equals(target) && Report.getCreatorCorePlayer().equals(creator)).findFirst().orElse(null);
        if(report != null && !isReportExpired(report))
            return true;
        report = this.reviewingReports.stream().filter(Report -> Report.getTargetCorePlayer().equals(target) && Report.getCreatorCorePlayer().equals(creator)).findFirst().orElse(null);
        return report != null;
    }

    @Override
    public void closeReport(Report report) {
        this.openReports.remove(report);
        this.reviewingReports.remove(report);
        deleteReport(report.getCreationTime());
    }

    @Override
    public void stopReviewingReport(Report report) {
        report.removeReviewer();
        this.reviewingReports.remove(report);
        this.openReports.add(report);
    }

    @Override
    public void reviewReport(Report report, ICorePlayer corePlayer) {
        report.setReviewer(corePlayer.getUniqueID());
        this.openReports.remove(report);
        this.reviewingReports.add(report);
        ICorePlayer target = report.getTargetCorePlayer();
        corePlayer.sendToServer(target.getConnectedCoreServer());
    }

    @Override
    public void addReviewer(UUID uuid) {
        this.availableReviewers.add(uuid);
    }

    @Override
    public void removeReviewer(UUID uuid) {
        this.availableReviewers.remove(uuid);
        if(isReviewingReport(uuid))
            stopReviewingReport(getReportByReviewer(uuid));
    }

    @Override
    public boolean isReviewer(UUID uuid) {
        return this.availableReviewers.contains(uuid);
    }

    @Override
    public boolean isReviewingReport(UUID uuid) {
        return this.reviewingReports.stream().anyMatch(Report -> Report.getReviewerUniqueId().equals(uuid));
    }

    @Override
    public boolean hasReportDelay(ICorePlayer corePlayer) {
        for (UUID current : this.reportDelayCache.keySet())
            if (this.reportDelayCache.get(current) < System.currentTimeMillis()) this.reportDelayCache.remove(current);

        if(!this.reportDelayCache.containsKey(corePlayer.getUniqueID())) {
            this.reportDelayCache.put(corePlayer.getUniqueID(), System.currentTimeMillis()+30*1000);
            return false;
        }
        return true;
    }

    private boolean isReportExpired(Report report) {
        long expiry = report.getCreationTime()+24*60*60*1000;
        if(expiry < System.currentTimeMillis()) {
            closeReport(report);
            return true;
        }
        return false;
    }

    private void registerAllReports() {
        Map<Long, Map<ReportProperty, String>> reports = getAllReports();
        for(Long current : reports.keySet()) {
            Map<ReportProperty, String> currentMap = reports.get(current);
            ReportReason reason = ReportReason.valueOf(currentMap.get(ReportProperty.REASON));
            UUID creator = UUID.fromString(currentMap.get(ReportProperty.CREATOR));
            UUID target = UUID.fromString(currentMap.get(ReportProperty.TARGET));
            String creationTimeName = currentMap.get(ReportProperty.CREATIONDATE);
            initialReport(creator, target, reason, creationTimeName, current);
        }
    }

}
