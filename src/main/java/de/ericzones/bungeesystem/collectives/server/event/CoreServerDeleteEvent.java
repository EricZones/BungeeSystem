// Created by Eric B. 31.01.2021 20:15
package de.ericzones.bungeesystem.collectives.server.event;

import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.driver.event.events.service.CloudServiceStopEvent;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.server.ICoreServer;
import de.ericzones.bungeesystem.collectives.server.ICoreServerManager;

public class CoreServerDeleteEvent {

    private final BungeeSystem instance;

    public CoreServerDeleteEvent(BungeeSystem instance) {
        this.instance = instance;
    }

    @EventListener
    public void onCoreServerDelete(CloudServiceStopEvent e) {
        if(e.getServiceInfo().getConfiguration().getProcessConfig().getEnvironment().isMinecraftProxy() || e.getServiceInfo().getConfiguration().getProcessConfig().getEnvironment().isMinecraftJavaProxy())
            return;
        ICoreServerManager coreServerManager = instance.getCoreServerManager();

        String serverName = e.getServiceInfo().getName();
        ICoreServer coreServer = coreServerManager.getCoreServer(serverName);
        coreServerManager.removeCoreServer(coreServer);
    }

}
