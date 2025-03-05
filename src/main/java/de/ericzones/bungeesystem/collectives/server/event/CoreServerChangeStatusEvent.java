// Created by Eric B. 31.01.2021 21:20
package de.ericzones.bungeesystem.collectives.server.event;

import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.driver.event.events.service.CloudServiceInfoUpdateEvent;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.ext.bridge.BridgeServiceProperty;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.server.ICoreServer;
import de.ericzones.bungeesystem.collectives.server.ICoreServerManager;
import de.ericzones.bungeesystem.collectives.server.connection.ServerConnectionStatus;

import java.util.Optional;

public class CoreServerChangeStatusEvent {

    private final BungeeSystem instance;

    public CoreServerChangeStatusEvent(BungeeSystem instance) {
        this.instance = instance;
    }

    @EventListener
    public void onCoreServerChangeStatus(CloudServiceInfoUpdateEvent e) {
        if(e.getServiceInfo().getConfiguration().getProcessConfig().getEnvironment().isMinecraftProxy() || e.getServiceInfo().getConfiguration().getProcessConfig().getEnvironment().isMinecraftJavaProxy())
            return;
        ICoreServerManager coreServerManager = instance.getCoreServerManager();

        ServiceInfoSnapshot serviceInfoSnapshot = e.getServiceInfo();
        ServerConnectionStatus serverConnectionStatus = ServerConnectionStatus.OFFLINE;
        Optional<Boolean> isOnline = serviceInfoSnapshot.getProperty(BridgeServiceProperty.IS_ONLINE);
        Optional<Boolean> isStarting = serviceInfoSnapshot.getProperty(BridgeServiceProperty.IS_STARTING);
        if(isStarting.isPresent() && isStarting.get())
            serverConnectionStatus = ServerConnectionStatus.STARTING;
        else if(isOnline.isPresent() && isOnline.get())
            serverConnectionStatus = ServerConnectionStatus.ONLINE;

        if(coreServerManager.getCoreServer(serviceInfoSnapshot.getName()) == null)
            return;
        ICoreServer coreServer = coreServerManager.getCoreServer(serviceInfoSnapshot.getName());
        if(coreServer.getConnectionInfo().getConnectionStatus() != serverConnectionStatus)
            coreServer.getConnectionInfo().setConnectionStatus(serverConnectionStatus);
    }

}
