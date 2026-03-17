package io.github.speranskyartyom.rbac.managers;

import io.github.speranskyartyom.rbac.interfaces.RoleAssignment;
import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.records.Permission;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RoleManagerTest {

    private RoleManager roleManager;
    private AssignmentManager assignmentManager;

    @BeforeEach
    void setUp() {
        roleManager = new RoleManager();
        assignmentManager = mock(AssignmentManager.class);
        roleManager.setAssignmentManager(assignmentManager);
    }

    @Test
    void add_shouldAddRole() {
        Role role = new Role("ADMIN", "1");

        roleManager.add(role);

        assertTrue(roleManager.exists("ADMIN"));
        assertEquals(1, roleManager.count());
    }

    @Test
    void add_duplicateRoleName_shouldThrow() {
        Role role = new Role("ADMIN", "1");
        roleManager.add(role);

        assertThrows(IllegalArgumentException.class,
                () -> roleManager.add(new Role("ADMIN", "2")));
    }

    @Test
    void findByName_existingRole() {
        Role role = new Role("ADMIN", "1");
        roleManager.add(role);

        assertTrue(roleManager.findByName("ADMIN").isPresent());
    }

    @Test
    void addPermissionToRole() {
        Role role = new Role("ADMIN", "1");
        roleManager.add(role);

        Permission permission = new Permission("READ", "user", "1");

        roleManager.addPermissionToRole("ADMIN", permission);

        assertTrue(role.hasPermission(permission));
    }

    @Test
    void remove_assignedRole_shouldThrow() {
        Role role = new Role("ADMIN", "1");
        roleManager.add(role);

        RoleAssignment assignment = mock(RoleAssignment.class);

        when(assignment.role()).thenReturn(role);

        when(assignmentManager.getActiveAssignments()).thenReturn(
                List.of(assignment)
        );

        assertThrows(IllegalArgumentException.class,
                () -> roleManager.remove(role));
    }
}
