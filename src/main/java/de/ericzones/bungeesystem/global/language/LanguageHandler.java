// Created by Eric B. 13.02.2021 14:32
package de.ericzones.bungeesystem.global.language;

import de.ericzones.bungeesystem.collectives.object.Pair;

import java.util.Objects;

public class LanguageHandler {

    private final Pair<Integer, String[]>[] messages = new Pair[Message.values().length];

    public LanguageHandler() {
        registerMessages();
    }

    public String getTranslatedMessage(Message message, Language language) {
        int translationId = language.getId();
        return messages[message.getId()].getSecondObject()[translationId];
    }

    private void registerMessages() {
        for(int i = 0; i < messages.length; i++) {
            messages[i] = new Pair<>();
            messages[i].setFirstObject(i);
            messages[i].setSecondObject(Objects.requireNonNull(Message.getMessageFromId(i)).getTranslations());
        }
    }

}
