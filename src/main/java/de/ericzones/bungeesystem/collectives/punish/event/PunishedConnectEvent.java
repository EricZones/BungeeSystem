// Created by Eric B. 11.02.2021 20:21
package de.ericzones.bungeesystem.collectives.punish.event;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.Player;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.collectives.punish.IPunishManager;
import de.ericzones.bungeesystem.collectives.punish.ban.Ban;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.messaging.disconnectmessage.DisconnectMessageHandler;
import de.ericzones.bungeesystem.global.messaging.disconnectmessage.DisconnectMessageType;
import net.kyori.adventure.text.Component;

public class PunishedConnectEvent {

    private final BungeeSystem instance;
    private final DisconnectMessageHandler disconnectMessageHandler;

    public PunishedConnectEvent(BungeeSystem instance) {
        this.instance = instance;
        this.disconnectMessageHandler = instance.getDisconnectMessageHandler();
    }

    @Subscribe
    public void onPunishedPreConnect(PreLoginEvent e) {
        IPunishManager punishManager = instance.getPunishManager();
        String ipAddress = e.getConnection().getRemoteAddress().getAddress().getHostAddress();
        if(punishManager.isBanned(ipAddress)) {
            Ban ban = punishManager.getBan(ipAddress);
            Language language = ban.getOfflineCorePlayer().getLanguage();
            e.setResult(PreLoginEvent.PreLoginComponentResult.denied(Component.text(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.ISBANNED,
                    ban.getReason().getName(language), ban.getExpiryCurrentName(language), language))));
            return;
        }
    }

    @Subscribe
    public void onPunishedConnect(LoginEvent e) {
        IPunishManager punishManager = instance.getPunishManager();
        Player player = e.getPlayer();
        IOfflineCorePlayer offlineCorePlayer = instance.getCorePlayerManager().getOfflineCorePlayer(player.getUniqueId());
        if(offlineCorePlayer == null) return;
        Language language = offlineCorePlayer.getLanguage();

        if(punishManager.isBanned(player.getUniqueId())) {
            Ban ban = punishManager.getBan(player.getUniqueId());
            e.setResult(ResultedEvent.ComponentResult.denied(Component.text(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.ISBANNED,
                    ban.getReason().getName(language), ban.getExpiryCurrentName(language), language))));
            return;
        }
    }

}
