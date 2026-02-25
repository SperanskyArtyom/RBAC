package types.classes;

import org.junit.jupiter.api.Test;
import types.records.AssignmentMetadata;
import types.records.User;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

class TemporaryAssignmentTest {

    private final User user = new User("john", "John Doe", "john@mail.com");
    private final Role role = new Role("Admin", "Administrator role");
    private final AssignmentMetadata metadata = AssignmentMetadata.now(
            "admin", "temporary access");
    private final String futureDate = OffsetDateTime.now().plusDays(1)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    private final String pastDate = OffsetDateTime.now().minusDays(1)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

    @Test
    void constructor_shouldCreateTemporaryAssignment() {
        TemporaryAssignment assignment =
                new TemporaryAssignment(user, role, metadata, futureDate, false);

        assertNotNull(assignment.assignmentId());
        assertEquals(user, assignment.user());
        assertEquals(role, assignment.role());
        assertEquals(metadata, assignment.metadata());
    }

    @Test
    void constructor_invalidDate_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> new TemporaryAssignment(
                        user, role, metadata, "invalid-date", false));
    }

    @Test
    void assignmentType_shouldBeTemporary() {
        TemporaryAssignment assignment =
                new TemporaryAssignment(user, role, metadata, futureDate, false);

        assertEquals("TEMPORARY", assignment.assignmentType());
    }

    @Test
    void isActive_futureExpiration_shouldBeTrue() {
        TemporaryAssignment assignment =
                new TemporaryAssignment(user, role, metadata, futureDate, false);

        assertTrue(assignment.isActive());
        assertFalse(assignment.isExpired());
    }

    @Test
    void isActive_pastExpiration_shouldBeFalse() {
        TemporaryAssignment assignment =
                new TemporaryAssignment(user, role, metadata, pastDate, false);

        assertFalse(assignment.isActive());
        assertTrue(assignment.isExpired());
    }

    @Test
    void isExpired_withProvidedNow_shouldWorkCorrectly() {
        String expiresAt =
                OffsetDateTime.now().plusHours(1)
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        TemporaryAssignment assignment =
                new TemporaryAssignment(user, role, metadata, expiresAt, false);

        assertFalse(assignment.isExpired(OffsetDateTime.now()));
        assertTrue(assignment.isExpired(OffsetDateTime.now().plusDays(1)));
    }

    @Test
    void extend_shouldUpdateExpirationDate() {
        TemporaryAssignment assignment =
                new TemporaryAssignment(user, role, metadata, pastDate, false);

        assertTrue(assignment.isExpired());

        assignment.extend(futureDate);

        assertFalse(assignment.isExpired());
        assertTrue(assignment.isActive());
    }

    @Test
    void extend_invalidDate_shouldThrow() {
        TemporaryAssignment assignment =
                new TemporaryAssignment(user, role, metadata, futureDate, false);

        assertThrows(IllegalArgumentException.class,
                () -> assignment.extend("bad-date"));
    }

    @Test
    void getTimeRemaining_expired_shouldReturnExpired() {
        TemporaryAssignment assignment =
                new TemporaryAssignment(user, role, metadata, pastDate, false);

        assertEquals("Expired", assignment.getTimeRemaining());
    }

    @Test
    void getTimeRemaining_future_shouldContainTimeUnits() {
        TemporaryAssignment assignment =
                new TemporaryAssignment(user, role, metadata, futureDate, false);

        String remaining = assignment.getTimeRemaining();

        assertTrue(remaining.contains("days"));
        assertTrue(remaining.contains("hours"));
        assertTrue(remaining.contains("minutes"));
        assertTrue(remaining.contains("seconds"));
    }

    @Test
    void summary_shouldContainTemporaryAndRemainingTime() {
        TemporaryAssignment assignment =
                new TemporaryAssignment(user, role, metadata, futureDate, false);

        String summary = assignment.summary();

        assertTrue(summary.contains("[TEMPORARY]"));
        assertTrue(summary.contains("Remaining time"));
        assertTrue(summary.contains(user.username()));
        assertTrue(summary.contains(role.getName()));
    }
}