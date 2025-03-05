// Created by Eric B. 10.02.2021 20:16
package de.ericzones.bungeesystem.collectives.punish;

import com.velocitypowered.api.event.EventManager;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.collectives.database.ISqlAdapter;
import de.ericzones.bungeesystem.collectives.object.Pair;
import de.ericzones.bungeesystem.collectives.punish.ban.Ban;
import de.ericzones.bungeesystem.collectives.punish.ban.BanHistory;
import de.ericzones.bungeesystem.collectives.punish.ban.BanReason;
import de.ericzones.bungeesystem.collectives.punish.event.PunishedChatEvent;
import de.ericzones.bungeesystem.collectives.punish.event.PunishedCommandEvent;
import de.ericzones.bungeesystem.collectives.punish.event.PunishedConnectEvent;
import de.ericzones.bungeesystem.collectives.punish.mute.Mute;
import de.ericzones.bungeesystem.collectives.punish.mute.MuteHistory;
import de.ericzones.bungeesystem.collectives.punish.mute.MuteReason;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.chatmessage.ChatMessageHandler;
import de.ericzones.bungeesystem.global.messaging.chatmessage.ChatMessageType;
import de.ericzones.bungeesystem.global.messaging.disconnectmessage.DisconnectMessageType;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixHandler;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;

import java.util.*;
import java.util.stream.Collectors;

public class PunishManager extends SqlPunish implements IPunishManager {

    private final BungeeSystem instance;

    private final List<Ban> globalBans = new ArrayList<>();
    private final List<BanHistory> globalBanHistories = new ArrayList<>();
    private final List<Mute> globalMutes = new ArrayList<>();
    private final List<MuteHistory> globalMuteHistories = new ArrayList<>();

    public PunishManager(BungeeSystem instance, ISqlAdapter sqlAdapter) {
        super(sqlAdapter); this.instance = instance;
        EventManager eventManager = instance.getProxyServer().getEventManager();
        eventManager.register(instance, new PunishedConnectEvent(instance));
        eventManager.register(instance, new PunishedChatEvent(instance));
        eventManager.register(instance, new PunishedCommandEvent(instance));
        registerAllBans(); registerAllMutes();
    }

    @Override
    public List<Mute> getGlobalMutes() {
        List<Mute> mutes = this.globalMutes;
        mutes.removeIf(this::isMuteExpired);
        return mutes;
    }

    @Override
    public List<MuteHistory> getGlobalMuteHistories() {
        return this.globalMuteHistories;
    }

    @Override
    public List<Mute> getMutesByCreator(UUID uuid) {
        List<Mute> mutes = this.globalMutes.stream().filter(Mute -> Mute.getCreatorOfflineCorePlayer().getUniqueID().equals(uuid)).collect(Collectors.toList());
        mutes.removeIf(this::isMuteExpired);
        return mutes;
    }

    @Override
    public List<Mute> getMutesByReason(MuteReason reason) {
        List<Mute> mutes = this.globalMutes.stream().filter(Mute -> Mute.getReason().equals(reason)).collect(Collectors.toList());
        mutes.removeIf(this::isMuteExpired);
        return mutes;
    }

    @Override
    public Mute initialMute(ICorePlayer corePlayer, MuteReason reason, UUID creator) {
        int previousMutesCount = getPreviousMutesCount(corePlayer.getUniqueID(), reason);
        Mute mute = new Mute(corePlayer.getUniqueID(), reason, previousMutesCount, creator);
        this.globalMutes.add(mute);

        addToMuteHistory(corePlayer.getUniqueID(), mute);
        createMute(mute.getOfflineCorePlayer().getUniqueID(), mute.getReason(), mute.getExpiry(), mute.getCreatorOfflineCorePlayer().getUniqueID(),
                mute.getCreationTime());

        ChatMessageHandler messageHandler = instance.getChatMessageHandler();
        corePlayer.sendMessage(" ");
        corePlayer.sendMessage(messageHandler.getChatMessage(ChatMessageType.MUTEDINCHAT, corePlayer.getLanguage()));
        corePlayer.sendMessage(getMutedChatMessage(mute, corePlayer));
        return mute;
    }

