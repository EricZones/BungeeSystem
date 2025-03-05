// Created by Eric B. 12.02.2021 16:48
package de.ericzones.bungeesystem.collectives.punish.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.punish.IPunishManager;
import de.ericzones.bungeesystem.collectives.punish.mute.Mute;
import de.ericzones.bungeesystem.global.messaging.chatmessage.ChatMessageHandler;
import de.ericzones.bungeesystem.global.messaging.chatmessage.ChatMessageType;

public class PunishedChatEvent {

    private final BungeeSystem instance;

    public PunishedChatEvent(BungeeSystem instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onPunishedChat(PlayerChatEvent e) {
        ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(e.getPlayer().getUniqueId());
        if(corePlayer == null) return;
        IPunishManager punishManager = instance.getPunishManager();
        ChatMessageHandler messageHandler = instance.getChatMessageHandler();

        if(punishManager.isMuted(corePlayer)) {
            e.setResult(PlayerChatEvent.ChatResult.denied());
            Mute mute = punishManager.getMute(corePlayer);
            corePlayer.sendMessage(" ");
            corePlayer.sendMessage(messageHandler.getChatMessage(ChatMessageType.ISMUTEDINCHAT, corePlayer.getLanguage()));
            corePlayer.sendMessage(punishManager.getIsMutedChatMessage(mute, corePlayer));
            return;
        }
    }

}
