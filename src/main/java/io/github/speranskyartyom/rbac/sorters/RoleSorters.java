package io.github.speranskyartyom.rbac.sorters;

import io.github.speranskyartyom.rbac.models.Role;

import java.util.Comparator;

public final class RoleSorters {

    private RoleSorters() {
        throw new AssertionError("Utility class");
    }

    public static Comparator<Role> byName() {
        return Comparator.comparing(Role::getName);
    }

    public static Comparator<Role> byPermissionCount() {
        return Comparator.comparingInt(
                role -> role.getPermissions().size());
    }
}
