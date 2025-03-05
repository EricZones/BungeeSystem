// Created by Eric B. 07.02.2021 22:28
package de.ericzones.bungeesystem.collectives.server.whitelist;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Whitelist {

    private WhitelistType whitelistType;
    private List<UUID> whitelistedUUIDs;

    public Whitelist(WhitelistType whitelistType) {
        this.whitelistType = whitelistType;
        this.whitelistedUUIDs = new ArrayList<>();
    }

    public WhitelistType getWhitelistType() {
        return whitelistType;
    }

    public boolean containsUUID(UUID uuid) {
        return whitelistedUUIDs.contains(uuid);
    }

    public void addToWhitelist(UUID uuid) {
        whitelistedUUIDs.add(uuid);
    }

    public void removeFromWhitelist(UUID uuid) {
        whitelistedUUIDs.remove(uuid);
    }

    public enum WhitelistType {

        RESTRICTED,
        MAINTENANCE,
        NONE;

        private WhitelistType(){}

    }
}
