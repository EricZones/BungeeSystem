// Created by Eric B. 12.02.2021 17:55
package de.ericzones.bungeesystem.collectives.punish.mute;

import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MuteHistory {

    private final UUID uuid;
    private final List<Mute> mutes;

    public MuteHistory(UUID uuid, List<Mute> previousMutes) {
        this.uuid = uuid;
        this.mutes = previousMutes;
    }

    public ICorePlayer getCorePlayer() {
        return BungeeSystem.getInstance().getCorePlayerManager().getCorePlayer(uuid);
    }

    public IOfflineCorePlayer getOfflineCorePlayer() {
        return BungeeSystem.getInstance().getCorePlayerManager().getOfflineCorePlayer(uuid);
    }

    public List<Mute> getMutes() {
        return this.mutes;
    }

    public void removeMute(Mute mute) {
        this.mutes.remove(mute);
    }

    public void removeMute(Long creationTime) {
        this.mutes.stream().filter(Mute -> Mute.getCreationTime().equals(creationTime)).findFirst().ifPresent(this.mutes::remove);
    }

    public void addMute(Mute mute) {
        this.mutes.add(mute);
    }

    public void addMutes(List<Mute> mutes) {
        this.mutes.addAll(mutes);
    }

    public List<Mute> getMutesByReason(MuteReason reason) {
        return this.mutes.stream().filter(Mute -> Mute.getReason().equals(reason)).collect(Collectors.toList());
    }

    public List<Mute> getMutesByCreator(UUID uuid) {
        return this.mutes.stream().filter(Mute -> Mute.getCreatorOfflineCorePlayer().getUniqueID().equals(uuid)).collect(Collectors.toList());
    }

    public Mute getMuteByCreationTime(Long creationTime) {
        return this.mutes.stream().filter(Mute -> Mute.getCreationTime().equals(creationTime)).findFirst().orElse(null);
    }

}