    @Override
    public Mute initialMute(IOfflineCorePlayer offlineCorePlayer, MuteReason reason, UUID creator) {
        int previousMutesCount = getPreviousMutesCount(offlineCorePlayer.getUniqueID(), reason);
        Mute mute = new Mute(offlineCorePlayer.getUniqueID(), reason, previousMutesCount, creator);
        this.globalMutes.add(mute);

        addToMuteHistory(offlineCorePlayer.getUniqueID(), mute);
        createMute(mute.getOfflineCorePlayer().getUniqueID(), mute.getReason(), mute.getExpiry(), mute.getCreatorOfflineCorePlayer().getUniqueID(),
                mute.getCreationTime());

        if(offlineCorePlayer.isOnline()) {
            ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(offlineCorePlayer.getUniqueID());
            ChatMessageHandler messageHandler = instance.getChatMessageHandler();
            corePlayer.sendMessage(" ");
            corePlayer.sendMessage(messageHandler.getChatMessage(ChatMessageType.MUTEDINCHAT, corePlayer.getLanguage()));
            corePlayer.sendMessage(getMutedChatMessage(mute, corePlayer));
        }
        return mute;
    }

    private void initialMute(UUID uuid, MuteReason reason, Long expiry, UUID creator, Long creationTime) {
        Mute mute = new Mute(uuid, reason, expiry, creator, creationTime);
        this.globalMutes.add(mute);
    }

    @Override
    public void removeMute(Mute mute) {
        this.globalMutes.remove(mute);
        deleteMute(mute.getOfflineCorePlayer().getUniqueID());
    }

    @Override
    public void resetMuteHistory(MuteHistory muteHistory) {
        this.globalMuteHistories.remove(muteHistory);
        deleteMuteHistory(muteHistory.getOfflineCorePlayer().getUniqueID());
    }

    @Override
    public boolean isMuted(UUID uuid) {
        boolean muted = this.globalMutes.stream().anyMatch(Mute -> Mute.getOfflineCorePlayer().getUniqueID().equals(uuid));
        if(muted)
            muted = !isMuteExpired(uuid);
        return muted;
    }

    @Override
    public boolean isMuted(ICorePlayer corePlayer) {
        boolean muted = this.globalMutes.stream().anyMatch(Mute -> Mute.getCorePlayer().equals(corePlayer));
        if(muted)
            muted = !isMuteExpired(corePlayer.getUniqueID());
        return muted;
    }

    @Override
    public boolean isMuted(IOfflineCorePlayer offlineCorePlayer) {
        boolean muted = this.globalMutes.stream().anyMatch(Mute -> Mute.getOfflineCorePlayer().equals(offlineCorePlayer));
        if(muted)
            muted = !isMuteExpired(offlineCorePlayer.getUniqueID());
        return muted;
    }

    @Override
    public boolean hasMuteHistory(UUID uuid) {
        return this.globalMuteHistories.stream().anyMatch(MuteHistory -> MuteHistory.getOfflineCorePlayer().getUniqueID().equals(uuid));
    }

    @Override
    public boolean hasMuteHistory(ICorePlayer corePlayer) {
        return this.globalMuteHistories.stream().anyMatch(MuteHistory -> MuteHistory.getCorePlayer().equals(corePlayer));
    }

    @Override
    public boolean hasMuteHistory(IOfflineCorePlayer offlineCorePlayer) {
        return this.globalMuteHistories.stream().anyMatch(MuteHistory -> MuteHistory.getOfflineCorePlayer().equals(offlineCorePlayer));
    }

    @Override
    public Mute getMute(UUID uuid) {
        Mute mute = this.globalMutes.stream().filter(Mute -> Mute.getOfflineCorePlayer().getUniqueID().equals(uuid)).findFirst().orElse(null);
        if(mute == null || isMuteExpired(mute))
            return null;
        return mute;
    }

    @Override
    public Mute getMute(ICorePlayer corePlayer) {
        Mute mute = this.globalMutes.stream().filter(Mute -> Mute.getCorePlayer().equals(corePlayer)).findFirst().orElse(null);
        if(mute == null || isMuteExpired(mute))
            return null;
        return mute;
    }

