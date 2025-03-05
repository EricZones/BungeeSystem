// Created by Eric B. 10.02.2021 14:23
package de.ericzones.bungeesystem.collectives.punish.ban;

import de.ericzones.bungeesystem.BungeeSystem;
import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.collectives.punish.IPunish;
import de.ericzones.bungeesystem.global.language.Language;
import de.ericzones.bungeesystem.global.language.LanguageHandler;
import de.ericzones.bungeesystem.global.language.Message;

import java.util.UUID;

public class Ban implements IPunish {

    private final UUID uuid, creator;
    private final String ipAddress;
    private final BanReason reason;
    private final Long expiry, creationTime;

    public Ban(UUID uuid, String ipAddress, BanReason reason, int previousBansCount, UUID creator) {
        this.uuid = uuid;
        this.ipAddress = ipAddress;
        this.creator = creator;
        this.reason = reason;
        this.creationTime = System.currentTimeMillis();
        if(reason.getDuration() != -1)
            this.expiry = System.currentTimeMillis() + (reason.getDuration()*previousBansCount)*1000;
        else
            this.expiry = -1L;
    }

    public Ban(UUID uuid, String ipAddress, BanReason reason, Long expiry, UUID creator, Long creationTime) {
        this.uuid = uuid;
        this.creator = creator;
        this.ipAddress = ipAddress;
        this.reason = reason;
        this.expiry = expiry;
        this.creationTime = creationTime;
    }

    @Override
    public ICorePlayer getCorePlayer() {
        return BungeeSystem.getInstance().getCorePlayerManager().getCorePlayer(uuid);
    }

    @Override
    public IOfflineCorePlayer getOfflineCorePlayer() {
        return BungeeSystem.getInstance().getCorePlayerManager().getOfflineCorePlayer(uuid);
    }

    @Override
    public ICorePlayer getCreatorCorePlayer() {
        return BungeeSystem.getInstance().getCorePlayerManager().getCorePlayer(creator);
    }

    @Override
    public IOfflineCorePlayer getCreatorOfflineCorePlayer() {
        return BungeeSystem.getInstance().getCorePlayerManager().getOfflineCorePlayer(creator);
    }

    @Override
    public Long getExpiry() {
        return expiry;
    }

    @Override
    public String getExpiryCurrentName(Language language) {
        LanguageHandler languageHandler = BungeeSystem.getInstance().getLanguageHandler();
        if(this.expiry == -1)
            return languageHandler.getTranslatedMessage(Message.PUNISH_DURATION_INFINITE, language);
        long millis = this.expiry - System.currentTimeMillis();
        return getExpiryName(millis);
    }

    @Override
    public String getExpiryTotalName(Language language) {
        LanguageHandler languageHandler = BungeeSystem.getInstance().getLanguageHandler();
        if(this.expiry == -1)
            return languageHandler.getTranslatedMessage(Message.PUNISH_DURATION_INFINITE, language);
        long millis = this.expiry - this.creationTime;
        return getExpiryName(millis);
    }

    @Override
    public Long getCreationTime() {
        return creationTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public BanReason getReason() {
        return reason;
    }

    private String getExpiryName(long expiryMillis) {
        long seconds = 0, minutes = 0, hours = 0, days = 0;
        while(expiryMillis > 1000) {
            expiryMillis-=1000;
            seconds++;
        }
        while(seconds > 60) {
            seconds-=60;
            minutes++;
        }
        while(minutes > 60) {
            minutes-=60;
            hours++;
        }
        while(hours > 24) {
            hours-=24;
            days++;
        }
        return days + "d " + hours + "h " + minutes + "m";
    }

}
