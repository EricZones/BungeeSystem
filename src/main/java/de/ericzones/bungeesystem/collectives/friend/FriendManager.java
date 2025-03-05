// Created by Eric B. 18.02.2021 14:28
package de.ericzones.bungeesystem.collectives.friend;

import com.velocitypowered.api.event.EventManager;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.database.ISqlAdapter;
import de.ericzones.bungeesystem.collectives.friend.event.FriendConnectEvent;
import de.ericzones.bungeesystem.collectives.friend.event.FriendDisconnectEvent;
import de.ericzones.bungeesystem.collectives.object.TextBuilder;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixHandler;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;
import net.kyori.adventure.text.TextComponent;

import java.util.*;

public class FriendManager extends SqlFriendPlayer implements IFriendManager {

    private final BungeeSystem instance;
    private final LanguageHandler languageHandler;
    private final PluginPrefixHandler pluginPrefixHandler;

    private final int standardFriendsAmount = 36;
    private final int premiumFriendsAmount = 72;

    private final List<FriendPlayer> friendPlayers = new ArrayList<>();

    public FriendManager(BungeeSystem instance, ISqlAdapter sqlAdapter) {
        super(sqlAdapter);
        this.instance = instance;
        this.languageHandler = instance.getLanguageHandler();
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
        EventManager eventManager = instance.getProxyServer().getEventManager();
        eventManager.register(instance, new FriendConnectEvent(instance));
        eventManager.register(instance, new FriendDisconnectEvent(instance));
    }

    @Override
    public List<FriendPlayer> getFriendPlayers() {
        return this.friendPlayers;
    }

    @Override
    public FriendPlayer getFriendPlayer(UUID uuid) {
        return this.friendPlayers.stream().filter(FriendPlayer -> FriendPlayer.getUniqueId().equals(uuid)).findFirst().orElse(getOfflineFriendPlayer(uuid));
    }

    @Override
    public FriendPlayer initialFriendPlayer(UUID uuid) {
        if(friendPlayerExists(uuid)) return getFriendPlayer(uuid);

        FriendPlayer friendPlayer = new FriendPlayer(uuid, new HashMap<>(), new HashMap<>(), FriendProperty.getDefaultSettings());
        this.friendPlayers.add(friendPlayer);
        createFriendPlayer(uuid);
        return friendPlayer;
    }

    private void initialFriendPlayer(UUID uuid, Map<UUID, Long> friends, Map<UUID, Long> requests, Map<FriendProperty, FriendProperty.Setting> properties) {
        FriendPlayer friendPlayer = new FriendPlayer(uuid, friends, requests, properties);
        this.friendPlayers.add(friendPlayer);
    }

    @Override
    public void removeFriendPlayer(FriendPlayer friendPlayer) {
        updateFriendPlayer(friendPlayer.getUniqueId(), friendPlayer.getProperties());
    }

