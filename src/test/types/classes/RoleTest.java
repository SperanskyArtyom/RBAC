package types.classes;

import org.junit.jupiter.api.Test;
import types.records.Permission;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void constructor_validArguments_shouldCreateRole() {
        Role role = new Role("Admin", "Administrator role");

        assertNotNull(role.getId());
        assertEquals("Admin", role.getName());
        assertEquals("Administrator role", role.getDescription());
        assertTrue(role.getPermissions().isEmpty());
    }

    @Test
    void constructor_nullName_shouldThrow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Role(null, "desc")
        );
    }

    @Test
    void constructor_blankName_shouldThrow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Role("   ", "desc")
        );
    }

    @Test
    void constructor_nullDescription_shouldThrow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Role("Admin", null)
        );
    }

    @Test
    void constructor_blankDescription_shouldThrow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Role("Admin", "   ")
        );
    }

    @Test
    void addPermission_shouldAddPermission() {
        Role role = new Role("Admin", "Admin role");
        Permission permission =
                new Permission("READ", "users", "Read users");

        role.addPermission(permission);

        assertTrue(role.hasPermission(permission));
        assertEquals(1, role.getPermissions().size());
    }

    @Test
    void addPermission_null_shouldThrow() {
        Role role = new Role("Admin", "Admin role");

        assertThrows(
                IllegalArgumentException.class,
                () -> role.addPermission(null)
        );
    }

    @Test
    void removePermission_shouldRemovePermission() {
        Role role = new Role("Admin", "Admin role");
        Permission permission =
                new Permission("READ", "users", "Read users");

        role.addPermission(permission);
        role.removePermission(permission);

        assertFalse(role.hasPermission(permission));
        assertTrue(role.getPermissions().isEmpty());
    }

    @Test
    void hasPermission_byPermission_shouldReturnTrue() {
        Role role = new Role("Admin", "Admin role");
        Permission permission =
                new Permission("WRITE", "users", "Write users");

        role.addPermission(permission);

        assertTrue(role.hasPermission(permission));
    }

    @Test
    void hasPermission_byPermission_shouldReturnFalse() {
        Role role = new Role("Admin", "Admin role");
        Permission permission =
                new Permission("DELETE", "users", "Delete users");

        assertFalse(role.hasPermission(permission));
    }

    @Test
    void hasPermission_byPattern_shouldMatch() {
        Role role = new Role("Admin", "Admin role");
        Permission permission =
                new Permission("READ", "users", "Read users");

        role.addPermission(permission);

        assertTrue(role.hasPermission("READ", "users"));
        assertTrue(role.hasPermission("RE.*", "us.*"));
    }

    @Test
    void hasPermission_byPattern_shouldNotMatch() {
        Role role = new Role("Admin", "Admin role");
        Permission permission =
                new Permission("READ", "users", "Read users");

        role.addPermission(permission);

        assertFalse(role.hasPermission("WRITE", "users"));
        assertFalse(role.hasPermission("READ", "settings"));
    }

    @Test
    void getPermissions_shouldReturnUnmodifiableSet() {
        Role role = new Role("Admin", "Admin role");

        Set<Permission> permissions = role.getPermissions();

        assertThrows(
                UnsupportedOperationException.class,
                () -> permissions.add(
                        new Permission("READ", "users", "Read users")
                )
        );
    }

    @Test
    void equals_sameId_shouldBeEqual() {
        Role role1 = new Role("Admin", "Admin role");
        Role role2 = role1;

        assertEquals(role1, role2);
        assertEquals(role1.hashCode(), role2.hashCode());
    }

    @Test
    void equals_differentIds_shouldNotBeEqual() {
        Role role1 = new Role("Admin", "Admin role");
        Role role2 = new Role("Admin", "Admin role");

        assertNotEquals(role1, role2);
    }

    @Test
    void toString_shouldContainRoleData() {
        Role role = new Role("Admin", "Admin role");

        String text = role.toString();

        assertTrue(text.contains("Admin"));
        assertTrue(text.contains("Admin role"));
        assertTrue(text.contains("permissions"));
    }

    @Test
    void format_shouldReturnFormattedOutput() {
        Role role = new Role("Admin", "Admin role");
        Permission permission =
                new Permission("READ", "users", "Read users");

        role.addPermission(permission);

        String formatted = role.format();

        assertTrue(formatted.contains("Role: Admin"));
        assertTrue(formatted.contains("Description: Admin role"));
        assertTrue(formatted.contains("Permissions (1):"));
        assertTrue(formatted.contains("READ on users: Read users"));
    }
}