// Created by Eric B. 31.01.2021 14:47
package de.ericzones.bungeesystem.collectives.server.connection;

public class ServerConnectionInfo {

    private int port;
    private ServerConnectionStatus connectionStatus;
    private String version;

    public ServerConnectionInfo(int port, ServerConnectionStatus connectionStatus, String version) {
        this.port = port;
        this.connectionStatus = connectionStatus;
        this.version = version;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public ServerConnectionStatus getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(ServerConnectionStatus connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
