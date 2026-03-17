package io.github.speranskyartyom.rbac.cli.commands;

import io.github.speranskyartyom.rbac.cli.CommandParser;
import io.github.speranskyartyom.rbac.core.RBACSystem;
import io.github.speranskyartyom.rbac.interfaces.RoleAssignment;
import io.github.speranskyartyom.rbac.managers.AssignmentManager;
import io.github.speranskyartyom.rbac.managers.RoleManager;
import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.records.Permission;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RolesCommandsTest {

    private RBACSystem mockSystem;
    private RoleManager mockRoleManager;
    private AssignmentManager mockAssignmentManager;
    private CommandParser parser;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        mockRoleManager = mock(RoleManager.class);
        mockAssignmentManager = mock(AssignmentManager.class);
        mockSystem = mock(RBACSystem.class);
        when(mockSystem.getRoleManager()).thenReturn(mockRoleManager);
        when(mockSystem.getAssignmentManager()).thenReturn(mockAssignmentManager);

        parser = new CommandParser();
        RolesCommands.registerRoleCommands(parser);

        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ========== role-list tests ==========

    @Test
    @DisplayName("role-list without arguments should print all roles")
    void roleListNoArgs() {
        Role role1 = new Role("Admin", "Administrator");
        Role role2 = new Role("Viewer", "Viewer");
        when(mockRoleManager.findAll()).thenReturn(List.of(role1, role2));

        parser.parseAndExecute("role-list", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains(role1.format()));
        assertTrue(output.contains(role2.format()));
        verify(mockRoleManager, times(1)).findAll();
    }

    @Test
    @DisplayName("role-list with extra arguments should warn")
    void roleListExtraArgs() {
        when(mockRoleManager.findAll()).thenReturn(List.of());

        parser.parseAndExecute("role-list extra stuff", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Warning: extra arguments ignored: extra stuff"));
        verify(mockRoleManager, times(1)).findAll();
    }

    @Test
    @DisplayName("role-list when no roles should print message")
    void roleListEmpty() {
        when(mockRoleManager.findAll()).thenReturn(List.of());

        parser.parseAndExecute("role-list", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("System has no roles."));
    }

    // ========== role-create tests ==========

    @Test
    @DisplayName("role-create with name and description arguments should create role")
    void roleCreateWithArgs() {
        parser.parseAndExecute("role-create Moderator \"Moderator role\"", new Scanner("stop"), mockSystem);

        verify(mockRoleManager, times(1)).add(argThat(role ->
                role.getName().equals("Moderator") &&
                        role.getDescription().equals("Moderator role")
        ));
        String output = outContent.toString();
        assertTrue(output.contains("Role Moderator added successfully"));
    }

    @Test
    @DisplayName("role-create interactive should prompt for name and description")
    void roleCreateInteractive() {
        String input = "Moderator\nModerator role\nstop\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("role-create", scanner, mockSystem);

        verify(mockRoleManager, times(1)).add(argThat(role ->
                role.getName().equals("Moderator") &&
                        role.getDescription().equals("Moderator role")
        ));
        String output = outContent.toString();
        assertTrue(output.contains("Enter role name"));
        assertTrue(output.contains("Enter role description"));
        assertTrue(output.contains("Role Moderator added successfully"));
    }

    @Test
    @DisplayName("role-create with permission addition should add permissions")
    void roleCreateWithPermissions() {
        // Simulate adding two permissions then "stop"
        String input = """
                Moderator
                Moderator role
                read messages "Can read messages"
                delete messages "Can delete messages"
                stop
                """;
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("role-create", scanner, mockSystem);

        verify(mockRoleManager, times(1)).add(argThat(role -> {
            Set<Permission> perms = role.getPermissions();
            return perms.size() == 2 &&
                    perms.stream().anyMatch(p -> p.name().equals("READ") && p.resource().equals("messages")) &&
                    perms.stream().anyMatch(p -> p.name().equals("DELETE") && p.resource().equals("messages"));
        }));
    }

    @Test
    @DisplayName("role-create with invalid role data should print error")
    void roleCreateInvalid() {
        // Role constructor may throw (e.g., blank name)
        parser.parseAndExecute("role-create \"\" \"desc\"", new Scanner(""), mockSystem);

        verify(mockRoleManager, never()).add(any());
        String output = outContent.toString();
        assertTrue(output.contains("Error"));
    }

    // ========== role-view tests ==========

    @Test
    @DisplayName("role-view with existing role should print role format")
    void roleViewExisting() {
        Role role = new Role("Admin", "Administrator");
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));

        parser.parseAndExecute("role-view Admin", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains(role.format()));
    }

    @Test
    @DisplayName("role-view with non-existing role should print error")
    void roleViewNonExisting() {
        when(mockRoleManager.findByName("unknown")).thenReturn(Optional.empty());

        parser.parseAndExecute("role-view unknown", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("No such role - \"unknown\""));
    }

    @Test
    @DisplayName("role-view interactive should prompt for role name")
    void roleViewInteractive() {
        Role role = new Role("Admin", "Administrator");
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));

        parser.parseAndExecute("role-view", new Scanner("Admin\n"), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Enter role name"));
        assertTrue(output.contains(role.format()));
    }

    // ========== role-update tests ==========

    @Test
    @DisplayName("role-update with all arguments should update role")
    void roleUpdateWithArgs() {
        parser.parseAndExecute("role-update Admin \"SuperAdmin\" \"New description\"", new Scanner(""), mockSystem);

        verify(mockRoleManager, times(1)).update("Admin", "SuperAdmin", "New description");
        String output = outContent.toString();
        assertTrue(output.contains("Role updated successfully"));
    }

    @Test
    @DisplayName("role-update interactive should prompt for fields")
    void roleUpdateInteractive() {
        String input = "Admin\nSuperAdmin\nNew description\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("role-update", scanner, mockSystem);

        verify(mockRoleManager, times(1)).update("Admin", "SuperAdmin", "New description");
        String output = outContent.toString();
        assertTrue(output.contains("Enter role's name"));
        assertTrue(output.contains("Enter new name"));
        assertTrue(output.contains("Enter new description"));
        assertTrue(output.contains("Role updated successfully"));
    }

    @Test
    @DisplayName("role-update when update throws should print error")
    void roleUpdateError() {
        doThrow(new IllegalArgumentException("Role not found")).when(mockRoleManager).update(any(), any(), any());

        parser.parseAndExecute("role-update Admin NewName NewDesc", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Error: Role not found"));
    }

    // ========== role-delete tests ==========

    @Test
    @DisplayName("role-delete with -y flag and no assignments should delete")
    void roleDeleteWithFlagNoAssignments() {
        Role role = new Role("Admin", "Administrator");
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));
        when(mockAssignmentManager.getActiveAssignments()).thenReturn(List.of());

        parser.parseAndExecute("role-delete Admin -y", new Scanner(""), mockSystem);

        verify(mockRoleManager).remove(role);
        String output = outContent.toString();
        assertTrue(output.contains("Role removed successfully"));
    }

    @Test
    @DisplayName("role-delete with -y flag and assignments should delete assignments and role")
    void roleDeleteWithFlagAndAssignments() {
        Role role = new Role("Admin", "Administrator");
        RoleAssignment assignment = mock(RoleAssignment.class);
        when(assignment.user()).thenReturn(mock(io.github.speranskyartyom.rbac.models.records.User.class));
        when(assignment.role()).thenReturn(role);
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));
        when(mockAssignmentManager.getActiveAssignments()).thenReturn(List.of(assignment));

        parser.parseAndExecute("role-delete Admin -y", new Scanner(""), mockSystem);

        verify(mockAssignmentManager).remove(assignment);
        verify(mockRoleManager).remove(role);
        String output = outContent.toString();
        assertTrue(output.contains("Assignment for user"));
        assertTrue(output.contains("Role removed successfully"));
    }

    @Test
    @DisplayName("role-delete interactive without confirmation should cancel")
    void roleDeleteInteractiveCancelled() {
        Role role = new Role("Admin", "Administrator");
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));
        when(mockAssignmentManager.getActiveAssignments()).thenReturn(List.of());

        parser.parseAndExecute("role-delete", new Scanner("Admin\nn\n"), mockSystem);

        verify(mockRoleManager, never()).remove(any());
        String output = outContent.toString();
        assertTrue(output.contains("Delete role Admin? (y/n)"));
        assertTrue(output.contains("Deletion cancelled"));
    }

    @Test
    @DisplayName("role-delete interactive with confirmation should delete")
    void roleDeleteInteractiveConfirmed() {
        Role role = new Role("Admin", "Administrator");
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));
        when(mockAssignmentManager.getActiveAssignments()).thenReturn(List.of());

        parser.parseAndExecute("role-delete", new Scanner("Admin\ny\n"), mockSystem);

        verify(mockRoleManager).remove(role);
        String output = outContent.toString();
        assertTrue(output.contains("Role removed successfully"));
    }

    @Test
    @DisplayName("role-delete with non-existing role should print error")
    void roleDeleteNonExisting() {
        when(mockRoleManager.findByName("unknown")).thenReturn(Optional.empty());

        parser.parseAndExecute("role-delete unknown", new Scanner(""), mockSystem);

        verify(mockRoleManager, never()).remove(any());
        String output = outContent.toString();
        assertTrue(output.contains("No such role - \"unknown\""));
    }

    // ========== role-add-permission tests ==========

    @Test
    @DisplayName("role-add-permission with all arguments should add permission")
    void roleAddPermissionWithArgs() {
        parser.parseAndExecute("role-add-permission Admin read messages \"Can read messages\"", new Scanner(""), mockSystem);

        verify(mockRoleManager, times(1)).addPermissionToRole(eq("Admin"), any(Permission.class));
        String output = outContent.toString();
        assertTrue(output.contains("Permission added successfully"));
    }

    @Test
    @DisplayName("role-add-permission interactive should prompt for fields")
    void roleAddPermissionInteractive() {
        String input = "Admin\nread\nmessages\nCan read messages\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("role-add-permission", scanner, mockSystem);

        verify(mockRoleManager, times(1)).addPermissionToRole(eq("Admin"), any(Permission.class));
        String output = outContent.toString();
        assertTrue(output.contains("Enter role's name"));
        assertTrue(output.contains("Enter new permission's name"));
        assertTrue(output.contains("Enter new permission's resource"));
        assertTrue(output.contains("Enter new permission's description"));
        assertTrue(output.contains("Permission added successfully"));
    }

    @Test
    @DisplayName("role-add-permission when add throws should print error")
    void roleAddPermissionError() {
        doThrow(new IllegalArgumentException("Role not found")).when(mockRoleManager).addPermissionToRole(any(), any());

        parser.parseAndExecute("role-add-permission Admin read messages desc", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Error: Role not found"));
    }

    // ========== role-remove-permission tests ==========

    @Test
    @DisplayName("role-remove-permission with valid number should remove permission")
    void roleRemovePermissionValid() {
        Role role = new Role("Admin", "Administrator");
        Permission perm1 = new Permission("write", "messages", "desc");
        Permission perm2 = new Permission("read", "messages", "desc");
        role.addPermission(perm1);
        role.addPermission(perm2);
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));

        String input = "2\n"; // choose second permission
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("role-remove-permission Admin", scanner, mockSystem);

        verify(mockRoleManager, times(1)).removePermissionFromRole("Admin", perm2);
        String output = outContent.toString();
        assertTrue(output.contains("Permissions:"));
        assertTrue(output.contains("1 - " + perm1.format()));
        assertTrue(output.contains("2 - " + perm2.format()));
        assertTrue(output.contains("Permission " + perm2.format() + " removed successfully."));
    }

    @Test
    @DisplayName("role-remove-permission with cancel should not remove")
    void roleRemovePermissionCancel() {
        Role role = new Role("Admin", "Administrator");
        role.addPermission(new Permission("read", "messages", "desc"));
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));

        String input = "cancel\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("role-remove-permission Admin", scanner, mockSystem);

        verify(mockRoleManager, never()).removePermissionFromRole(any(), any());
        String output = outContent.toString();
        assertTrue(output.contains("Deletion cancelled"));
    }

    @Test
    @DisplayName("role-remove-permission with invalid number should retry")
    void roleRemovePermissionInvalidNumber() {
        Role role = new Role("Admin", "Administrator");
        role.addPermission(new Permission("read", "messages", "desc"));
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));

        String input = "5\n1\n"; // first invalid, then valid
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("role-remove-permission Admin", scanner, mockSystem);

        verify(mockRoleManager, times(1)).removePermissionFromRole(any(), any());
        String output = outContent.toString();
        assertTrue(output.contains("Number 5 is not in range [1, 1]"));
    }

    @Test
    @DisplayName("role-remove-permission when role has no permissions should print message")
    void roleRemovePermissionNoPermissions() {
        Role role = new Role("Admin", "Administrator");
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));

        parser.parseAndExecute("role-remove-permission Admin", new Scanner(""), mockSystem);

        verify(mockRoleManager, never()).removePermissionFromRole(any(), any());
        String output = outContent.toString();
        assertTrue(output.contains("doesn't have any permission"));
    }

    // ========== role-search tests ==========

    @Test
    @DisplayName("role-search with role-name filter should delegate to findByFilter")
    void roleSearchByNameFilter() {
        Role role = new Role("Admin", "Administrator");
        when(mockRoleManager.findByFilter(any())).thenReturn(List.of(role));

        parser.parseAndExecute("role-search role-name=Admin", new Scanner(""), mockSystem);

        verify(mockRoleManager).findByFilter(any());
        String output = outContent.toString();
        assertTrue(output.contains("Found roles"));
        assertTrue(output.contains(role.format()));
    }

    @Test
    @DisplayName("role-search with permission filter (name only) should delegate")
    void roleSearchPermissionFilterNameOnly() {
        when(mockRoleManager.findByFilter(any())).thenReturn(List.of());

        parser.parseAndExecute("role-search permission=read", new Scanner(""), mockSystem);

        verify(mockRoleManager).findByFilter(any());
        String output = outContent.toString();
        assertTrue(output.contains("No roles found"));
    }

    @Test
    @DisplayName("role-search with permission filter (name and resource) should delegate")
    void roleSearchPermissionFilterFull() {
        when(mockRoleManager.findByFilter(any())).thenReturn(List.of());

        parser.parseAndExecute("role-search permission=read,messages", new Scanner(""), mockSystem);

        verify(mockRoleManager).findByFilter(any());
    }

    @Test
    @DisplayName("role-search with min-permissions filter should delegate")
    void roleSearchMinPermissions() {
        when(mockRoleManager.findByFilter(any())).thenReturn(List.of());

        parser.parseAndExecute("role-search min-permissions=3", new Scanner(""), mockSystem);

        verify(mockRoleManager).findByFilter(any());
    }

    @Test
    @DisplayName("role-search with invalid filter format should print error")
    void roleSearchInvalidFormat() {
        parser.parseAndExecute("role-search foo", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Invalid filter format"));
        verifyNoInteractions(mockRoleManager);
    }

    @Test
    @DisplayName("role-search with unknown filter should print error")
    void roleSearchUnknownFilter() {
        parser.parseAndExecute("role-search foo=bar", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Unknown filter"));
        verifyNoInteractions(mockRoleManager);
    }

    @Test
    @DisplayName("role-search interactive should collect filter then search")
    void roleSearchInteractive() {
        // Simulate choosing filter 1 (by name), entering "Admin", then search
        String input = "1\nAdmin\n";
        Scanner scanner = new Scanner(input);
        when(mockRoleManager.findByFilter(any())).thenReturn(List.of(new Role("Admin", "desc")));

        parser.parseAndExecute("role-search", scanner, mockSystem);

        verify(mockRoleManager).findByFilter(any());
        String output = outContent.toString();
        assertTrue(output.contains("Found roles"));
    }

    @Test
    @DisplayName("role-search interactive with extra arguments warning")
    void roleSearchExtraArgsWarning() {
        when(mockRoleManager.findByFilter(any())).thenReturn(List.of());

        parser.parseAndExecute("role-search role-name=Admin extra", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Warning: extra arguments ignored: extra"));
    }
}