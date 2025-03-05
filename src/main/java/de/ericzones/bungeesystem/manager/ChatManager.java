// Created by Eric B. 13.02.2021 22:10
package de.ericzones.bungeesystem.manager;

import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.command.CommandExecuteEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
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

import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatManager {

    private final BungeeSystem instance;

    private final ChatMessageHandler chatMessageHandler;
    private final LanguageHandler languageHandler;
    private final PermissionHandler permissionHandler;
    private final DisconnectMessageHandler disconnectMessageHandler;

    private final Map<UUID, Long> playerDelayCache = new HashMap<>();
    private final Map<UUID, Pair<Long, String>> playerRepeatCache = new HashMap<>();
    private final Map<UUID, Pair<Integer, Long>> playerComplainsCache = new HashMap<>();

    private final String allowedSymbols = "|<>-_.:,;#'+*~´`?ß\\=}])([{/&%$!€@^°³²\" ";
    private final String[] socialCommands = new String[]{"/p", "/msg", "/r", "/reply"};

    public ChatManager(BungeeSystem instance) {
        this.instance = instance;
        this.chatMessageHandler = instance.getChatMessageHandler();
        this.languageHandler = instance.getLanguageHandler();
        this.permissionHandler = instance.getPermissionHandler();
        this.disconnectMessageHandler = instance.getDisconnectMessageHandler();
        EventManager eventManager = instance.getProxyServer().getEventManager();
        eventManager.register(instance, this);
    }

    @Subscribe
    public void onPlayerChat(PlayerChatEvent e) {
        ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(e.getPlayer().getUniqueId());
        if(corePlayer == null) return;
        Language language = corePlayer.getLanguage();

        if(corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.IGNORECHATPROTECTIONS))) {
            e.setResult(PlayerChatEvent.ChatResult.message(getReplacedSymbolsMessage(e.getMessage())));
            return;
        }

        if(isChatDelayed(corePlayer.getUniqueID())) {
            if(hasReachedComplainsLimit(corePlayer.getUniqueID())) {
                e.setResult(PlayerChatEvent.ChatResult.denied());
                corePlayer.disconnect(disconnectMessageHandler.getDisconnectMessage(DisconnectMessageType.KICKED,
                        languageHandler.getTranslatedMessage(Message.PUNISH_KICKED_SPAMMING, language), "", language));
                return;
            }
            e.setResult(PlayerChatEvent.ChatResult.denied());
            corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.SENTMESSAGETOOFAST, language));
            return;
        }
        if(hasRepeatedMessage(corePlayer.getUniqueID(), e.getMessage())) {
            e.setResult(PlayerChatEvent.ChatResult.denied());
            corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.REPEATEDCHATMESSAGE, language));
            return;
        }
        if(containsForbiddenSymbols(e.getMessage())) {
            e.setResult(PlayerChatEvent.ChatResult.denied());
            corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.SENTFORBIDDENSYMBOLS, language));
            return;
        }
        String chatMessage = getLowerCaseMessage(e.getMessage());
        chatMessage = getReplacedSymbolsMessage(chatMessage);
        e.setResult(PlayerChatEvent.ChatResult.message(chatMessage));
    }

    @Subscribe
    public void onPlayerExecuteCommand(CommandExecuteEvent e) {
        if(!(e.getCommandSource() instanceof Player))
            return;
        Player player = (Player) e.getCommandSource();
        ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(player.getUniqueId());
        if(corePlayer == null) return;
        Language language = corePlayer.getLanguage();

        if(!isSocialCommand(e.getCommand())) return;
        if(corePlayer.hasPermission(permissionHandler.getPermission(PermissionType.IGNORECHATPROTECTIONS))) return;

        String command = e.getCommand();
        for(int i = 0; i < socialCommands.length; i++)
            command = command.replace(socialCommands[i]+" ", "");

        if(isChatDelayed(corePlayer.getUniqueID())) {
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
        if(hasRepeatedMessage(corePlayer.getUniqueID(), command)) {
            e.setResult(CommandExecuteEvent.CommandResult.denied());
            corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.REPEATEDCHATMESSAGE, language));
            return;
        }
        if(containsForbiddenSymbols(command)) {
            e.setResult(CommandExecuteEvent.CommandResult.denied());
            corePlayer.sendMessage(chatMessageHandler.getChatMessage(ChatMessageType.SENTFORBIDDENSYMBOLS, language));
            return;
        }
        e.setResult(CommandExecuteEvent.CommandResult.command(getLowerCaseMessage(e.getCommand())));
    }



    private boolean isSocialCommand(String command) {
        for(int i = 0; i < socialCommands.length; i++) {
            if(command.toLowerCase().startsWith(socialCommands[i]+" "))
                return true;
        }
        return false;
    }

    private String getReplacedSymbolsMessage(String message) {
        return message.replace("%", "%%").replace("<3", "❤");
    }

    private String getLowerCaseMessage(String message) {
        int uppercaseLetters = 0;
        for(int i = 0; i < message.length(); i++) {
            if (Character.isUpperCase(message.charAt(i)) && Character.isLetter(message.charAt(i)))
                uppercaseLetters++;
        }
        if((float) uppercaseLetters / (float) message.length() > 0.3F)
            return message.toLowerCase();
        return message;
    }

    private boolean containsForbiddenSymbols(String message) {
        for(int i = 0; i < message.length(); i++) {
            boolean isNumber = false;
            try {
                int number = Integer.parseInt(String.valueOf(message.charAt(i)));
                isNumber = true;
            } catch (NumberFormatException e) {}
            if(!(Character.isAlphabetic(message.charAt(i)) && isNumber && allowedSymbols.contains(CharBuffer.wrap(new char[]{message.charAt(i)}))))
                return true;
        }
        return false;
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
            playerDelayCache.put(uuid, System.currentTimeMillis()+2*1000);
            return false;
        }
        if(playerDelayCache.get(uuid) > System.currentTimeMillis())
            return true;
        playerDelayCache.put(uuid, System.currentTimeMillis()+2*1000);
        return false;
    }

    private boolean hasRepeatedMessage(UUID uuid, String message) {
        Map<UUID, Pair<Long, String>> repeat = new HashMap<>(this.playerRepeatCache);
        for(UUID current : repeat.keySet()) {
            if (this.playerRepeatCache.get(current).getFirstObject() < System.currentTimeMillis()) this.playerRepeatCache.remove(current);
        }

        if(!playerRepeatCache.containsKey(uuid)) {
            playerRepeatCache.put(uuid, new Pair<>(System.currentTimeMillis()+30*1000, message));
            return false;
        }
        if(playerRepeatCache.get(uuid).getSecondObject().equalsIgnoreCase(message))
            return true;
        playerRepeatCache.put(uuid, new Pair<>(System.currentTimeMillis()+30*1000, message));
        return false;
    }

}
