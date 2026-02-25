package types.classes;

import org.junit.jupiter.api.Test;
import types.records.AssignmentMetadata;
import types.records.User;

import static org.junit.jupiter.api.Assertions.*;

class PermanentAssignmentTest {

    private final User user = new User("john", "John Doe", "john@mail.com");
    private final Role role = new Role("Admin", "Administrator role");
    private final AssignmentMetadata metadata = AssignmentMetadata.now("admin", "Test");

    @Test
    void constructor_shouldCreateAssignment() {
        PermanentAssignment assignment =
                new PermanentAssignment(user, role, metadata);

        assertNotNull(assignment.assignmentId());
        assertEquals(user, assignment.user());
        assertEquals(role, assignment.role());
        assertEquals(metadata, assignment.metadata());
    }

    @Test
    void assignmentType_shouldBePermanent() {
        PermanentAssignment assignment =
                new PermanentAssignment(user, role, metadata);

        assertEquals("PERMANENT", assignment.assignmentType());
    }

    @Test
    void isActive_initially_shouldBeTrue() {
        PermanentAssignment assignment =
                new PermanentAssignment(user, role, metadata);

        assertTrue(assignment.isActive());
        assertFalse(assignment.isRevoked());
    }

    @Test
    void revoke_shouldDeactivateAssignment() {
        PermanentAssignment assignment =
                new PermanentAssignment(user, role, metadata);

        assignment.revoke();

        assertTrue(assignment.isRevoked());
        assertFalse(assignment.isActive());
    }

    @Test
    void revoke_twice_shouldRemainRevoked() {
        PermanentAssignment assignment =
                new PermanentAssignment(user, role, metadata);

        assignment.revoke();
        assignment.revoke();

        assertTrue(assignment.isRevoked());
        assertFalse(assignment.isActive());
    }

    @Test
    void equals_differentAssignments_shouldNotBeEqual() {
        PermanentAssignment assignment1 =
                new PermanentAssignment(user, role, metadata);
        PermanentAssignment assignment2 =
                new PermanentAssignment(user, role, metadata);

        assertNotEquals(assignment1, assignment2);
    }

    @Test
    void summary_shouldContainAllRequiredParts() {
        PermanentAssignment assignment =
                new PermanentAssignment(user, role, metadata);

        String summary = assignment.summary();

        assertTrue(summary.contains("[PERMANENT]"));
        assertTrue(summary.contains(role.getName()));
        assertTrue(summary.contains(user.username()));
        assertTrue(summary.contains(metadata.assignedBy()));
        assertTrue(summary.contains(metadata.assignedAt()));
        assertTrue(summary.contains("Status: ACTIVE"));
    }

    @Test
    void summary_afterRevoke_shouldShowInactive() {
        PermanentAssignment assignment =
                new PermanentAssignment(user, role, metadata);

        assignment.revoke();

        String summary = assignment.summary();

        assertTrue(summary.contains("Status: INACTIVE"));
    }
}