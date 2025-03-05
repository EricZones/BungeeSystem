// Created by Eric B. 07.02.2021 16:39
package de.ericzones.bungeesystem.global.messaging.disconnectmessage;

public enum DisconnectMessageType {

    REJECTED(new String[]{"Verbindung abgelehnt", "Information"}, new String[]{"Connection rejected", "Information"}, 0),
    DISCONNECTED(new String[]{"Verbindung getrennt", "Information"}, new String[]{"Connection lost", "Information"},  1),
    LOBBIESFULL(new String[]{"Lobbys sind voll", "Information"}, new String[]{"Lobbies full", "Information"}, 2),
    BANNED(new String[]{"Du wurdest gebannt", "Grund", "Dauer"}, new String[]{"You have been banned", "Reason", "Duration"}, 3),
    ISBANNED(new String[]{"Du bist gebannt", "Grund", "Restzeit"}, new String[]{"You are banned", "Reason", "Remaining time"}, 4),
    KICKED(new String[]{"Du wurdest gekickt", "Grund"}, new String[]{"You have been kicked", "Reason"}, 5);

    private DisconnectMessageType(String[] standardMessages, String[] englishMessages, int id) {
        this.standardMessages = standardMessages;
        this.englishMessages = englishMessages;
        this.id = id;
    }

    private String[] standardMessages;
    private String[] englishMessages;
    private int id;

    public String[] getStandardMessages() {
        return standardMessages;
    }

    public String[] getEnglishMessages() {
        return englishMessages;
    }

    public int getId() {
        return id;
    }

    public static DisconnectMessageType getDisconnectMessageTypeFromId(int id) {
        for(DisconnectMessageType current : DisconnectMessageType.values()) {
            if (current.getId() == id) return current;
        }
        return null;
    }
}
