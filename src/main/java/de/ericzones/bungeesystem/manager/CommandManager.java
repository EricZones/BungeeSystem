// Created by Eric B. 14.02.2021 18:39
package de.ericzones.bungeesystem.manager;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.command.PlayerAvailableCommandsEvent;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.Player;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.object.Pair;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.chatmessage.ChatMessageHandler;
import de.ericzones.bungeesystem.global.messaging.chatmessage.ChatMessageType;
import de.ericzones.bungeesystem.global.messaging.disconnectmessage.DisconnectMessageHandler;
import de.ericzones.bungeesystem.global.messaging.disconnectmessage.DisconnectMessageType;
import de.ericzones.bungeesystem.global.permission.PermissionHandler;
import de.ericzones.bungeesystem.global.permission.PermissionType;

import java.util.*;

public class CommandManager {

    private final BungeeSystem instance;
    private final ChatMessageHandler chatMessageHandler;
    private final PermissionHandler permissionHandler;
    private final DisconnectMessageHandler disconnectMessageHandler;
    private final LanguageHandler languageHandler;

    private final String[] disabledCommands = new String[]{"server", "glist", "alert", "alertraw", "end", "find", "greload", "ip",
            "perms", "send", "bungee", "reload", "rl", "stop", "version", "ver", "help", "minecraft:help", "pardon", "ban-ip",
            "me", "tell", "?", "minecraft:tell", "minecraft:me", "list", "minecraft:list", "pardon-ip", "say", "tellraw",
            "minecraft:tellraw", "minecraft:say", "minecraft:ban", "minecraft:ban-ip", "minecraft:pardon", "minecraft:pardon-ip"};
    private final String[] restrictedCommands = new String[]{"pl", "plugins", "tps", "spigot:tps", "velocity", "timings", "bukkit:timings",
            "op", "minecraft:op", "deop", "minecraft:deop"};

    private final Map<UUID, Long> playerDelayCache = new HashMap<>();
    private final Map<UUID, Pair<Integer, Long>> playerComplainsCache = new HashMap<>();

    public CommandManager(BungeeSystem instance) {
        this.instance = instance;
        this.chatMessageHandler = instance.getChatMessageHandler();
        this.permissionHandler = instance.getPermissionHandler();
        this.disconnectMessageHandler = instance.getDisconnectMessageHandler();
        this.languageHandler = instance.getLanguageHandler();
        EventManager eventManager = instance.getProxyServer().getEventManager();
        eventManager.register(instance, this);
    }

    @Subscribe
    public void onPlayerExecuteCommand(CommandExecuteEvent e) {
        if(!(e.getCommandSource() instanceof Player))
            return;
        Player player = (Player) e.getCommandSource();
        ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(player.getUniqueId());
        if(corePlayer == null) return;
        Language language = corePlayer.getLanguage();

        if(isChatDelayed(corePlayer.getUniqueID()) && !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.IGNORECHATPROTECTIONS))) {
            if(hasReachedComplainsLimit(corePlayer.getUniqueID())) {
                e.setResult(CommandExecuteEvent.CommandResult.denied());
                corePlayer.disconnect(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.KICKED,
                        languageHandler.getTranslatedMessage(Message.PUNISH_KICKED_SPAMMING, language), "", language));
                return;
            }
            e.setResult(CommandExecuteEvent.CommandResult.denied());
            corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.SENTMESSAGETOOFAST, language));
            return;
        }
        if(isDisabledCommand(e.getCommand())) {
            e.setResult(CommandExecuteEvent.CommandResult.denied());
            corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOCOMMAND, language));
            return;
        }
        if(isRestrictedCommand(e.getCommand()) && !corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.ACCESRESTRICTEDCOMMANDS))) {
            e.setResult(CommandExecuteEvent.CommandResult.denied());
            corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.NOCOMMAND, language));
            return;
        }
    }

    private boolean hasReachedComplainsLimit(UUID uuid) {
        Map<UUID, Pair<Integer, Long>> complains = new HashMap<>(playerComplainsCache);
        for(UUID current : complains.keySet()) {
            if (this.playerComplainsCache.get(current).getSecondObject() < System.currentTimeMillis()) this.playerComplainsCache.remove(current);
        }

        if(!playerComplainsCache.containsKey(uuid)) {
            playerComplainsCache.put(uuid, new Pair<>());
            playerComplainsCache.get(uuid).setFirstObject(1);
            playerComplainsCache.get(uuid).setSecondObject(System.currentTimeMillis()+5*60*1000);
            return false;
        }
        int complainsCount = playerComplainsCache.get(uuid).getFirstObject()+1;
        if(complainsCount >= 5) {
            playerComplainsCache.remove(uuid);
            return true;
        }
        playerComplainsCache.get(uuid).setFirstObject(complainsCount);
        return false;
    }

    private boolean isChatDelayed(UUID uuid) {
        Map<UUID, Long> delay = new HashMap<>(this.playerDelayCache);
        for(UUID current : delay.keySet()) {
            if (this.playerDelayCache.get(current) < System.currentTimeMillis()) this.playerDelayCache.remove(current);
        }

        if(!playerDelayCache.containsKey(uuid)) {
            playerDelayCache.put(uuid, System.currentTimeMillis()+ 1000);
            return false;
        }
        if(playerDelayCache.get(uuid) > System.currentTimeMillis())
            return true;
        playerDelayCache.put(uuid, System.currentTimeMillis()+ 1000);
        return false;
    }

    private boolean isRestrictedCommand(String command) {
        for(int i = 0; i < restrictedCommands.length; i++) {
            if(command.toLowerCase().startsWith(restrictedCommands[i]+" ") || command.equalsIgnoreCase(restrictedCommands[i]))
                return true;
        }
        return false;
    }

    private boolean isDisabledCommand(String command) {
        for(int i = 0; i < disabledCommands.length; i++) {
            if(command.toLowerCase().startsWith(disabledCommands[i]+" ") || command.equalsIgnoreCase(disabledCommands[i]))
                return true;
        }
        return false;
    }

}
