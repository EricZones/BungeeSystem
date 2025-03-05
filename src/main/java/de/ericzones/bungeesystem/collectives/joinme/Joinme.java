// Created by Eric B. 14.02.2021 23:31
package de.ericzones.bungeesystem.collectives.joinme;

import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.collectives.server.ICoreServer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Joinme {

    private final UUID uniqueId;
    private final UUID creator;
    private final String serverName;
    private final String creationTimeName;
    private final Long creationTime;

    public Joinme(UUID creator, String serverName)  {
        this.uniqueId = UUID.randomUUID();
        this.creator = creator;
        this.serverName = serverName;
        this.creationTimeName = DateTimeFormatter.ofPattern("HH:mm").format(LocalDateTime.now(ZoneId.of("UTC+"+BungeeSystem.getInstance().getTimezoneNumber())));
        this.creationTime = System.currentTimeMillis();
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public UUID getCreator() {
        return this.creator;
    }

    public ICorePlayer getCorePlayerCreator() {
        return BungeeSystem.getInstance().getCorePlayerManager().getCorePlayer(this.creator);
    }

    public IOfflineCorePlayer getOfflineCorePlayerCreator() {
        return BungeeSystem.getInstance().getCorePlayerManager().getOfflineCorePlayer(this.creator);
    }

    public String getServerName() {
        return this.serverName;
    }

    public ICoreServer getCoreServer() {
        return BungeeSystem.getInstance().getCoreServerManager().getCoreServer(this.serverName);
    }

    public Long getCreationTime() {
        return this.creationTime;
    }

    public String getCreationTimeName() {
        return this.creationTimeName;
    }

}