    @Override
    public Mute getMute(IOfflineCorePlayer offlineCorePlayer) {
        Mute mute = this.globalMutes.stream().filter(Mute -> Mute.getOfflineCorePlayer().equals(offlineCorePlayer)).findFirst().orElse(null);
        if(mute == null || isMuteExpired(mute))
            return null;
        return mute;
    }

    @Override
    public MuteHistory getMuteHistory(UUID uuid) {
        return this.globalMuteHistories.stream().filter(MuteHistory -> MuteHistory.getOfflineCorePlayer().getUniqueID().equals(uuid)).findFirst().orElse(null);
    }

    @Override
    public MuteHistory getMuteHistory(ICorePlayer corePlayer) {
        return this.globalMuteHistories.stream().filter(MuteHistory -> MuteHistory.getCorePlayer().equals(corePlayer)).findFirst().orElse(null);
    }

    @Override
    public MuteHistory getMuteHistory(IOfflineCorePlayer offlineCorePlayer) {
        return this.globalMuteHistories.stream().filter(MuteHistory -> MuteHistory.getOfflineCorePlayer().equals(offlineCorePlayer)).findFirst().orElse(null);
    }

    private void registerAllMutes() {
        Map<UUID, Map<PunishProperty, String>> mutes = getAllMutes();
        for(UUID current : mutes.keySet()) {
            Map<PunishProperty, String> currentMap = mutes.get(current);
            MuteReason reason = MuteReason.valueOf(currentMap.get(PunishProperty.REASON));
            Long expiry = Long.parseLong(currentMap.get(PunishProperty.EXPIRY));
            UUID creator = UUID.fromString(currentMap.get(PunishProperty.CREATOR));
            Long creationTime = Long.parseLong(currentMap.get(PunishProperty.CREATIONTIME));
            initialMute(current, reason, expiry, creator, creationTime);
        }

        List<Pair<UUID, Map<PunishProperty, String>>> muteHistories = getAllMuteHistories();
        Map<UUID, List<Mute>> historicMutes = new HashMap<>();
        for(Pair<UUID, Map<PunishProperty, String>> current : muteHistories) {
            UUID uuid = current.getFirstObject();
            Map<PunishProperty, String> currentMap = current.getSecondObject();
            MuteReason reason = MuteReason.valueOf(currentMap.get(PunishProperty.REASON));
            UUID creator = UUID.fromString(currentMap.get(PunishProperty.CREATOR));
            Long creationTime = Long.parseLong(currentMap.get(PunishProperty.CREATIONTIME));
            Mute mute = new Mute(uuid, reason, null, creator, creationTime);
            if(historicMutes.containsKey(uuid))
                historicMutes.get(uuid).add(mute);
            else {
                historicMutes.put(uuid, new ArrayList<>());
                historicMutes.get(uuid).add(mute);
            }
        }
        for(UUID current : historicMutes.keySet()) {
            if(!hasMuteHistory(current))
                this.globalMuteHistories.add(new MuteHistory(current, historicMutes.get(current)));
            else
                getMuteHistory(current).addMutes(historicMutes.get(current));
        }
    }

    private boolean isMuteExpired(Mute mute) {
        Long expiry = mute.getExpiry();
        if(expiry < System.currentTimeMillis()) {
            removeMute(mute);
            return true;
        }
        return false;
    }

    private boolean isMuteExpired(UUID uuid) {
        Mute mute = this.globalMutes.stream().filter(Mute -> Mute.getOfflineCorePlayer().getUniqueID().equals(uuid)).findFirst().orElse(null);
        if(mute == null) return false;
        Long expiry = mute.getExpiry();
        if(expiry < System.currentTimeMillis()) {
            removeMute(mute);
            return true;
        }
        return false;
    }

    private void addToMuteHistory(UUID uuid, Mute mute) {
        if(hasMuteHistory(uuid))
            getMuteHistory(uuid).addMute(mute);
        else {
            List<Mute> previousMutes = new ArrayList<>(); previousMutes.add(mute);
            this.globalMuteHistories.add(new MuteHistory(uuid, previousMutes));
        }
    }

    private int getPreviousMutesCount(UUID uuid, MuteReason reason) {
        int previousMutesCount = 1;
        if(hasMuteHistory(uuid))
            previousMutesCount += getMuteHistory(uuid).getMutesByReason(reason).size();
        return previousMutesCount;
    }

