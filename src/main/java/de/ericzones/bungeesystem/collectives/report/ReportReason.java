// Created by Eric B. 15.02.2021 12:08
package de.ericzones.bungeesystem.collectives.report;

import de.ericzones.bungeesystem.global.language.Language;

import java.util.ArrayList;
import java.util.List;

public enum ReportReason {

    USERNAME("Namensgebung", "Naming", 1),
    APPEARANCE("Skin/Cape", "Skin/Cape", 1),
    BUILDING("Bauwerk", "Building", 1),
    SPAWNTRAPPING("Spawntrapping", "Spawntrapping", 1),
    RANDOMKILLING("Randomkilling", "Randomkilling", 1),
    TEAMING("Teaming", "Teaming", 1),
    TROLLING("Trolling", "Trolling", 1),
    BUGUSING("Bugusing", "Bugusing", 1),
    HACKING("Hacking", "Hacking", 1),

    ADVERTISING("Werbung", "Advertising", 3),

    SPAM("Spam", "Spam", 2),
    PROVOCATION("Provokation", "Provocation", 2),
    WORDING("Wortwahl", "Wording", 2),
    INSULT("Beleidigung", "Insult", 2),
    RACISM("Rassismus", "Racism", 2),
    CHATBOT("Chatbot", "Chatbot", 2);

    private ReportReason(String name, String englishName, int typeId) {
        this.name = name;
        this.englishName = englishName;
        this.typeId = typeId;
    }

    private String name;
    private String englishName;
    private int typeId;

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

    public int getTypeId() {
        return typeId;
    }

    public static List<ReportReason> getReasonsByTypeId(int typeId) {
        List<ReportReason> reasons = new ArrayList<>();
        for(ReportReason current : ReportReason.values()) {
            if(current.getTypeId() == typeId || current.getTypeId() == 3) reasons.add(current);
        }
        return reasons;
    }

    public static ReportReason getReasonByString(String reportReason, Language language) {
        for(ReportReason current : ReportReason.values()) {
            if(current.getName(language).equalsIgnoreCase(reportReason)) return current;
        }
        return null;
    }

}
