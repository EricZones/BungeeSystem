// Created by Eric B. 07.02.2021 18:17
package de.ericzones.bungeesystem.global.permission;

public enum PermissionType {

    FULLSERVERJOIN("proxy.connect.full", getCurrentId()),
    MAINTENANCESERVERJOIN("proxy.connect.maintenance", getCurrentId()),
    RESTRICTEDSERVERJOIN("proxy.connect.restricted", getCurrentId()),

    IGNORECHATPROTECTIONS("proxy.chat.admin", getCurrentId()),

    UNLIMITEDCONNECTIONS("proxy.connect.unlimited", getCurrentId()),
    NOCONNECTIONDELAY("proxy.connect.ignoredelay", getCurrentId()),

    ACCESRESTRICTEDCOMMANDS("proxy.command.restricted", getCurrentId()),

    MANAGEREPORTS("proxy.report.manage", getCurrentId()),

    COMMAND_PROXY("proxy.command.proxy", getCurrentId()),
    COMMAND_PING_OTHER("proxy.command.ping.other", getCurrentId()),
    COMMAND_INFO("proxy.command.info", getCurrentId()),
    COMMAND_CHATCLEAR("proxy.command.chatclear", getCurrentId()),
    COMMAND_CHATCLEAR_PROXY("proxy.command.chatclear.proxy", getCurrentId()),
    COMMAND_ONTIME_RESET("proxy.command.ontime.reset", getCurrentId()),
    COMMAND_ONTIME_GET("proxy.command.ontime.get", getCurrentId()),
    COMMAND_ONTIME_TOP("proxy.command.ontime.top", getCurrentId()),
    COMMAND_COINS_GET("proxy.command.coins.get", getCurrentId()),
    COMMAND_COINS_ADMIN("proxy.command.coins.admin", getCurrentId()),
    COMMAND_JOINME("proxy.command.joinme", getCurrentId()),
    COMMAND_JOINME_LIST("proxy.command.joinme.list", getCurrentId()),
    COMMAND_SOCIALSPY("proxy.command.socialspy", getCurrentId()),
    COMMAND_BAN_LIST("proxy.command.ban.list", getCurrentId()),
    COMMAND_BAN_CHECK("proxy.command.ban.check", getCurrentId()),
    COMMAND_BAN("proxy.command.ban", getCurrentId()),
    COMMAND_UNBAN("proxy.command.unban", getCurrentId()),
    COMMAND_MUTE_LIST("proxy.command.mute.list", getCurrentId()),
    COMMAND_MUTE_CHECK("proxy.command.mute.check", getCurrentId()),
    COMMAND_MUTE("proxy.command.mute", getCurrentId()),
    COMMAND_UNMUTE("proxy.command.unmute", getCurrentId()),

    PUNISH_IGNOREBANS("proxy.punish.ban.ignore", getCurrentId()),
    PUNISH_IGNOREMUTES("proxy.punish.mute.ignore", getCurrentId()),

    FRIEND_COUNT_UNLIMITED("proxy.friend.count.unlimited", getCurrentId()),
    FRIEND_COUNT_PREMIUM("proxy.friend.count.premium", getCurrentId()),
    FRIEND_IGNORESETTINGS("proxy.friend.settings.ignore", getCurrentId()),

    PARTY_COUNT_PREMIUM("proxy.party.count.premium", getCurrentId()),
    PARTY_COUNT_UNLIMITED("proxy.party.count.unlimited", getCurrentId());

    private PermissionType(String standardPermission, int id) {
        this.standardPermission = standardPermission;
        this.id = id;
    }

    private static int currentId = 0;
    private String standardPermission;
    private int id;

    public String getStandardPermission() {
        return standardPermission;
    }

    public int getId() {
        return id;
    }

    public static PermissionType getPermissionTypeFromId(int id) {
        for(PermissionType current : PermissionType.values()) {
            if (current.getId() == id) return current;
        }
        return null;
    }

    private static int getCurrentId() {
        currentId++;
        return currentId-1;
    }
}
