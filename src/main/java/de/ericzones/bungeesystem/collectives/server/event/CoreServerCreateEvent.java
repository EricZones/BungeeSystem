// Created by Eric B. 31.01.2021 20:16
package de.ericzones.bungeesystem.collectives.server.event;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.driver.event.events.service.CloudServiceConnectNetworkEvent;
import de.dytanic.cloudnet.driver.event.events.service.CloudServiceInfoUpdateEvent;
import de.dytanic.cloudnet.driver.event.events.service.CloudServiceRegisterEvent;
import de.dytanic.cloudnet.driver.event.events.service.CloudServiceStartEvent;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.driver.service.property.ServiceProperty;
import de.dytanic.cloudnet.event.service.CloudServicePostStartEvent;
import de.dytanic.cloudnet.ext.bridge.BridgeServiceProperty;
import de.dytanic.cloudnet.ext.bridge.velocity.event.VelocityCloudServiceStartEvent;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.server.ICoreServerManager;
import de.ericzones.bungeesystem.collectives.server.connection.ServerConnectionInfo;
import de.ericzones.bungeesystem.collectives.server.connection.ServerConnectionStatus;

import java.util.Optional;

public class CoreServerCreateEvent {

    private final BungeeSystem instance;

    public CoreServerCreateEvent(BungeeSystem instance) {
        this.instance = instance;
    }

    @EventListener
    public void onCoreServerStarted(CloudServiceInfoUpdateEvent e) {
        if(e.getServiceInfo().getConfiguration().getProcessConfig().getEnvironment().isMinecraftProxy() || e.getServiceInfo().getConfiguration().getProcessConfig().getEnvironment().isMinecraftJavaProxy())
            return;
        ICoreServerManager coreServerManager = instance.getCoreServerManager();
        if(coreServerManager.getCoreServer(e.getServiceInfo().getName()) != null) return;
        if(!(e.getServiceInfo().getProperty(BridgeServiceProperty.IS_ONLINE).isPresent() && e.getServiceInfo().getProperty(BridgeServiceProperty.IS_ONLINE).get())) return;

        String serverName = e.getServiceInfo().getName();
        ServiceInfoSnapshot serviceInfoSnapshot = e.getServiceInfo();

        int port = serviceInfoSnapshot.getAddress().getPort();
        ServerConnectionStatus serverConnectionStatus = coreServerManager.getServerConnectionStatus(serviceInfoSnapshot);
        String version = "";
        Optional<String> versionCache = serviceInfoSnapshot.getProperty(BridgeServiceProperty.VERSION);
        if(versionCache.isPresent())
            version = versionCache.get();
        ServerConnectionInfo connectionInfo = new ServerConnectionInfo(port, serverConnectionStatus, version);
        int maxPlayerCount = 0;
        if(serviceInfoSnapshot.getProperty(BridgeServiceProperty.MAX_PLAYERS).isPresent())
            maxPlayerCount = serviceInfoSnapshot.getProperty(BridgeServiceProperty.MAX_PLAYERS).get();

        coreServerManager.initialCoreServer(serverName, connectionInfo, coreServerManager.getCoreServerType(serviceInfoSnapshot), maxPlayerCount);
    }

}
