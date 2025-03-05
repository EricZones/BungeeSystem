// Created by Eric B. 08.03.2021 11:34
package de.ericzones.bungeesystem.collectives.party;

import com.velocitypowered.api.event.EventManager;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.collectives.object.TextBuilder;
import de.ericzones.bungeesystem.collectives.party.event.MemberDisconnectEvent;
import de.ericzones.bungeesystem.collectives.party.event.MemberSwitchServerEvent;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixHandler;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;
import net.kyori.adventure.text.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PartyManager implements IPartyManager {

    private final BungeeSystem instance;
    private final LanguageHandler languageHandler;
    private final PluginPrefixHandler pluginPrefixHandler;

    private final int standardMemberAmount = 5;
    private final int premiumMemberAmount = 9;

    private final List<Party> parties = new ArrayList<>();

    public PartyManager(BungeeSystem instance) {
        this.instance = instance;
        this.languageHandler = instance.getLanguageHandler();
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
        EventManager eventManager = instance.getProxyServer().getEventManager();
        eventManager.register(instance, new MemberDisconnectEvent(instance));
        eventManager.register(instance, new MemberSwitchServerEvent(instance));
    }

    @Override
    public List<Party> getParties() {
        return this.parties;
    }

    @Override
    public Party getPartyFromLeader(UUID uuid) {
        return this.parties.stream().filter(Party -> Party.getLeaderUniqueId().equals(uuid)).findFirst().orElse(null);
    }

    @Override
    public Party getPartyFromMember(UUID uuid) {
        return this.parties.stream().filter(Party -> Party.isMember(uuid)).findFirst().orElse(null);
    }

    @Override
    public Party getPartyFromPlayer(UUID uuid) {
        return this.parties.stream().filter(Party -> Party.isMember(uuid)).findFirst()
                .orElse(this.parties.stream().filter(Party -> Party.getLeaderUniqueId().equals(uuid)).findFirst().orElse(null));
    }

    @Override
    public Party getPartyById(UUID id) {
        return this.parties.stream().filter(Party -> Party.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public Party initialParty(UUID creator) {
        Party party = new Party(creator);
        this.parties.add(party);

        instance.getProxyServer().getScheduler().buildTask(instance, new Runnable() {
            @Override
            public void run() {
                Party targetParty = getPartyById(party.getId());
                if(targetParty != null && targetParty.getMembers().size() == 0)
                    removeParty(targetParty, PartyDeleteReason.NOMEMBERS);
            }
        }).delay(2L, TimeUnit.MINUTES).schedule();
        return party;
    }

    @Override
    public void removeParty(Party party, PartyDeleteReason reason) {
        this.parties.remove(party);
        switch (reason) {
            case DISBANDED:
                for(UUID current : party.getMembers()) {
                    ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(current);
                    if(corePlayer == null) continue;
                    Language language = corePlayer.getLanguage();
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(
                            Message.PARTY_DELETED_DISBANDED, language));
                }
                break;
            case NOMEMBERS:
                List<UUID> partyMembers = party.getMembers();
                if(party.getLeaderUniqueId() != null)
                    partyMembers.add(party.getLeaderUniqueId());
                for(UUID current : partyMembers) {
                    ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(current);
                    if(corePlayer == null) continue;
                    Language language = corePlayer.getLanguage();
                    corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(
                            Message.PARTY_DELETED_NOMEMBERS, language));
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isInParty(UUID uuid) {
        return this.parties.stream().anyMatch(Party -> Party.isMember(uuid) || Party.getLeaderUniqueId().equals(uuid));
    }

    @Override
    public void invitePlayer(Party party, UUID uuid) {
        ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(party.getLeaderUniqueId());
        ICorePlayer targetPlayer = instance.getCorePlayerManager().getCorePlayer(uuid);
        Language language = targetPlayer.getLanguage();

        party.createInvite(uuid);
        String message = languageHandler.getTranslatedMessage(Message.PARTY_INVITED, language).replace("%REPLACE%",
                corePlayer.getRankPrefix()+corePlayer.getUsername());
        targetPlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+message);
        TextComponent acceptText = new TextBuilder("§a§l"+languageHandler.getTranslatedMessage(Message.PARTY_INVITE_ACCEPT, language))
                .setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§8● ").setPostText(" §8● ")
                .setHoverText("§a"+languageHandler.getTranslatedMessage(Message.PARTY_INVITE_ACCEPT_HOVER, language))
                .setClickEvent(TextBuilder.Action.COMMAND, "party accept "+corePlayer.getUsername()).build();
        TextComponent denyText = new TextBuilder("§c§l"+languageHandler.getTranslatedMessage(Message.PARTY_INVITE_DENY, language))
                .setPostText(" §8●").setHoverText("§c"+languageHandler.getTranslatedMessage(Message.PARTY_INVITE_DENY_HOVER, language))
                .setClickEvent(TextBuilder.Action.COMMAND, "party deny "+corePlayer.getUsername()).build();
        targetPlayer.sendMessage(acceptText.append(denyText));
    }

    @Override
    public void leaveParty(UUID uuid) {
        IOfflineCorePlayer offlineCorePlayer = instance.getCorePlayerManager().getOfflineCorePlayer(uuid);
        Party party = getPartyFromPlayer(uuid);

        if(party.getLeaderUniqueId() != uuid) {
            party.removeMember(uuid);
            List<UUID> members = new ArrayList<>(party.getMembers()); members.add(party.getLeaderUniqueId());
            for(UUID current : members) {
                ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(current);
                if(corePlayer == null) continue;
                Language language = corePlayer.getLanguage();
                corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(
                        Message.PARTY_MEMBER_LEFT, language).replace("%REPLACE%", offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername()));
            }
            if(party.getMembers().size() == 0)
                removeParty(party, PartyDeleteReason.NOMEMBERS);
            return;
        }

        party.setLeader(null);
        for(UUID current : party.getMembers()) {
            ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(current);
            if(corePlayer == null) continue;
            Language language = corePlayer.getLanguage();
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(
                    Message.PARTY_LEADER_LEFT, language).replace("%REPLACE%", offlineCorePlayer.getRankPrefix()+offlineCorePlayer.getUsername()));
        }
        if(party.getMembers().size() == 0 || party.getMembers().size() == 1) {
            removeParty(party, PartyDeleteReason.NOMEMBERS);
            return;
        }
        setPartyLeader(party, party.getRandomMember());
    }

    @Override
    public void setPartyLeader(Party party, UUID uuid) {
        ICorePlayer leader = instance.getCorePlayerManager().getCorePlayer(uuid);
        party.setLeader(uuid);
        for(UUID current : party.getMembers()) {
            ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(current);
            if(corePlayer == null) continue;
            Language language = corePlayer.getLanguage();
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(
                    Message.PARTY_LEADER_CHANGED, language).replace("%REPLACE%", leader.getRankPrefix()+leader.getUsername()));
        }
        leader.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_NOW_LEADER, leader.getLanguage()));
    }

    @Override
    public void kickPlayer(UUID uuid) {
        ICorePlayer targetPlayer = instance.getCorePlayerManager().getCorePlayer(uuid);
        Party party = getPartyFromPlayer(uuid);

        party.removeMember(uuid);
        for(UUID current : party.getMembers()) {
            ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(current);
            if(corePlayer == null) continue;
            Language language = corePlayer.getLanguage();
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(
                    Message.PARTY_MEMBER_KICKED, language).replace("%REPLACE%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()));
        }
        if(party.getMembers().size() == 0)
            removeParty(party, PartyDeleteReason.NOMEMBERS);

        targetPlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(Message.PARTY_NOW_KICKED, targetPlayer.getLanguage()));
    }

    @Override
    public void acceptInvite(Party party, UUID uuid) {
        ICorePlayer targetPlayer = instance.getCorePlayerManager().getCorePlayer(uuid);

        List<UUID> members = new ArrayList<>(party.getMembers()); members.add(party.getLeaderUniqueId());
        for(UUID current : members) {
            ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(current);
            if(corePlayer == null) continue;
            Language language = corePlayer.getLanguage();
            corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.PARTY)+"§7"+languageHandler.getTranslatedMessage(
                    Message.PARTY_MEMBER_JOINED, language).replace("%REPLACE%", targetPlayer.getRankPrefix()+targetPlayer.getUsername()));
        }
        party.addMember(uuid);
    }

    @Override
    public int getStandardMemberAmount() {
        return this.standardMemberAmount;
    }

    @Override
    public int getPremiumMemberAmount() {
        return this.premiumMemberAmount;
    }

    public enum PartyDeleteReason {

        NOMEMBERS,
        DISBANDED;

        private PartyDeleteReason(){};

    }

}
