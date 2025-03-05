// Created by Eric B. 10.02.2021 14:36
package de.ericzones.bungeesystem.collectives.punish.ban;

import de.ericzones.bungeesystem.collectives.report.ReportReason;
import de.ericzones.bungeesystem.global.language.Language;

public enum BanReason {

    USERNAME("Namensgebung", "Naming", 432000),
    APPEARANCE("Skin/Cape", "Skin/Cape", 432000),
    BUILDING("Bauwerk", "Building", 86400),
    SPAWNTRAPPING("Spawntrapping", "Spawntrapping", 172800),
    RANDOMKILLING("Randomkilling", "Randomkilling", 172800),
    TEAMING("Teaming", "Teaming", 864000),
    TROLLING("Trolling", "Trolling", 86400),
    ADVERTISING("Werbung", "Advertising", 172800),
    BUGUSING("Bugusing", "Bugusing", 1296000),
    HACKING("Hacking", "Hacking", 2592000),
    CRASHCLIENT("Crashclient", "Crashclient", -1),
    BANBYPASS("Bannumgehung", "Banbypassing", -1);

    private BanReason(String name, String englishName, long duration) {
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

    public static BanReason getReasonByString(String banReason, Language language) {
        for(BanReason current : BanReason.values()) {
            if(current.getName(language).equalsIgnoreCase(banReason)) return current;
        }
        return null;
    }

}
