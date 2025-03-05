// Created by Eric B. 10.02.2021 14:29
package de.ericzones.bungeesystem.collectives.punish;

import de.ericzones.bungeesystem.collectives.coreplayer.ICorePlayer;
import de.ericzones.bungeesystem.collectives.coreplayer.IOfflineCorePlayer;
import de.ericzones.bungeesystem.global.language.Language;

public interface IPunish {

    ICorePlayer getCorePlayer();
    IOfflineCorePlayer getOfflineCorePlayer();

    ICorePlayer getCreatorCorePlayer();
    IOfflineCorePlayer getCreatorOfflineCorePlayer();

    Long getExpiry();
    String getExpiryTotalName(Language language);
    String getExpiryCurrentName(Language language);
    Long getCreationTime();

}
