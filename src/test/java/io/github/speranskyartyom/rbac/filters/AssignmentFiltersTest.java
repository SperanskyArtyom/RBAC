package io.github.speranskyartyom.rbac.filters;

import io.github.speranskyartyom.rbac.interfaces.functional.AssignmentFilter;
import io.github.speranskyartyom.rbac.models.PermanentAssignment;
import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.TemporaryAssignment;
import io.github.speranskyartyom.rbac.models.records.AssignmentMetadata;
import io.github.speranskyartyom.rbac.models.records.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AssignmentFiltersTest {

    private User user;
    private Role role;
    private PermanentAssignment permanent;
    private TemporaryAssignment temporary;

    @BeforeEach
    void setUp() {
        user = new User("John", "John Doe", "john@mail.com");
        User otherUser = new User("Jane", "Jane Doe", "jane@mail.com");
        role = new Role("ADMIN", "Administrator role");

        AssignmentMetadata metadata = new AssignmentMetadata(
                "admin",
                OffsetDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                "Initial setup"
        );

        permanent = new PermanentAssignment(user, role, metadata);
        temporary = new TemporaryAssignment(
                otherUser,
                role,
                new AssignmentMetadata(
                        "admin",
                        OffsetDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        "Temp assignment"
                ),
                OffsetDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                false
        );
    }

    @Test
    void byUser_matches() {
        AssignmentFilter filter = AssignmentFilters.byUser(user);

        assertTrue(filter.test(permanent));
        assertFalse(filter.test(temporary));
    }

    @Test
    void byUsername_matches() {
        AssignmentFilter filter = AssignmentFilters.byUsername("John");

        assertTrue(filter.test(permanent));
        assertFalse(filter.test(temporary));
    }

    @Test
    void byRole_matches() {
        AssignmentFilter filter = AssignmentFilters.byRole(role);

        assertTrue(filter.test(permanent));
        assertTrue(filter.test(temporary));
    }

    @Test
    void byRoleName_matches() {
        AssignmentFilter filter = AssignmentFilters.byRoleName("ADMIN");

        assertTrue(filter.test(permanent));
        assertTrue(filter.test(temporary));
    }

    @Test
    void activeOnly_matches() {
        permanent.revoke();
        AssignmentFilter filter = AssignmentFilters.activeOnly();

        assertTrue(filter.test(temporary));
        assertFalse(filter.test(permanent));
    }

    @Test
    void inactiveOnly_matches() {
        permanent.revoke();
        AssignmentFilter filter = AssignmentFilters.inactiveOnly();

        assertFalse(filter.test(temporary));
        assertTrue(filter.test(permanent));
    }

    @Test
    void byType_matches() {
        AssignmentFilter filterPermanent = AssignmentFilters.byType("PERMANENT");
        AssignmentFilter filterTemporary = AssignmentFilters.byType("TEMPORARY");

        assertTrue(filterPermanent.test(permanent));
        assertFalse(filterPermanent.test(temporary));

        assertTrue(filterTemporary.test(temporary));
        assertFalse(filterTemporary.test(permanent));
    }

    @Test
    void assignedBy_matches() {
        AssignmentFilter filter = AssignmentFilters.assignedBy("admin");

        assertTrue(filter.test(permanent));
        assertTrue(filter.test(temporary));

        AssignmentFilter filterOther = AssignmentFilters.assignedBy("someoneElse");
        assertFalse(filterOther.test(permanent));
    }

    @Test
    void assignedAfter_matches() {
        String date = OffsetDateTime.now().minusDays(2).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        AssignmentFilter filter = AssignmentFilters.assignedAfter(date);

        assertTrue(filter.test(permanent));
        assertTrue(filter.test(temporary));

        String futureDate = OffsetDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        AssignmentFilter filterFuture = AssignmentFilters.assignedAfter(futureDate);
        assertFalse(filterFuture.test(permanent));
    }

    @Test
    void expiringBefore_matches() {
        String future = OffsetDateTime.now().plusDays(2).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        AssignmentFilter filter = AssignmentFilters.expiringBefore(future);

        assertTrue(filter.test(temporary));
        assertFalse(filter.test(permanent));

        String past = OffsetDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        AssignmentFilter filterPast = AssignmentFilters.expiringBefore(past);
        assertFalse(filterPast.test(temporary));
    }

    @Test
    void and_combination() {
        AssignmentFilter filter = AssignmentFilters.byUser(user)
                .and(AssignmentFilters.byType("PERMANENT"));

        assertTrue(filter.test(permanent));
        assertFalse(filter.test(temporary));
    }

    @Test
    void or_combination() {
        AssignmentFilter filter = AssignmentFilters.byUser(user)
                .or(AssignmentFilters.byType("TEMPORARY"));

        assertTrue(filter.test(permanent));
        assertTrue(filter.test(temporary));
    }
}