    @Override
    public void sendFriendRequest(FriendPlayer friendPlayer, FriendPlayer target) {
        target.getRequests().put(friendPlayer.getUniqueId(), System.currentTimeMillis());
        createFriendRequest(friendPlayer.getUniqueId(), target.getUniqueId());
        ICorePlayer corePlayer = target.getCorePlayer();
        if(corePlayer == null) return;

        Language language = corePlayer.getLanguage();
        String message = "§7"+languageHandler.getTranslatedMessage(Message.FRIEND_REQUESTSENT, language).replace("%REPLACE%",
                friendPlayer.getCorePlayer().getRankPrefix()+friendPlayer.getCorePlayer().getUsername());
        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+message);
        TextComponent acceptText = new TextBuilder("§a§l"+languageHandler.getTranslatedMessage(Message.FRIEND_REQUEST_ACCEPT, language))
                .setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§8● ").setPostText(" §8● ")
                .setHoverText("§a"+languageHandler.getTranslatedMessage(Message.FRIEND_REQUEST_ACCEPT_HOVER, language))
                .setClickEvent(TextBuilder.Action.COMMAND, "friend accept "+friendPlayer.getCorePlayer().getUsername()).build();
        TextComponent denyText = new TextBuilder("§c§l"+languageHandler.getTranslatedMessage(Message.FRIEND_REQUEST_DENY, language))
                .setPostText(" §8●").setHoverText("§c"+languageHandler.getTranslatedMessage(Message.FRIEND_REQUEST_DENY_HOVER, language))
                .setClickEvent(TextBuilder.Action.COMMAND, "friend deny "+friendPlayer.getCorePlayer().getUsername()).build();
        corePlayer.sendMessage(acceptText.append(denyText));
    }

    @Override
    public void acceptFriendRequest(FriendPlayer friendPlayer, FriendPlayer requester) {
        friendPlayer.getRequests().remove(requester.getUniqueId());
        deleteFriendRequest(requester.getUniqueId(), friendPlayer.getUniqueId());
        friendPlayer.getFriends().put(requester.getUniqueId(), System.currentTimeMillis());
        requester.getFriends().put(friendPlayer.getUniqueId(), System.currentTimeMillis());
        createFriendship(friendPlayer.getUniqueId(), requester.getUniqueId());
        ICorePlayer corePlayer = requester.getCorePlayer();
        if(corePlayer == null) return;

        Language language = corePlayer.getLanguage();
        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"
                +languageHandler.getTranslatedMessage(Message.FRIEND_REQUESTACCEPTED, language).replace("%REPLACE%",
                friendPlayer.getCorePlayer().getRankPrefix()+friendPlayer.getCorePlayer().getUsername()));
    }

    @Override
    public void acceptAllFriendRequests(FriendPlayer friendPlayer) {
        for(UUID requester : friendPlayer.getRequests().keySet())
            acceptFriendRequest(friendPlayer, getFriendPlayer(requester));
    }

    @Override
    public void denyFriendRequest(FriendPlayer friendPlayer, FriendPlayer requester) {
        friendPlayer.getRequests().remove(requester.getUniqueId());
        deleteFriendRequest(requester.getUniqueId(), friendPlayer.getUniqueId());
    }

    @Override
    public void denyAllFriendRequests(FriendPlayer friendPlayer) {
        for(UUID requester : friendPlayer.getRequests().keySet())
            denyFriendRequest(friendPlayer, getFriendPlayer(requester));
    }

    @Override
    public void removeFriend(FriendPlayer friendPlayer, FriendPlayer target) {
        friendPlayer.getFriends().remove(target.getUniqueId());
        target.getFriends().remove(friendPlayer.getUniqueId());
        deleteFriendship(friendPlayer.getUniqueId(), target.getUniqueId());
        ICorePlayer corePlayer = target.getCorePlayer();
        if(corePlayer == null) return;

        Language language = corePlayer.getLanguage();
        corePlayer.sendMessage(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.FRIENDS)+"§7"
                +languageHandler.getTranslatedMessage(Message.FRIEND_REMOVEDFRIEND, language).replace("%REPLACE%",
                friendPlayer.getCorePlayer().getRankPrefix()+friendPlayer.getCorePlayer().getUsername()));
    }

    @Override
    public void removeAllFriends(FriendPlayer friendPlayer) {
        for(UUID friend : friendPlayer.getFriends().keySet())
            removeFriend(friendPlayer, getFriendPlayer(friend));
    }

    @Override
    public int getStandardFriendsAmount() {
        return this.standardFriendsAmount;
    }

    @Override
    public int getPremiumFriendsAmount() {
        return this.premiumFriendsAmount;
    }

    private boolean friendPlayerExists(UUID uuid) {
        return getFriendPlayer(uuid) != null;
    }

    private FriendPlayer getOfflineFriendPlayer(UUID uuid) {
        Map<FriendProperty, FriendProperty.Setting> properties = getFriendPlayerSettings(uuid);
        Map<UUID, Long> friends = getFriendPlayerFriends(uuid);
        Map<UUID, Long> requests = getFriendPlayerRequests(uuid);
        return new FriendPlayer(uuid, friends, requests, properties);
    }

    private void registerFriendPlayers() {
        Map<UUID, Map<FriendProperty, FriendProperty.Setting>> friendPlayers = getAllFriendPlayers();
        for(UUID current : friendPlayers.keySet()) {
            Map<FriendProperty, FriendProperty.Setting> properties = friendPlayers.get(current);
            Map<UUID, Long> friends = getFriendPlayerFriends(current);
            Map<UUID, Long> requests = getFriendPlayerRequests(current);
            initialFriendPlayer(current, friends, requests, properties);
        }
    }

}
