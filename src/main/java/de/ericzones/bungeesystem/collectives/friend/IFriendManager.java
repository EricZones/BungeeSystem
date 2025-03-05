// Created by Eric B. 18.02.2021 12:41
package de.ericzones.bungeesystem.collectives.friend;

import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;

import java.util.List;
import java.util.UUID;

public interface IFriendManager {

    List<FriendPlayer> getFriendPlayers();

    FriendPlayer getFriendPlayer(UUID uuid);

    FriendPlayer initialFriendPlayer(UUID uuid);
    void removeFriendPlayer(FriendPlayer friendPlayer);

    void sendFriendRequest(FriendPlayer friendPlayer, FriendPlayer target);

    void acceptFriendRequest(FriendPlayer friendPlayer, FriendPlayer requester);
    void acceptAllFriendRequests(FriendPlayer friendPlayer);

    void denyFriendRequest(FriendPlayer friendPlayer, FriendPlayer requester);
    void denyAllFriendRequests(FriendPlayer friendPlayer);

    void removeFriend(FriendPlayer friendPlayer, FriendPlayer target);
    void removeAllFriends(FriendPlayer friendPlayer);

    int getStandardFriendsAmount();
    int getPremiumFriendsAmount();

}