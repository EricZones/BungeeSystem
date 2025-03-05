// Created by Eric B. 07.02.2021 11:12
package de.ericzones.bungeesystem.global.messaging.pluginprefix;

public enum PluginPrefixType {

    CLOUD("§3•§b● §bCloud §8§l┃§r", 0),
    PROXY("§3•§b● §bProxy §8§l┃§r", 1),
    SERVER("§3•§b● §bServer §8§l┃§r", 2),
    CHAT("§3•§b● §bChat §8§l┃§r", 3),
    FRIENDS("§3•§b● §bFriends §8§l┃§r", 4),
    PARTY("§3•§b● §bParty §8§l┃§r", 5),
    MESSAGE("§3•§b● §bMsg §8§l┃§r", 6),
    COINSYSTEM("§6•§e● §eCoins §8§l┃§r", 7),
    BANSYSTEM("§4•§c● §cPunish §8§l┃§r", 8),
    REPORTSYSTEM("§4•§c● §cReport §8§l┃§r", 9),
    ANTICHEAT("§4•§c● §cAntiCheat §8§l┃§r", 10),
    BUILDSYSTEM("§2•§a● §aBuildSystem §8§l┃§r", 11),

    REPORTSYSTEM_BC(" §8[§4!§8] §c§lReport§r §8●§r", 12),
    JOINME_BC(" §8[§3!§8] §b§lJoinme§r §8●§r", 13);

    private PluginPrefixType(String standardPrefix, int id){
        this.standardPrefix = standardPrefix;
        this.id = id;
    }
    private String standardPrefix;
    private int id;

    public String getStandardPrefix() {
        return standardPrefix;
    }

    public int getId() {
        return id;
    }

    public static PluginPrefixType getPluginPrefixTypeFromId(int id) {
        for(PluginPrefixType current : PluginPrefixType.values()) {
            if (current.getId() == id) return current;
        }
        return null;
    }
}
