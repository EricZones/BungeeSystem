// Created by Eric B. 31.01.2021 16:30
package de.ericzones.bungeesystem.collectives.coreplayer.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayerManager;
import de.ericzones.bungeesystem.collectives.coreplayer.SqlCorePlayer;
import de.ericzones.bungeesystem.collectives.database.ISqlAdapter;

import java.util.UUID;

public class CorePlayerDisconnectEvent extends SqlCorePlayer {

    private final BungeeSystem instance;

    public CorePlayerDisconnectEvent(BungeeSystem instance, ISqlAdapter sqlAdapter) {
        super(sqlAdapter);
        this.instance = instance;
    }

    @Subscribe
    public void onCorePlayerDisconnect(DisconnectEvent e) {
        ICorePlayerManager corePlayerManager = instance.getCorePlayerManager();
        UUID uuid = e.getPlayer().getUniqueId();
        ICorePlayer corePlayer = corePlayerManager.getCorePlayer(uuid);
        if(corePlayer == null)
            return;
        corePlayerManager.removeCorePlayer(corePlayer);
    }

}
