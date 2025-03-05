// Created by Eric B. 10.02.2021 17:47
package de.ericzones.bungeesystem.collectives.punish.ban;

import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class BanHistory {

    private final UUID uuid;
    private final List<Ban> bans;

    public BanHistory(UUID uuid, List<Ban> previousBans) {
        this.uuid = uuid;
        this.bans = previousBans;
    }

    public ICorePlayer getCorePlayer() {
        return BungeeSystem.getInstance().getCorePlayerManager().getCorePlayer(uuid);
    }

    public IOfflineCorePlayer getOfflineCorePlayer() {
        return BungeeSystem.getInstance().getCorePlayerManager().getOfflineCorePlayer(uuid);
    }

    public List<Ban> getBans() {
        return this.bans;
    }

    public void removeBan(Ban ban) {
        this.bans.remove(ban);
    }

    public void removeBan(Long creationTime) {
        this.bans.stream().filter(Ban -> Ban.getCreationTime().equals(creationTime)).findFirst().ifPresent(this.bans::remove);
    }

    public void addBan(Ban ban) {
        this.bans.add(ban);
    }

    public void addBans(List<Ban> bans) {
        this.bans.addAll(bans);
    }

    public List<Ban> getBansByReason(BanReason reason) {
        return this.bans.stream().filter(Ban -> Ban.getReason().equals(reason)).collect(Collectors.toList());
    }

    public List<Ban> getBansByCreator(UUID uuid) {
        return this.bans.stream().filter(Ban -> Ban.getCreatorOfflineCorePlayer().getUniqueID().equals(uuid)).collect(Collectors.toList());
    }

    public Ban getBanByCreationTime(Long creationTime) {
        return this.bans.stream().filter(Ban -> Ban.getCreationTime().equals(creationTime)).findFirst().orElse(null);
    }

}
