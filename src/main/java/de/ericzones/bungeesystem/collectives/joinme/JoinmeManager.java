// Created by Eric B. 15.02.2021 00:17
package de.ericzones.bungeesystem.collectives.joinme;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.event.EventListener;
import de.dytanic.cloudnet.driver.event.events.service.CloudServiceStopEvent;
import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.friend.FriendPlayer;
import de.ericzones.bungeesystem.collectives.friend.FriendProperty;
import de.ericzones.bungeesystem.collectives.object.TextBuilder;
import de.ericzones.bungeesystem.collectives.server.ICoreServer;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixHandler;
import de.ericzones.bungeesystem.global.messaging.pluginprefix.PluginPrefixType;
import net.kyori.adventure.text.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JoinmeManager {

    private final BungeeSystem instance;
    private final PluginPrefixHandler pluginPrefixHandler;
    private final LanguageHandler languageHandler;

    private final List<Joinme> activeJoinmes = new ArrayList<>();

    public JoinmeManager(BungeeSystem instance) {
        this.instance = instance;
        this.pluginPrefixHandler = instance.getPluginPrefixHandler();
        this.languageHandler = instance.getLanguageHandler();
        CloudNetDriver.getInstance().getEventManager().registerListeners(this);
    }

    public List<Joinme> getActiveJoinmes() {
        return this.activeJoinmes;
    }

    public Joinme initialJoinme(ICorePlayer corePlayer) {
        Joinme joinme = new Joinme(corePlayer.getUniqueID(), corePlayer.getConnectedCoreServer().getServerName());
        this.activeJoinmes.add(joinme);
        startExpiryCountdown(joinme);

        List<ICorePlayer> corePlayers = new ArrayList<>(instance.getCorePlayerManager().getCorePlayers());
        corePlayers.remove(corePlayer);

        for(ICorePlayer current : corePlayers) {
            FriendPlayer friendPlayer = instance.getFriendManager().getFriendPlayer(current.getUniqueID());
            if(friendPlayer == null) continue;
            if(friendPlayer.getPropertySetting(FriendProperty.JOINMES) == FriendProperty.Setting.DISABLED) continue;
            TextComponent message = new TextBuilder("§7"+languageHandler.getTranslatedMessage(Message.JOINME_MESSAGE, current.getLanguage())
            .replace("%PLAYER%", corePlayer.getRankPrefix()+corePlayer.getUsername()).replace("%REPLACE%", joinme.getServerName()))
                    .setHoverText("§b"+languageHandler.getTranslatedMessage(Message.JOINME_MESSAGE_HOVER, current.getLanguage()))
                    .setClickEvent(TextBuilder.Action.COMMAND, "joinme join "+joinme.getUniqueId())
                    .setPreText(pluginPrefixHandler.getPluginPrefix(PluginPrefixType.JOINME_BC)).build();
            current.sendMessage(message);
        }

        return joinme;
    }

    public Joinme getJoinmeById(UUID uniqueId) {
        return this.activeJoinmes.stream().filter(Joinme -> Joinme.getUniqueId().equals(uniqueId)).findFirst().orElse(null);
    }

    public Joinme getJoinmeByCreator(ICorePlayer corePlayer) {
        return this.activeJoinmes.stream().filter(Joinme -> Joinme.getCreator().equals(corePlayer.getUniqueID())).findFirst().orElse(null);
    }

    public Joinme getJoinmeByServer(ICoreServer coreServer) {
        return this.activeJoinmes.stream().filter(Joinme -> Joinme.getServerName().equals(coreServer.getServerName())).findFirst().orElse(null);
    }

    public boolean joinmeExists(UUID uniqueId) {
        return this.activeJoinmes.stream().anyMatch(Joinme -> Joinme.getUniqueId().equals(uniqueId));
    }

    public boolean joinmeExists(ICorePlayer corePlayer) {
        return this.activeJoinmes.stream().anyMatch(Joinme -> Joinme.getCreator().equals(corePlayer.getUniqueID()));
    }

    public boolean joinmeExists(ICoreServer coreServer) {
        return this.activeJoinmes.stream().anyMatch(Joinme -> Joinme.getServerName().equals(coreServer.getServerName()));
    }

    public void removeJoinme(Joinme joinme) {
        this.activeJoinmes.remove(joinme);
    }

    private void startExpiryCountdown(Joinme joinme) {
        instance.getProxyServer().getScheduler().buildTask(instance, new Runnable() {
            @Override
            public void run() {
                activeJoinmes.remove(joinme);
            }
        }).delay(1L, TimeUnit.MINUTES).schedule();
    }

    @EventListener
    public void onJoinmeServerStop(CloudServiceStopEvent e) {
        if(e.getServiceInfo().getConfiguration().getProcessConfig().getEnvironment().isMinecraftProxy() || e.getServiceInfo().getConfiguration().getProcessConfig().getEnvironment().isMinecraftJavaProxy())
            return;
        this.activeJoinmes.stream().filter(Joinme -> Joinme.getServerName().equals(e.getServiceInfo().getName())).findFirst().ifPresent(this::removeJoinme);
    }

}
