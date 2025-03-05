// Created by Eric B. 30.01.2021 23:34
package de.ericzones.bungeesystem.global.messaging.chatmessage;

import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixHandler;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ChatMessageHandler {

    private PluginPrefixHandler pluginPrefixHandler;
    private Map<Language, String[]> chatMessages = new HashMap<>();

    public ChatMessageHandler(PluginPrefixHandler pluginPrefixHandler) {
        this.pluginPrefixHandler = pluginPrefixHandler;
        registerChatMessages();
    }

    public String getChatMessage(ChatMessageType chatMessageType, Language language) {
        String chatMessage = chatMessages.get(language)[chatMessageType.getId()];
        switch (chatMessageType) {
            case NOCONSOLE:
                chatMessage = BungeeSystem.getInstance().getPluginName()+" "+chatMessage;
                break;
            case NOPERMS: case NOCOMMAND: case NOSERVER: case ALREADYCONNECTED: case TARGETALREADYCONNECTED: case KICKEDBYPREMIUM:
            case ERROR_COMMAND:
                chatMessage = pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PROXY)+chatMessage;
                break;
            case MUTEDINCHAT: case ISMUTEDINCHAT:
                chatMessage = pluginPrefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+chatMessage;
                break;
            case REPEATEDCHATMESSAGE: case SENTMESSAGETOOFAST: case SENTFORBIDDENSYMBOLS:
                chatMessage = pluginPrefixHandler.getPluginPrefix(PluginPrefixType.CHAT)+chatMessage;
                break;
            default:
                break;
        }
        return chatMessage;
    }

    private void registerChatMessages() {
        for(int i = 0; i < Language.values().length; i++)
            chatMessages.put(Language.getLanguageFromId(i), new String[ChatMessageType.values().length]);
        for(Language current : chatMessages.keySet()) {
                switch (current) {
                    case GERMAN:
                        for(int i = 0; i < chatMessages.get(current).length; i++)
                            chatMessages.get(current)[i] = Objects.requireNonNull(ChatMessageType.getChatMessageTypeFromId(i)).getStandardMessage();
                        break;
                    case ENGLISH:
                        for(int i = 0; i < chatMessages.get(current).length; i++)
                            chatMessages.get(current)[i] = Objects.requireNonNull(ChatMessageType.getChatMessageTypeFromId(i)).getEnglishMessage();
                        break;
                    default:
                        break;
            }
        }
    }

}
