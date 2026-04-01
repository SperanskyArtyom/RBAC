package io.github.speranskyartyom.rbac.models.records;

import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentMetadataTest {
    @Test
    void constructor_validData_shouldCreate() {
        String assignedAt = OffsetDateTime.now()
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        AssignmentMetadata metadata =
                new AssignmentMetadata("admin", assignedAt, "initial assignment");

        assertEquals("admin", metadata.assignedBy());
        assertEquals(assignedAt, metadata.assignedAt());
        assertEquals("initial assignment", metadata.reason());
    }

    @Test
    void constructor_nullAssignedBy_shouldThrow() {
        String assignedAt = OffsetDateTime.now()
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new AssignmentMetadata(null, assignedAt, null)
        );

        assertEquals("assignedBy must not be null or blank", ex.getMessage());
    }

    @Test
    void constructor_blankAssignedBy_shouldThrow() {
        String assignedAt = OffsetDateTime.now()
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        assertThrows(
                IllegalArgumentException.class,
                () -> new AssignmentMetadata("   ", assignedAt, null)
        );
    }

    @Test
    void constructor_nullAssignedAt_shouldThrow() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new AssignmentMetadata("admin", null, null)
        );

        assertEquals("assignedAt must not be null or blank", ex.getMessage());
    }

    @Test
    void constructor_blankAssignedAt_shouldThrow() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new AssignmentMetadata("admin", "   ", null)
        );
    }

    @Test
    void constructor_invalidDateFormat_shouldThrow() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new AssignmentMetadata("admin", "2024-01-01 12:00", null)
        );

        assertEquals("assignedAt must be ISO offset format", ex.getMessage());
    }

    @Test
    void now_shouldCreateMetadataWithCurrentTime() {
        AssignmentMetadata metadata =
                AssignmentMetadata.now("admin", "auto assign");

        assertEquals("admin", metadata.assignedBy());
        assertEquals("auto assign", metadata.reason());

        assertDoesNotThrow(() ->
                OffsetDateTime.parse(
                        metadata.assignedAt(),
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME
                )
        );
    }

    @Test
    void format_withReason_shouldIncludeReason() {
        String assignedAt = OffsetDateTime.now()
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        AssignmentMetadata metadata =
                new AssignmentMetadata("admin", assignedAt, "manual");

        String formatted = metadata.format();

        assertTrue(formatted.contains("Assignment metadata"));
        assertTrue(formatted.contains("admin"));
        assertTrue(formatted.contains(assignedAt));
        assertTrue(formatted.contains("manual"));
    }

    @Test
    void format_withoutReason_shouldNotIncludeReason() {
        String assignedAt = OffsetDateTime.now()
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        AssignmentMetadata metadata =
                new AssignmentMetadata("admin", assignedAt, null);

        String formatted = metadata.format();

        assertTrue(formatted.contains("Assignment metadata"));
        assertTrue(formatted.contains("admin"));
        assertTrue(formatted.contains(assignedAt));
        assertFalse(formatted.contains("reason"));
    }
}