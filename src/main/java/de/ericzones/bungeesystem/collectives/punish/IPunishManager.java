// Created by Eric B. 10.02.2021 20:18
package de.ericzones.bungeesystem.collectives.punish;

import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.collectives.punish.ban.Ban;
import de.ericzones.bungeesystem.collectives.punish.ban.BanHistory;
import de.ericzones.bungeesystem.collectives.punish.ban.BanReason;
import de.ericzones.bungeesystem.collectives.punish.mute.Mute;
import de.ericzones.bungeesystem.collectives.punish.mute.MuteHistory;
import de.ericzones.bungeesystem.collectives.punish.mute.MuteReason;

import java.util.List;
import java.util.UUID;

public interface IPunishManager {

    List<Ban> getGlobalBans();
    List<BanHistory> getGlobalBanHistories();
    List<Mute> getGlobalMutes();
    List<MuteHistory> getGlobalMuteHistories();

    List<Ban> getBansByCreator(UUID uuid);
    List<Ban> getBansByReason(BanReason reason);
    List<Mute> getMutesByCreator(UUID uuid);
    List<Mute> getMutesByReason(MuteReason reason);

    Ban initialBan(ICorePlayer corePlayer, BanReason reason, UUID creator);
    Ban initialBan(IOfflineCorePlayer offlineCorePlayer, BanReason reason, UUID creator);
    Mute initialMute(ICorePlayer corePlayer, MuteReason reason, UUID creator);
    Mute initialMute(IOfflineCorePlayer offlineCorePlayer, MuteReason reason, UUID creator);

    void removeBan(Ban ban);
    void resetBanHistory(BanHistory banHistory);
    void removeMute(Mute mute);
    void resetMuteHistory(MuteHistory muteHistory);

    boolean isBanned(UUID uuid);
    boolean isBanned(String ipAddress);
    boolean isBanned(ICorePlayer corePlayer);
    boolean isBanned(IOfflineCorePlayer offlineCorePlayer);

    boolean isMuted(UUID uuid);
    boolean isMuted(ICorePlayer corePlayer);
    boolean isMuted(IOfflineCorePlayer offlineCorePlayer);

    boolean hasBanHistory(UUID uuid);
    boolean hasBanHistory(ICorePlayer corePlayer);
    boolean hasBanHistory(IOfflineCorePlayer offlineCorePlayer);

    boolean hasMuteHistory(UUID uuid);
    boolean hasMuteHistory(ICorePlayer corePlayer);
    boolean hasMuteHistory(IOfflineCorePlayer offlineCorePlayer);

    Ban getBan(UUID uuid);
    Ban getBan(ICorePlayer corePlayer);
    Ban getBan(IOfflineCorePlayer offlineCorePlayer);
    Ban getBan(String ipAddress);

    Mute getMute(UUID uuid);
    Mute getMute(ICorePlayer corePlayer);
    Mute getMute(IOfflineCorePlayer offlineCorePlayer);

    BanHistory getBanHistory(UUID uuid);
    BanHistory getBanHistory(ICorePlayer corePlayer);
    BanHistory getBanHistory(IOfflineCorePlayer offlineCorePlayer);

    MuteHistory getMuteHistory(UUID uuid);
    MuteHistory getMuteHistory(ICorePlayer corePlayer);
    MuteHistory getMuteHistory(IOfflineCorePlayer offlineCorePlayer);

    String getIsMutedChatMessage(Mute mute, ICorePlayer corePlayer);

}
