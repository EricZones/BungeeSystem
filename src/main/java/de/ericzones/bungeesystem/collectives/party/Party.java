// Created by Eric B. 08.03.2021 11:35
package de.ericzones.bungeesystem.collectives.party;

import java.util.*;

public class Party {

    private final UUID id = UUID.randomUUID();
    private final List<UUID> members = new ArrayList<>();
    private final Map<UUID, Long> invites = new HashMap<>();
    private UUID leader;

    public Party(UUID creator) {
        this.leader = creator;
    }

    public UUID getId() {
        return this.id;
    }

    public UUID getLeaderUniqueId() {
        return this.leader;
    }

    public void setLeader(UUID uuid) {
        this.leader = uuid;
        if(uuid != null)
            removeMember(uuid);
        this.invites.clear();
    }

    public List<UUID> getMembers() {
        return this.members;
    }

    public void addMember(UUID uuid) {
        this.members.add(uuid);
        removeInvite(uuid);
    }

    public void removeMember(UUID uuid) {
        this.members.remove(uuid);
    }

    public boolean isMember(UUID uuid) {
        return this.members.contains(uuid);
    }

    public Map<UUID, Long> getInvites() {
        for(UUID current : this.invites.keySet()) {
            if(this.invites.get(current) < System.currentTimeMillis())
                removeInvite(current);
        }
        return this.invites;
    }

    public void createInvite(UUID uuid) {
        this.invites.put(uuid, System.currentTimeMillis() + 2*60*1000);
    }

    public void removeInvite(UUID uuid) {
        this.invites.remove(uuid);
    }

    public boolean isInvited(UUID uuid) {
        if(this.invites.containsKey(uuid) && this.invites.get(uuid) < System.currentTimeMillis())
            removeInvite(uuid);
        return this.invites.containsKey(uuid);
    }

    public UUID getRandomMember() {
        Random random = new Random();
        return this.members.get(random.nextInt(this.members.size()));
    }

}
