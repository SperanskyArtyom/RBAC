package types.filters;

import org.junit.jupiter.api.Test;
import types.classes.Role;
import types.interfaces.functional.RoleFilter;
import types.records.Permission;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleFiltersTest {

    private final Role role;

    RoleFiltersTest() {
        role = new Role("ADMIN", "Administrator role");
        role.addPermission(new Permission("READ", "users", "read users"));
        role.addPermission(new Permission("WRITE", "users", "write users"));
    }

    @Test
    void byName_exactMatch_true() {
        RoleFilter filter = RoleFilters.byName("ADMIN");

        assertTrue(filter.test(role));
    }

    @Test
    void byName_exactMatch_false() {
        RoleFilter filter = RoleFilters.byName("MANAGER");

        assertFalse(filter.test(role));
    }

    @Test
    void byNameContains_ignoreCase_true() {
        RoleFilter filter = RoleFilters.byNameContains("adm");

        assertTrue(filter.test(role));
    }

    @Test
    void byNameContains_ignoreCase_false() {
        RoleFilter filter = RoleFilters.byNameContains("user");

        assertFalse(filter.test(role));
    }

    @Test
    void hasPermission_object_true() {
        Permission permission =
                new Permission("READ", "users", "read users");

        RoleFilter filter = RoleFilters.hasPermission(permission);

        assertTrue(filter.test(role));
    }

    @Test
    void hasPermission_object_false() {
        Permission permission =
                new Permission("DELETE", "users", "delete users");

        RoleFilter filter = RoleFilters.hasPermission(permission);

        assertFalse(filter.test(role));
    }

    @Test
    void hasPermission_byNameAndResource_true() {
        RoleFilter filter =
                RoleFilters.hasPermission("READ", "users");

        assertTrue(filter.test(role));
    }

    @Test
    void hasPermission_byNameAndResource_false() {
        RoleFilter filter =
                RoleFilters.hasPermission("DELETE", "users");

        assertFalse(filter.test(role));
    }

    @Test
    void hasAtLeastNPermissions_true() {
        RoleFilter filter = RoleFilters.hasAtLeastNPermissions(2);

        assertTrue(filter.test(role));
    }

    @Test
    void hasAtLeastNPermissions_false() {
        RoleFilter filter = RoleFilters.hasAtLeastNPermissions(3);

        assertFalse(filter.test(role));
    }

    @Test
    void and_shouldReturnTrue_whenBothMatch() {
        RoleFilter filter =
                RoleFilters.byName("ADMIN")
                        .and(RoleFilters.hasAtLeastNPermissions(2));

        assertTrue(filter.test(role));
    }

    @Test
    void and_shouldReturnFalse_whenOneDoesNotMatch() {
        RoleFilter filter =
                RoleFilters.byName("ADMIN")
                        .and(RoleFilters.hasAtLeastNPermissions(3));

        assertFalse(filter.test(role));
    }

    @Test
    void or_shouldReturnTrue_whenOneMatches() {
        RoleFilter filter =
                RoleFilters.byName("MANAGER")
                        .or(RoleFilters.hasPermission("READ", "users"));

        assertTrue(filter.test(role));
    }

    @Test
    void or_shouldReturnFalse_whenNoneMatch() {
        RoleFilter filter =
                RoleFilters.byName("MANAGER")
                        .or(RoleFilters.hasPermission("DELETE", "users"));

        assertFalse(filter.test(role));
    }
}