// Created by Eric B. 18.02.2021 14:59
package de.ericzones.bungeesystem.collectives.friend;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum FriendProperty {

    REQUESTS,
    PARTYINVITES,
    MESSAGES,
    JUMPING,
    STATUS,
    FRIENDSTATUS,
    JOINMES;

    private FriendProperty(){}

    public static Map<FriendProperty, FriendProperty.Setting> getDefaultSettings() {
        Map<FriendProperty, FriendProperty.Setting> map = new HashMap<>();
        map.put(FriendProperty.REQUESTS, Setting.ENABLED);
        map.put(FriendProperty.PARTYINVITES, Setting.ENABLED);
        map.put(FriendProperty.MESSAGES, Setting.FRIENDS);
        map.put(FriendProperty.JUMPING, Setting.FRIENDS);
        map.put(FriendProperty.STATUS, Setting.FRIENDS);
        map.put(FriendProperty.FRIENDSTATUS, Setting.ENABLED);
        map.put(FriendProperty.JOINMES, Setting.ENABLED);
        return map;
    }

    public static String[] getDefaultValues(UUID uuid) {
        return new String[]{uuid.toString(), Setting.ENABLED.toString(), Setting.ENABLED.toString(), Setting.FRIENDS.toString(), Setting.FRIENDS.toString(),
                            Setting.FRIENDS.toString(), Setting.ENABLED.toString(), Setting.ENABLED.toString()};
    }

    public enum Setting {

        ENABLED,
        FRIENDS,
        DISABLED;

        private Setting(){}

    }

}
