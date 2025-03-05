// Created by Eric B. 07.02.2021 18:11
package de.ericzones.bungeesystem.global.permission;

import java.util.Objects;

public class PermissionHandler {

    private String[] permissions = new String[PermissionType.values().length];

    public PermissionHandler() {
        registerPermissions();
    }

    public String getPermission(PermissionType permissionType) {
        return permissions[permissionType.getId()];
    }

    private void registerPermissions() {
        for(int i = 0; i < permissions.length; i++)
            permissions[i] = Objects.requireNonNull(PermissionType.getPermissionTypeFromId(i)).getStandardPermission();
    }

}
