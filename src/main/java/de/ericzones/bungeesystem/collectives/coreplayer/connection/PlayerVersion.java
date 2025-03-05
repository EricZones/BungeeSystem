// Created by Eric B. 08.02.2021 17:43
package de.ericzones.bungeesystem.collectives.coreplayer.connection;

public enum PlayerVersion {

    VERSION8(47, "1.8.x", "Bountiful Update", 2015), // 1.8.x
    VERSION9_0(107, "1.9.0", "Combat Update", 2016), // 1.9.0
    VERSION9_1(108, "1.9.1", "Combat Update", 2016), // 1.9.1
    VERSION9_2(109, "1.9.2", "Combat Update", 2016), // 1.9.2
    VERSION9_3_4(110, "1.9.3|4", "Combat Update", 2016), // 1.9.3 | 1.9.4
    VERSION10(210, "1.10.x", "Frostburn Update", 2016), // 1.10.x
    VERSION11_0(315, "1.11.0", "Exploration Update", 2016), // 1.11.0
    VERSION11_1_2(316, "1.11.1|2", "Exploration Update", 2016), // 1.11.1 | 1.11.2
    VERSION12_0(335, "1.12.0", "World of Color Update", 2017), // 1.12.0
    VERSION12_1(338, "1.12.1", "World of Color Update", 2017), // 1.12.1
    VERSION12_2(340, "1.12.2", "World of Color Update", 2017), // 1.12.2
    VERSION13_0(393, "1.13.0", "Aquatic Update", 2018), // 1.13.0
    VERSION13_1(401, "1.13.1", "Aquatic Update", 2018), // 1.13.1
    VERSION13_2(404, "1.13.2", "Aquatic Update", 2018), // 1.13.2
    VERSION14_0(477, "1.14.0", "Village & Pillage Update", 2019), // 1.14.0
    VERSION14_1(480, "1.14.1", "Village & Pillage Update", 2019), // 1.14.1
    VERSION14_2(485, "1.14.2", "Village & Pillage Update", 2019), // 1.14.2
    VERSION14_3(490, "1.14.3", "Village & Pillage Update", 2019), // 1.14.3
    VERSION14_4(498, "1.14.4", "Village & Pillage Update", 2019), // 1.14.4
    VERSION15_0(573, "1.15.0", "Buzzy Bees Update", 2019), // 1.15.0
    VERSION15_1(575, "1.15.1", "Buzzy Bees Update", 2019), // 1.15.1
    VERSION15_2(578, "1.15.2", "Buzzy Bees Update", 2020), // 1.15.2
    VERSION16_0(735, "1.16.0", "Nether Update", 2020), // 1.16.0
    VERSION16_1(736, "1.16.1", "Nether Update", 2020), // 1.16.1
    VERSION16_2(751, "1.16.2", "Nether Update", 2020), // 1.16.2
    VERSION16_3(753, "1.16.3", "Nether Update", 2020), // 1.16.3
    VERSION16_4_5(754, "1.16.4|5", "Nether Update", 2021), // 1.16.4 | 1.16.5
    VERSION17_0(755, "1.17.0", "Caves & Cliffs Update", 2021); // 1.17.0

    private PlayerVersion(int versionId, String versionName, String updateName, int releaseYear) {
        this.versionId = versionId;
        this.versionName = versionName;
        this.updateName = updateName;
        this.releaseYear = releaseYear;
    }
    
    private int versionId;
    private String versionName;
    private String updateName;
    private int releaseYear;

    public int getVersionId() {
        return versionId;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getUpdateName() {
        return updateName;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public static PlayerVersion getPlayerVersionById(int versionId) {
        for(PlayerVersion current : PlayerVersion.values()) {
            if(current.getVersionId() == versionId) return current;
        }
        return null;
    }

    public static PlayerVersion getPlayerVersionByName(String versionName) {
        for(PlayerVersion current : PlayerVersion.values()) {
            if(current.getVersionName().equals(versionName)) return current;
        }
        return null;
    }
}
