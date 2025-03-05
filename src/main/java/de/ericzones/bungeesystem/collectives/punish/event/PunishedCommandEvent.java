// Created by Eric B. 12.02.2021 23:24
package de.ericzones.bungeesystem.collectives.punish.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.proxy.Player;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.punish.IPunishManager;
import de.ericzones.bungeesystem.collectives.punish.mute.Mute;
import de.ericzones.bungeesystem.global.messaging.chatmessage.ChatMessageHandler;
import de.ericzones.bungeesystem.global.messaging.chatmessage.ChatMessageType;

public class PunishedCommandEvent {

    private final BungeeSystem instance;
    private final String[] mutedForbiddenCommands = new String[]{"p", "msg", "r", "reply"};

    public PunishedCommandEvent(BungeeSystem instance) {
        this.instance = instance;
    }

    @Subscribe
    public void onPunishedCommand(CommandExecuteEvent e) {
        if(!(e.getCommandSource() instanceof Player))
            return;
        Player player = (Player) e.getCommandSource();
        ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(player.getUniqueId());
        if(corePlayer == null) return;
        IPunishManager punishManager = instance.getPunishManager();
        ChatMessageHandler messageHandler = instance.getChatMessageHandler();
        if(!punishManager.isMuted(corePlayer)) return;

        for(int i = 0; i < mutedForbiddenCommands.length; i++) {
            if (!e.getCommand().toLowerCase().startsWith(mutedForbiddenCommands[i]+" ")) continue;
            e.setResult(CommandExecuteEvent.CommandResult.denied());
            Mute mute = punishManager.getMute(corePlayer);
            corePlayer.sendMessage(" ");
            corePlayer.sendMessage(messageHandler.getChatMessage(ChatMessageType.ISMUTEDINCHAT, corePlayer.getLanguage()));
            corePlayer.sendMessage(punishManager.getIsMutedChatMessage(mute, corePlayer));
            return;
        }
    }

}