    private String getMutedChatMessage(Mute mute, ICorePlayer corePlayer) {
        PluginPrefixHandler prefixHandler = instance.getPluginPrefixHandler();
        LanguageHandler languageHandler = instance.getLanguageHandler();
        Language language = corePlayer.getLanguage();
        return prefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.MUTED_REASON,
                language)+" §8• §c"+mute.getReason().getName(language)+" §7"+languageHandler.getTranslatedMessage(Message.MUTED_DURATION,
                language)+" §8• §c"+mute.getExpiryTotalName(language);
    }

    @Override
    public String getIsMutedChatMessage(Mute mute, ICorePlayer corePlayer) {
        PluginPrefixHandler prefixHandler = instance.getPluginPrefixHandler();
        LanguageHandler languageHandler = instance.getLanguageHandler();
        Language language = corePlayer.getLanguage();
        return prefixHandler.getPluginPrefix(PluginPrefixType.BANSYSTEM)+"§7"+languageHandler.getTranslatedMessage(Message.MUTED_REASON,
                language)+" §8• §c"+mute.getReason().getName(language)+" §7"+languageHandler.getTranslatedMessage(Message.MUTED_REMAININGTIME,
                language)+" §8• §c"+mute.getExpiryCurrentName(language);
    }


    // Ban & BanHistory

    @Override
    public List<Ban> getGlobalBans() {
        List<Ban> bans = this.globalBans;
        bans.removeIf(this::isBanExpired);
        return bans;
    }

    @Override
    public List<BanHistory> getGlobalBanHistories() {
        return this.globalBanHistories;
    }

    @Override
    public List<Ban> getBansByCreator(UUID uuid) {
        List<Ban> bans = this.globalBans.stream().filter(Ban -> Ban.getCreatorOfflineCorePlayer().getUniqueID().equals(uuid)).collect(Collectors.toList());
        bans.removeIf(this::isBanExpired);
        return bans;
    }

    @Override
    public List<Ban> getBansByReason(BanReason reason) {
        List<Ban> bans = this.globalBans.stream().filter(Ban -> Ban.getReason().equals(reason)).collect(Collectors.toList());
        bans.removeIf(this::isBanExpired);
        return bans;
    }

    @Override
    public Ban initialBan(ICorePlayer corePlayer, BanReason reason, UUID creator) {
        int previousBansCount = getPreviousBansCount(corePlayer.getUniqueID(), reason);
        Ban ban = new Ban(corePlayer.getUniqueID(), corePlayer.getConnectionInfo().getIpAddress(), reason, previousBansCount, creator);
        this.globalBans.add(ban);

        addToBanHistory(corePlayer.getUniqueID(), ban);
        createBan(ban.getOfflineCorePlayer().getUniqueID(), ban.getIpAddress(), ban.getReason(), ban.getExpiry(), ban.getCreatorOfflineCorePlayer().getUniqueID(),
                ban.getCreationTime());

        Language language = corePlayer.getLanguage();
        corePlayer.disconnect(instance.getDisconnectMessageHandler().getDisconnectMessage(DisconnectMessageType.BANNED, ban.getReason().getName(language),
                ban.getExpiryTotalName(language), language));
        return ban;
    }

    @Override
    public Ban initialBan(IOfflineCorePlayer offlineCorePlayer, BanReason reason, UUID creator) {
        int previousBansCount = getPreviousBansCount(offlineCorePlayer.getUniqueID(), reason);
        Ban ban = new Ban(offlineCorePlayer.getUniqueID(), offlineCorePlayer.getIpAddress(), reason, previousBansCount, creator);
        this.globalBans.add(ban);

        addToBanHistory(offlineCorePlayer.getUniqueID(), ban);
        createBan(ban.getOfflineCorePlayer().getUniqueID(), ban.getIpAddress(), ban.getReason(), ban.getExpiry(), ban.getCreatorOfflineCorePlayer().getUniqueID(),
                ban.getCreationTime());

        if(offlineCorePlayer.isOnline()) {
            ICorePlayer corePlayer = instance.getCorePlayerManager().getCorePlayer(offlineCorePlayer.getUniqueID());
            Language language = corePlayer.getLanguage();
            corePlayer.disconnect(instance.getDisconnectMessageHandler().getDisconnectMessage(DisconnectMessageType.BANNED,
                    ban.getReason().getName(language), ban.getExpiryTotalName(language), language));
        }
        return ban;
    }

    private void initialBan(UUID uuid, String ipAddress, BanReason reason, Long expiry, UUID creator, Long creationTime) {
        Ban ban = new Ban(uuid, ipAddress, reason, expiry, creator, creationTime);
        this.globalBans.add(ban);
    }

    @Override
    public void removeBan(Ban ban) {
        this.globalBans.remove(ban);
        deleteBan(ban.getOfflineCorePlayer().getUniqueID());
    }

    @Override
    public void resetBanHistory(BanHistory banHistory) {
        this.globalBanHistories.remove(banHistory);
        deleteBanHistory(banHistory.getOfflineCorePlayer().getUniqueID());
    }

    @Override
    public boolean isBanned(UUID uuid) {
        boolean banned = this.globalBans.stream().anyMatch(Ban -> Ban.getOfflineCorePlayer().getUniqueID().equals(uuid));
        if(banned)
            banned = !isBanExpired(uuid);
        return banned;
    }

    @Override
    public boolean isBanned(String ipAddress) {
        boolean banned = this.globalBans.stream().anyMatch(Ban -> Ban.getIpAddress().equals(ipAddress));
        if(banned)
            banned = !isBanExpired(ipAddress);
        return banned;
    }

    @Override
    public boolean isBanned(ICorePlayer corePlayer) {
        boolean banned = this.globalBans.stream().anyMatch(Ban -> Ban.getCorePlayer().equals(corePlayer));
        if(banned)
            banned = !isBanExpired(corePlayer.getUniqueID());
        return banned;
    }

    @Override
    public boolean isBanned(IOfflineCorePlayer offlineCorePlayer) {
        boolean banned = this.globalBans.stream().anyMatch(Ban -> Ban.getOfflineCorePlayer().equals(offlineCorePlayer));
        if(banned)
            banned = !isBanExpired(offlineCorePlayer.getUniqueID());
        return banned;
    }

    @Override
    public boolean hasBanHistory(UUID uuid) {
        return this.globalBanHistories.stream().anyMatch(BanHistory -> BanHistory.getOfflineCorePlayer().getUniqueID().equals(uuid));
    }

    @Override
    public boolean hasBanHistory(ICorePlayer corePlayer) {
        return this.globalBanHistories.stream().anyMatch(BanHistory -> BanHistory.getCorePlayer().equals(corePlayer));
    }

    @Override
    public boolean hasBanHistory(IOfflineCorePlayer offlineCorePlayer) {
        return this.globalBanHistories.stream().anyMatch(BanHistory -> BanHistory.getOfflineCorePlayer().equals(offlineCorePlayer));
    }

    @Override
    public Ban getBan(UUID uuid) {
        Ban ban = this.globalBans.stream().filter(Ban -> Ban.getOfflineCorePlayer().getUniqueID().equals(uuid)).findFirst().orElse(null);
        if(ban == null || isBanExpired(ban))
            return null;
        return ban;
    }

    @Override
    public Ban getBan(ICorePlayer corePlayer) {
        Ban ban = this.globalBans.stream().filter(Ban -> Ban.getCorePlayer().equals(corePlayer)).findFirst().orElse(null);
        if(ban == null || isBanExpired(ban))
            return null;
        return ban;
    }

    @Override
    public Ban getBan(IOfflineCorePlayer offlineCorePlayer) {
        Ban ban = this.globalBans.stream().filter(Ban -> Ban.getOfflineCorePlayer().equals(offlineCorePlayer)).findFirst().orElse(null);
        if(ban == null || isBanExpired(ban))
            return null;
        return ban;
    }

    @Override
    public Ban getBan(String ipAddress) {
        Ban ban = this.globalBans.stream().filter(Ban -> Ban.getIpAddress().equals(ipAddress)).findFirst().orElse(null);
        if(ban == null || isBanExpired(ban))
            return null;
        return ban;
    }

    @Override
    public BanHistory getBanHistory(UUID uuid) {
        return this.globalBanHistories.stream().filter(BanHistory -> BanHistory.getOfflineCorePlayer().getUniqueID().equals(uuid)).findFirst().orElse(null);
    }

    @Override
    public BanHistory getBanHistory(ICorePlayer corePlayer) {
        return this.globalBanHistories.stream().filter(BanHistory -> BanHistory.getCorePlayer().equals(corePlayer)).findFirst().orElse(null);
    }

    @Override
    public BanHistory getBanHistory(IOfflineCorePlayer offlineCorePlayer) {
        return this.globalBanHistories.stream().filter(BanHistory -> BanHistory.getOfflineCorePlayer().equals(offlineCorePlayer)).findFirst().orElse(null);
    }

    private void registerAllBans() {
        Map<UUID, Map<PunishProperty, String>> bans = getAllBans();
        for(UUID current : bans.keySet()) {
            Map<PunishProperty, String> currentMap = bans.get(current);
            String ipAddress = currentMap.get(PunishProperty.IP);
            BanReason reason = BanReason.valueOf(currentMap.get(PunishProperty.REASON));
            Long expiry = Long.parseLong(currentMap.get(PunishProperty.EXPIRY));
            UUID creator = UUID.fromString(currentMap.get(PunishProperty.CREATOR));
            Long creationTime = Long.parseLong(currentMap.get(PunishProperty.CREATIONTIME));
            initialBan(current, ipAddress, reason, expiry, creator, creationTime);
        }

        List<Pair<UUID, Map<PunishProperty, String>>> banHistories = getAllBanHistories();
        Map<UUID, List<Ban>> historicBans = new HashMap<>();
        for(Pair<UUID, Map<PunishProperty, String>> current : banHistories) {
            UUID uuid = current.getFirstObject();
            Map<PunishProperty, String> currentMap = current.getSecondObject();
            BanReason reason = BanReason.valueOf(currentMap.get(PunishProperty.REASON));
            UUID creator = UUID.fromString(currentMap.get(PunishProperty.CREATOR));
            Long creationTime = Long.parseLong(currentMap.get(PunishProperty.CREATIONTIME));
            Ban ban = new Ban(uuid, null, reason, null, creator, creationTime);
            if(historicBans.containsKey(uuid))
                historicBans.get(uuid).add(ban);
            else {
                historicBans.put(uuid, new ArrayList<>());
                historicBans.get(uuid).add(ban);
            }
        }
        for(UUID current : historicBans.keySet()) {
            if(!hasBanHistory(current))
                this.globalBanHistories.add(new BanHistory(current, historicBans.get(current)));
            else
                getBanHistory(current).addBans(historicBans.get(current));
        }
    }

    private boolean isBanExpired(Ban ban) {
        Long expiry = ban.getExpiry();
        if(expiry < System.currentTimeMillis()) {
            removeBan(ban);
            return true;
        }
        return false;
    }

    private boolean isBanExpired(UUID uuid) {
        Ban ban = this.globalBans.stream().filter(Ban -> Ban.getOfflineCorePlayer().getUniqueID().equals(uuid)).findFirst().orElse(null);
        if(ban == null) return false;
        Long expiry = ban.getExpiry();
        if(expiry < System.currentTimeMillis()) {
            removeBan(ban);
            return true;
        }
        return false;
    }

    private boolean isBanExpired(String ipAddress) {
        Ban ban = this.globalBans.stream().filter(Ban -> Ban.getIpAddress().equals(ipAddress)).findFirst().orElse(null);
        if(ban == null) return false;
        Long expiry = ban.getExpiry();
        if(expiry < System.currentTimeMillis()) {
            removeBan(ban);
            return true;
        }
        return false;
    }

    private void addToBanHistory(UUID uuid, Ban ban) {
        if(hasBanHistory(uuid))
            getBanHistory(uuid).addBan(ban);
        else {
            List<Ban> previousBans = new ArrayList<>(); previousBans.add(ban);
            this.globalBanHistories.add(new BanHistory(uuid, previousBans));
        }
    }

    private int getPreviousBansCount(UUID uuid, BanReason reason) {
        int previousBansCount = 1;
        if(hasBanHistory(uuid))
            previousBansCount += getBanHistory(uuid).getBansByReason(reason).size();
        return previousBansCount;
    }

}
