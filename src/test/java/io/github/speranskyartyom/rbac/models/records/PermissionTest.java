package io.github.speranskyartyom.rbac.models.records;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionTest {

    @Test
    void constructor_shouldNormalizeNameAndResource() {
        Permission permission =
                new Permission("read", "USERS", "Read users");

        assertEquals("READ", permission.name());
        assertEquals("users", permission.resource());
        assertEquals("Read users", permission.description());
    }

    @Test
    void constructor_validData_shouldCreate() {
        Permission permission =
                new Permission("WRITE", "settings", "Write settings");

        assertNotNull(permission);
    }

    @Test
    void constructor_nullName_shouldThrow() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Permission(null, "users", "desc")
        );

        assertTrue(ex.getMessage().contains("Name must not be null or blank"));
    }

    @Test
    void constructor_blankName_shouldThrow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Permission("   ", "users", "desc")
        );
    }

    @Test
    void constructor_nameWithSpaces_shouldThrow() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Permission("READ USERS", "users", "desc")
        );

        assertTrue(ex.getMessage().contains("must not contain spaces"));
    }

    @Test
    void constructor_nullResource_shouldThrow() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Permission("READ", null, "desc")
        );

        assertTrue(ex.getMessage().contains("Resource must not be null or blank"));
    }

    @Test
    void constructor_blankResource_shouldThrow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Permission("READ", "   ", "desc")
        );
    }

    @Test
    void constructor_nullDescription_shouldThrow() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Permission("READ", "users", null)
        );

        assertTrue(ex.getMessage().contains("Description must not be null or blank"));
    }

    @Test
    void constructor_blankDescription_shouldThrow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Permission("READ", "users", "   ")
        );
    }

    @Test
    void format_shouldReturnFormattedString() {
        Permission permission =
                new Permission("READ", "users", "Read users");

        String formatted = permission.format();

        assertEquals("READ on users: Read users", formatted);
    }

    @Test
    void matches_exactMatch_shouldReturnTrue() {
        Permission permission =
                new Permission("READ", "users", "Read users");

        assertTrue(permission.matches("READ", "users"));
    }

    @Test
    void matches_regexMatch_shouldReturnTrue() {
        Permission permission =
                new Permission("DELETE", "reports", "Delete reports");

        assertTrue(permission.matches("DEL.*", "rep.*"));
    }

    @Test
    void matches_nullPatterns_shouldMatchAll() {
        Permission permission =
                new Permission("WRITE", "settings", "Write settings");

        assertTrue(permission.matches(null, null));
    }

    @Test
    void matches_nameMismatch_shouldReturnFalse() {
        Permission permission =
                new Permission("READ", "users", "Read users");

        assertFalse(permission.matches("WRITE", "users"));
    }

    @Test
    void matches_resourceMismatch_shouldReturnFalse() {
        Permission permission =
                new Permission("READ", "users", "Read users");

        assertFalse(permission.matches("READ", "settings"));
    }
}