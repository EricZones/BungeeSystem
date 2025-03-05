// Created by Eric B. 31.01.2021 14:22
package de.ericzones.bungeesystem.collectives.coreplayer.connection;

import java.util.UUID;

public class PlayerConnectionInfo {

    private UUID uuid;
    private PlayerVersion version;
    private String ipAddress;
    private int port;
    private boolean legacy;

    public PlayerConnectionInfo(UUID uuid, PlayerVersion version, String ipAddress, int port, boolean legacy) {
        this.uuid = uuid;
        this.version = version;
        this.ipAddress = ipAddress;
        this.port = port;
        this.legacy = legacy;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public PlayerVersion getVersion() {
        return version;
    }

    public void setVersion(PlayerVersion version) {
        this.version = version;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean getLegacy() {
        return legacy;
    }

    public void setLegacy(boolean legacy) {
        this.legacy = legacy;
    }
}
