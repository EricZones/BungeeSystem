// Created by Eric B. 08.03.2021 11:35
package de.ericzones.bungeesystem.collectives.party;

import java.util.List;
import java.util.UUID;

public interface IPartyManager {

    List<Party> getParties();

    Party getPartyFromLeader(UUID uuid);
    Party getPartyFromMember(UUID uuid);
    Party getPartyFromPlayer(UUID uuid);
    Party getPartyById(UUID id);

    Party initialParty(UUID creator);
    void removeParty(Party party, PartyManager.PartyDeleteReason reason);

    boolean isInParty(UUID uuid);

    void invitePlayer(Party party, UUID uuid);
    void leaveParty(UUID uuid);
    void setPartyLeader(Party party, UUID uuid);
    void kickPlayer(UUID uuid);
    void acceptInvite(Party party, UUID uuid);

    int getStandardMemberAmount();
    int getPremiumMemberAmount();

}
