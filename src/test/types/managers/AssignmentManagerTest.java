package types.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import types.classes.PermanentAssignment;
import types.classes.Role;
import types.classes.TemporaryAssignment;
import types.records.AssignmentMetadata;
import types.records.Permission;
import types.records.User;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AssignmentManagerTest {

    private AssignmentManager assignmentManager;

    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        // создаём моки зависимостей
        UserManager userManager = mock(UserManager.class);
        RoleManager roleManager = mock(RoleManager.class);

        assignmentManager = new AssignmentManager(userManager, roleManager);

        // создаём реальные объекты для тестов
        user = new User("john", "John Doe", "john@mail.com");
        role = new Role("ADMIN", "Administrator role");

        // настройка моков для существующих пользователей и ролей
        when(userManager.exists("john")).thenReturn(true);
        when(roleManager.exists("ADMIN")).thenReturn(true);
        when(userManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(roleManager.findByName("ADMIN")).thenReturn(Optional.of(role));
    }

    @Test
    void add_assignment_shouldAdd() {
        PermanentAssignment assignment = new PermanentAssignment(
                user,
                role,
                AssignmentMetadata.now("admin", "Test"));

        assignmentManager.add(assignment);

        assertEquals(1, assignmentManager.count());
        assertTrue(assignmentManager.userHasRole(user, role));
    }

    @Test
    void add_duplicateActiveRole_shouldThrow() {
        PermanentAssignment assignment =
                new PermanentAssignment(
                        user,
                        role,
                        AssignmentMetadata.now("admin", "Test"));

        assignmentManager.add(assignment);

        assertThrows(IllegalArgumentException.class,
                () -> assignmentManager.add(
                        new PermanentAssignment(
                                user,
                                role,
                                AssignmentMetadata.now("admin", "Test"))
                ));
    }

    @Test
    void getUserPermissions_shouldAggregate() {
        // даём роли одно разрешение
        role.addPermission(new Permission(
                "READ",
                "USER",
                "Read user info"));
        PermanentAssignment assignment = new PermanentAssignment(
                user,
                role,
                AssignmentMetadata.now("admin", "Test"));
        assignmentManager.add(assignment);

        assertEquals(1, assignmentManager.getUserPermissions(user).size());
        assertTrue(assignmentManager.getUserPermissions(user).stream()
                .anyMatch(p ->
                        p.name().equals("READ") &&
                                p.resource().equals("user")));
    }

    @Test
    void revokePermanentAssignment_shouldDeactivate() {
        PermanentAssignment assignment = new PermanentAssignment(
                user,
                role,
                AssignmentMetadata.now("admin", "Test"));
        assignmentManager.add(assignment);

        assignmentManager.revokeAssignment(assignment.assignmentId());

        assertFalse(assignment.isActive());
    }

    @Test
    void extendTemporaryAssignment_shouldExtend() {
        TemporaryAssignment assignment = new TemporaryAssignment(
                user,
                role,
                AssignmentMetadata.now("admin", "Test"),
                OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                false
        );

        assignmentManager.add(assignment);

        assignmentManager.extendTemporaryAssignment(
                assignment.assignmentId(),
                OffsetDateTime.now().plusDays(10).toString()
        );

        assertFalse(assignment.isExpired());
    }
}