// Created by Eric B. 15.02.2021 14:53
package de.ericzones.bungeesystem.collectives.report;

import com.velocitypowered.api.util.UuidUtils;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;

import java.util.List;
import java.util.UUID;

public interface IReportManager {

    List<Report> getOpenReports();
    List<Report> getReviewingReports();

    int getOpenReportsCount();

    List<Report> getOpenReportsByReason(ReportReason reason);
    List<Report> getReviewingReportsByReason(ReportReason reason);

    List<Report> getReportsByCreator(ICorePlayer corePlayer);
    List<Report> getReportsByCreator(IOfflineCorePlayer offlineCorePlayer);
    List<Report> getReportsByTarget(ICorePlayer corePlayer);
    List<Report> getReportsByTarget(IOfflineCorePlayer offlineCorePlayer);

    List<Report> getReportsByReviewedTarget(UUID uuid);

    Report getReportByCreationTime(Long creationTime);
    Report getReportByReviewer(UUID uuid);

    Report initialReport(ICorePlayer creator, IOfflineCorePlayer target, ReportReason reason);
    Report initialReport(ICorePlayer creator, ICorePlayer target, ReportReason reason);

    boolean isReported(ICorePlayer corePlayer);
    boolean isReported(IOfflineCorePlayer offlineCorePlayer);
    boolean isReviewed(UUID uuid);

    boolean hasReported(ICorePlayer corePlayer);
    boolean hasReported(IOfflineCorePlayer offlineCorePlayer);
    boolean hasReported(ICorePlayer creator, IOfflineCorePlayer target);
    boolean hasReported(ICorePlayer creator, ICorePlayer target);

    void closeReport(Report report);

    void stopReviewingReport(Report report);
    void reviewReport(Report report, ICorePlayer corePlayer);

    void addReviewer(UUID uuid);
    void removeReviewer(UUID uuid);
    boolean isReviewer(UUID uuid);
    boolean isReviewingReport(UUID uuid);

    boolean hasReportDelay(ICorePlayer corePlayer);

}
