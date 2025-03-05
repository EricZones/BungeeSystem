// Created by Eric B. 07.02.2021 11:23
package de.ericzones.bungeesystem.global.messaging.chatmessage;

public enum ChatMessageType {

    NOCONSOLE("§cDieser Befehl ist nur als Spieler ausführbar", "§cThis command can be executed by players only", 0),
    NOPERMS("§7Fehlende §cRechte §7für diesen Befehl", "§7Missing §cpermissions §7for this command", 1),
    NOCOMMAND("§7Dieser §cBefehl §7existiert nicht", "§7This §ccommand §7does not exist", 2),
    NOSERVER("§7Dieser §cServer §7wurde nicht gefunden", "§7This §cserver §7was not found", 3),
    NOTONLINE("§7Dieser §cSpieler §7wurde nicht gefunden", "§7This §cplayer §7was not found", 4),
    ALREADYCONNECTED("§7Du bist bereits auf diesem Server", "§7You are already connected to this server", 5),
    TARGETALREADYCONNECTED("§7Dieser §cSpieler §7ist bereits auf diesem Server",
            "§7This §cplayer §7is already connected to this server", 6),

    KICKEDBYPREMIUM("§7Ein Spieler mit §6Premium§7, §dContent §7oder ein §bTeammitglied §7hat den vollen Server betreten",
            "§7A player with §6Premium§c, §dContent §cor a §bTeammember §centered this full server", 7),
    MUTEDINCHAT("§cDu wurdest aus dem Chat ausgeschlossen", "§cYou have been excluded from the chat", 8),
    ISMUTEDINCHAT("§cDu bist aus dem Chat ausgeschlossen", "§cYou are excluded from the chat", 9),

    REPEATEDCHATMESSAGE("§7Du wiederholst dich...", "§7You are repeating yourself...", 10),
    SENTMESSAGETOOFAST("§7Du schreibst zu schnell...", "§7You write too fast...", 11),
    SENTFORBIDDENSYMBOLS("§7Bitte verwende keine Sonderzeichen...", "§7Please do not use symbols...", 12),

    ERROR_COMMAND("§7Ein §cFehler §7ist aufgetreten", "§7An §cerror §7has occurred", 13);


    private ChatMessageType(String standardMessage, String englishMessage, int id) {
        this.standardMessage = standardMessage;
        this.englishMessage = englishMessage;
        this.id = id;
    }

    private String standardMessage;
    private String englishMessage;
    private int id;

    public String getStandardMessage() {
        return standardMessage;
    }

    public String getEnglishMessage() {
        return englishMessage;
    }

    public int getId() {
        return id;
    }

    public static ChatMessageType getChatMessageTypeFromId(int id) {
        for(ChatMessageType current : ChatMessageType.values()) {
            if (current.getId() == id) return current;
        }
        return null;
    }
}
