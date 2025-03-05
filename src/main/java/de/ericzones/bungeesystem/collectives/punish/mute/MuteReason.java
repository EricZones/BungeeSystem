// Created by Eric B. 12.02.2021 17:56
package de.ericzones.bungeesystem.collectives.punish.mute;

import de.ericzones.bungeesystem.collectives.report.ReportReason;
import de.ericzones.bungeesystem.global.language.Language;

public enum MuteReason {

    SPAM("Spam", "Spam", 86400),
    ADVERTISING("Werbung", "Advertising", 432000),
    PROVOCATION("Provokation", "Provocation", 432000),
    WORDING("Wortwahl", "Wording", 604800),
    INSULT("Beleidigung", "Insult", 864000),
    RACISM("Rassismus", "Racism", 1728000),
    CHATBOT("Chatbot", "Chatbot", -1);

    private MuteReason(String name, String englishName, long duration) {
        this.name = name;
        this.englishName = englishName;
        this.duration = duration;
    }

    private String name;
    private String englishName;
    private long duration;

    public String getName(Language language) {
        String name = null;
        switch (language) {
            case GERMAN:
                name = this.name;
                break;
            case ENGLISH:
                name = this.englishName;
                break;
            default:
                break;
        }
        return name;
    }

    public long getDuration() {
        return duration;
    }

    public static MuteReason getReasonByString(String muteReason, Language language) {
        for(MuteReason current : MuteReason.values()) {
            if(current.getName(language).equalsIgnoreCase(muteReason)) return current;
        }
        return null;
    }

}
