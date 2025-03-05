// Created by Eric B. 04.02.2021 21:29
package de.ericzones.bungeesystem.global.messaging.disconnectmessage;

import de.ericzones.bungeesystem.collectives.object.Pair;
import de.ericzones.bungeesystem.global.language.Language;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DisconnectMessageHandler {

    private Map<Language, Pair<Integer, String[]>[]> disconnectMessages = new HashMap<>();
    private Map<Language, String> disconnectHeader = new HashMap<>(), disconnectFooter = new HashMap<>();

    public DisconnectMessageHandler() {
        registerDisconnectMessages();
    }

    public String getDisconnectMessage(DisconnectMessageType disconnectMessageType, String text, String text2, Language language) {
        String disconnectMessage = null;
        String[] disconnectMessageArray = disconnectMessages.get(language)[disconnectMessageType.getId()].getSecondObject();
        switch (disconnectMessageArray.length) {
            case 2:
                disconnectMessage = disconnectHeader.get(language)+"§8• §c"+disconnectMessageArray[0]+" §8•\n\n§8§m--------------\n\n§8• §7"+
                                    disconnectMessageArray[1]+" §8•\n§c"+text+disconnectFooter.get(language);
                break;
            case 3:
                disconnectMessage = disconnectHeader.get(language)+"§8• §c"+disconnectMessageArray[0]+" §8•\n\n§8§m--------------\n\n§8• §7"+
                                    disconnectMessageArray[1]+" §8•\n§c"+text+"§r\n\n§8• §7"+disconnectMessageArray[2]+" §8•\n§c"+
                                    text2+disconnectFooter.get(language);
                break;
            default:
                break;
        }
        return disconnectMessage;
    }

    private void registerDisconnectMessages() {
        for(int i = 0; i < Language.values().length; i++)
            disconnectMessages.put(Language.getLanguageFromId(i), new Pair[DisconnectMessageType.values().length]);
        for(Language current : disconnectMessages.keySet()) {
            switch (current) {
                case GERMAN:
                    for(int i = 0; i < disconnectMessages.get(current).length; i++) {
                        disconnectMessages.get(current)[i] = new Pair<>();
                        disconnectMessages.get(current)[i].setFirstObject(i);
                        disconnectMessages.get(current)[i].setSecondObject(Objects.requireNonNull(DisconnectMessageType.getDisconnectMessageTypeFromId(i)).
                                getStandardMessages());
                    }
                    break;
                case ENGLISH:
                    for(int i = 0; i < disconnectMessages.get(current).length; i++) {
                        disconnectMessages.get(current)[i] = new Pair<>();
                        disconnectMessages.get(current)[i].setFirstObject(i);
                        disconnectMessages.get(current)[i].setSecondObject(Objects.requireNonNull(DisconnectMessageType.getDisconnectMessageTypeFromId(i)).
                                getEnglishMessages());
                    }
                    break;
                default:
                    break;
            }
        }
        for(int i = 0; i < Language.values().length; i++) {
            Language language = Language.getLanguageFromId(i);
            switch (language) {
                case GERMAN:
                    disconnectHeader.put(language, "§8•● §bEndux§8.§bnet §8× §7Netzwerk§r §8●•\n\n\n§8§m----------------------------§r\n");
                    disconnectFooter.put(language, "\n\n§8§m----------------------------\n\n\n§7Bei weiteren §bFragen §7melde dich im §bSupport\n§7TeamSpeak §8● §bEndux§8.§bnet");
                    break;
                case ENGLISH:
                    disconnectHeader.put(language, "§8•● §bEndux§8.§bnet §8× §7Network§r §8●•\n\n\n§8§m----------------------------§r\n");
                    disconnectFooter.put(language, "\n\n§8§m----------------------------\n\n\n§7For further §bquestions §7claim our §bSupport\n§7TeamSpeak §8● §bEndux§8.§bnet");
                    break;
                default:
                    break;
            }
        }
    }

}
