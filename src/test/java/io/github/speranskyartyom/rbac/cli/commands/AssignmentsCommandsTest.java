package io.github.speranskyartyom.rbac.cli.commands;

import io.github.speranskyartyom.rbac.cli.CommandParser;
import io.github.speranskyartyom.rbac.core.RBACSystem;
import io.github.speranskyartyom.rbac.interfaces.RoleAssignment;
import io.github.speranskyartyom.rbac.logging.AuditLog;
import io.github.speranskyartyom.rbac.managers.AssignmentManager;
import io.github.speranskyartyom.rbac.managers.RoleManager;
import io.github.speranskyartyom.rbac.managers.UserManager;
import io.github.speranskyartyom.rbac.models.PermanentAssignment;
import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.TemporaryAssignment;
import io.github.speranskyartyom.rbac.models.records.AssignmentMetadata;
import io.github.speranskyartyom.rbac.models.records.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

class AssignmentsCommandsTest {

    private RBACSystem mockSystem;
    private UserManager mockUserManager;
    private RoleManager mockRoleManager;
    private AssignmentManager mockAssignmentManager;
    private AuditLog mockLogger;
    private CommandParser parser;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        mockUserManager = mock(UserManager.class);
        mockRoleManager = mock(RoleManager.class);
        mockAssignmentManager = mock(AssignmentManager.class);
        mockLogger = mock(AuditLog.class);
        mockSystem = mock(RBACSystem.class);
        when(mockSystem.getUserManager()).thenReturn(mockUserManager);
        when(mockSystem.getRoleManager()).thenReturn(mockRoleManager);
        when(mockSystem.getAssignmentManager()).thenReturn(mockAssignmentManager);
        when(mockSystem.getLogger()).thenReturn(mockLogger);

        parser = new CommandParser();
        AssignmentsCommands.registerCommands(parser);

        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ========== assign-role tests ==========

    @Test
    @DisplayName("assign-role with all arguments (permanent) should create permanent assignment")
    void assignRolePermanentWithArgs() {
        User user = new User("john", "John Doe", "john@doe.com");
        Role role = new Role("Admin", "Administrator");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));
        when(mockSystem.getCurrentUser()).thenReturn("admin");

        parser.parseAndExecute("assign-role john Admin permanent \"Main admin\"", new Scanner(""), mockSystem);

        verify(mockAssignmentManager, times(1)).add(argThat(assignment ->
                assignment instanceof PermanentAssignment &&
                        assignment.user().equals(user) &&
                        assignment.role().equals(role) &&
                        assignment.metadata().assignedBy().equals("admin") &&
                        assignment.metadata().reason().equals("Main admin")
        ));
        String output = outContent.toString();
        assertTrue(output.contains("Role assigned successfully"));
    }

    @Test
    @DisplayName("assign-role with all arguments (temporary) should create temporary assignment")
    void assignRoleTemporaryWithArgs() {
        User user = new User("john", "John Doe", "john@doe.com");
        Role role = new Role("Admin", "Administrator");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));
        when(mockSystem.getCurrentUser()).thenReturn("admin");
        String expiration = "2025-12-31T23:59:59+00:00";

        parser.parseAndExecute("assign-role john Admin temporary " + expiration + " \"Temporary admin\"", new Scanner(""), mockSystem);

        verify(mockAssignmentManager, times(1)).add(argThat(assignment ->
                assignment instanceof TemporaryAssignment &&
                        ((TemporaryAssignment) assignment).isExpired() &&
                        assignment.user().equals(user) &&
                        assignment.role().equals(role) &&
                        assignment.metadata().assignedBy().equals("admin") &&
                        assignment.metadata().reason().equals("Temporary admin")
        ));
        String output = outContent.toString();
        assertTrue(output.contains("Role assigned successfully"));
    }

    @Test
    @DisplayName("assign-role interactive should prompt and create permanent assignment")
    void assignRoleInteractivePermanent() {
        User user = new User("john", "John Doe", "john@doe.com");
        Role role1 = new Role("Admin", "Administrator");
        Role role2 = new Role("Viewer", "Viewer");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockRoleManager.findAll()).thenReturn(List.of(role1, role2));
        when(mockSystem.getCurrentUser()).thenReturn("admin");

        String input = "john\n1\n1\nMain admin\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("assign-role", scanner, mockSystem);

        verify(mockAssignmentManager, times(1)).add(argThat(assignment ->
                assignment instanceof PermanentAssignment &&
                        assignment.user().equals(user) &&
                        assignment.role().equals(role1) &&
                        assignment.metadata().reason().equals("Main admin")
        ));
        String output = outContent.toString();
        assertTrue(output.contains("Available roles:"));
        assertTrue(output.contains("1 - Admin"));
        assertTrue(output.contains("2 - Viewer"));
        assertTrue(output.contains("Enter role number"));
        assertTrue(output.contains("Choose assignment type"));
        assertTrue(output.contains("Enter assignment reason"));
        assertTrue(output.contains("Role assigned successfully"));
    }

    @Test
    @DisplayName("assign-role interactive temporary should prompt for date")
    void assignRoleInteractiveTemporary() {
        User user = new User("john", "John Doe", "john@doe.com");
        Role role = new Role("Admin", "Administrator");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockRoleManager.findAll()).thenReturn(List.of(role));
        when(mockSystem.getCurrentUser()).thenReturn("admin");

        String expiration = "2025-12-31T23:59:59+00:00";
        String input = "john\n1\n2\n" + expiration + "\nTemporary admin\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("assign-role", scanner, mockSystem);

        verify(mockAssignmentManager, times(1)).add(argThat(assignment ->
                assignment instanceof TemporaryAssignment
        ));
    }

    @Test
    @DisplayName("assign-role with non-existing user should print error")
    void assignRoleUserNotFound() {
        when(mockUserManager.findByUsername("unknown")).thenReturn(Optional.empty());

        parser.parseAndExecute("assign-role unknown Admin permanent reason", new Scanner(""), mockSystem);

        verify(mockAssignmentManager, never()).add(any());
        String output = outContent.toString();
        assertTrue(output.contains("Error: No such user."));
    }

    @Test
    @DisplayName("assign-role with non-existing role should print error")
    void assignRoleRoleNotFound() {
        User user = new User("john", "John Doe", "john@doe.com");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockRoleManager.findByName("unknown")).thenReturn(Optional.empty());

        parser.parseAndExecute("assign-role john unknown permanent reason", new Scanner(""), mockSystem);

        verify(mockAssignmentManager, never()).add(any());
        String output = outContent.toString();
        assertTrue(output.contains("Error: no such role."));
    }

    @Test
    @DisplayName("assign-role with invalid type should print error")
    void assignRoleInvalidType() {
        User user = new User("john", "John Doe", "john@doe.com");
        Role role = new Role("Admin", "Administrator");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));

        parser.parseAndExecute("assign-role john Admin invalid reason", new Scanner(""), mockSystem);

        verify(mockAssignmentManager, never()).add(any());
        String output = outContent.toString();
        assertTrue(output.contains("Error: Invalid assignment type."));
    }

    @Test
    @DisplayName("assign-role with extra arguments should warn")
    void assignRoleExtraArgs() {
        User user = new User("john", "John Doe", "john@doe.com");
        Role role = new Role("Admin", "Administrator");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));
        when(mockSystem.getCurrentUser()).thenReturn("admin");

        parser.parseAndExecute("assign-role john Admin permanent reason extra stuff", new Scanner(""), mockSystem);

        verify(mockAssignmentManager, times(1)).add(any());
        String output = outContent.toString();
        assertTrue(output.contains("Warning: extra arguments ignored: extra stuff"));
    }

    // ========== revoke-role tests ==========

    @Test
    @DisplayName("revoke-role with username argument and valid selection should revoke permanent assignment")
    void revokeRolePermanent() {
        User user = new User("john", "John Doe", "john@doe.com");
        Role role = new Role("Admin", "Admin");
        PermanentAssignment assignment = new PermanentAssignment(user, role,
                AssignmentMetadata.now("admin", "reason"));
        when(mockUserManager.exists("john")).thenReturn(true);
        when(mockAssignmentManager.findByFilter(any())).thenReturn(List.of(assignment));

        String input = "1\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("revoke-role john", scanner, mockSystem);

        when(mockAssignmentManager.findByFilter(any())).thenReturn(List.of(assignment));
        PermanentAssignment spyAssignment = spy(new PermanentAssignment(user, role,
                AssignmentMetadata.now("admin", "reason")));
        when(mockAssignmentManager.findByFilter(any())).thenReturn(List.of(spyAssignment));

        parser.parseAndExecute("revoke-role john", new Scanner("1\n"), mockSystem);

        verify(spyAssignment).revoke();
        verify(mockAssignmentManager, never()).remove(any());
        String output = outContent.toString();
        assertTrue(output.contains("Assignment revoked successfully"));
    }

    @Test
    @DisplayName("revoke-role with temporary assignment should remove it")
    void revokeRoleTemporary() {
        User user = new User("john", "John Doe", "john@doe.com");
        Role role = new Role("Admin", "Admin");
        TemporaryAssignment assignment = new TemporaryAssignment(user, role,
                AssignmentMetadata.now("admin", "reason"), "2025-01-01T00:00:00+00:00", false);
        when(mockUserManager.exists("john")).thenReturn(true);
        when(mockAssignmentManager.findByFilter(any())).thenReturn(List.of(assignment));

        parser.parseAndExecute("revoke-role john", new Scanner("1\n"), mockSystem);

        verify(mockAssignmentManager).remove(assignment);
        String output = outContent.toString();
        assertTrue(output.contains("Assignment revoked successfully"));
    }

    @Test
    @DisplayName("revoke-role with cancel should not revoke")
    void revokeRoleCancel() {
        User user = new User("john", "John Doe", "john@doe.com");
        Role role = new Role("Admin", "Admin");
        PermanentAssignment assignment = new PermanentAssignment(user, role,
                AssignmentMetadata.now("admin", "reason"));
        when(mockUserManager.exists("john")).thenReturn(true);
        when(mockAssignmentManager.findByFilter(any())).thenReturn(List.of(assignment));

        parser.parseAndExecute("revoke-role john", new Scanner("cancel\n"), mockSystem);

        verify(mockAssignmentManager, never()).remove(any());
        String output = outContent.toString();
        assertTrue(output.contains("Assignment revoke cancelled"));
    }

    @Test
    @DisplayName("revoke-role with non-existing user should print error")
    void revokeRoleUserNotFound() {
        when(mockUserManager.exists("unknown")).thenReturn(false);

        parser.parseAndExecute("revoke-role unknown", new Scanner(""), mockSystem);

        verify(mockAssignmentManager, never()).findByFilter(any());
        String output = outContent.toString();
        assertTrue(output.contains("No such user."));
    }

    @Test
    @DisplayName("revoke-role with no active assignments should print message")
    void revokeRoleNoAssignments() {
        when(mockUserManager.exists("john")).thenReturn(true);
        when(mockAssignmentManager.findByFilter(any())).thenReturn(List.of());

        parser.parseAndExecute("revoke-role john", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("User has no assignments."));
    }

    // ========== assignment-list tests ==========

    @Test
    @DisplayName("assignment-list should print table of all assignments")
    void assignmentList() {
        User user = new User("john", "John", "j@d.com");
        Role role = new Role("Admin", "Admin");
        PermanentAssignment assignment = new PermanentAssignment(user, role,
                AssignmentMetadata.now("admin", "reason"));
        when(mockAssignmentManager.findAll()).thenReturn(List.of(assignment));

        parser.parseAndExecute("assignment-list", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("john"));
        assertTrue(output.contains("Admin"));
        assertTrue(output.contains("PERMANENT"));
        assertTrue(output.contains("ACTIVE"));
        assertTrue(output.contains(assignment.metadata().assignedAt()));
    }

    @Test
    @DisplayName("assignment-list with extra arguments should warn")
    void assignmentListExtraArgs() {
        when(mockAssignmentManager.findAll()).thenReturn(List.of());

        parser.parseAndExecute("assignment-list extra stuff", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Warning: extra arguments ignored: extra stuff"));
        verify(mockAssignmentManager, times(1)).findAll();
    }

    // ========== assignment-list-user tests ==========

    @Test
    @DisplayName("assignment-list-user with username should print assignments summary")
    void assignmentListUser() {
        User user = new User("john", "John", "j@d.com");
        Role role = new Role("Admin", "Admin");
        PermanentAssignment assignment = new PermanentAssignment(user, role,
                AssignmentMetadata.now("admin", "reason"));
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockAssignmentManager.findByUser(user)).thenReturn(List.of(assignment));

        parser.parseAndExecute("assignment-list-user john", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Role assignments:"));
        assertTrue(output.contains(assignment.summary()));
    }

    @Test
    @DisplayName("assignment-list-user interactive should prompt for username")
    void assignmentListUserInteractive() {
        User user = new User("john", "John", "j@d.com");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockAssignmentManager.findByUser(user)).thenReturn(List.of());

        parser.parseAndExecute("assignment-list-user", new Scanner("john\n"), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Enter username"));
        assertTrue(output.contains("User has no role assignments"));
    }

    @Test
    @DisplayName("assignment-list-user with non-existing user should print error")
    void assignmentListUserNotFound() {
        when(mockUserManager.findByUsername("unknown")).thenReturn(Optional.empty());

        parser.parseAndExecute("assignment-list-user unknown", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("No such user."));
    }

    // ========== assignment-list-role tests ==========

    @Test
    @DisplayName("assignment-list-role with role name should list users")
    void assignmentListRole() {
        Role role = new Role("Admin", "Admin");
        User user1 = new User("john", "John", "j@d.com");
        User user2 = new User("jane", "Jane", "jane@d.com");
        RoleAssignment assignment1 = mock(RoleAssignment.class);
        when(assignment1.user()).thenReturn(user1);
        RoleAssignment assignment2 = mock(RoleAssignment.class);
        when(assignment2.user()).thenReturn(user2);
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));
        when(mockAssignmentManager.findByRole(role)).thenReturn(List.of(assignment1, assignment2));

        parser.parseAndExecute("assignment-list-role Admin", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Users with role Admin: john, jane."));
    }

    @Test
    @DisplayName("assignment-list-role with no assignments should print message")
    void assignmentListRoleNoAssignments() {
        Role role = new Role("Admin", "Admin");
        when(mockRoleManager.findByName("Admin")).thenReturn(Optional.of(role));
        when(mockAssignmentManager.findByRole(role)).thenReturn(List.of());

        parser.parseAndExecute("assignment-list-role Admin", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("The role is not assigned to any user"));
    }

    // ========== assignment-active tests ==========

    @Test
    @DisplayName("assignment-active should print active assignments")
    void assignmentActive() {
        User user = new User("john", "John", "j@d.com");
        Role role = new Role("Admin", "Admin");
        PermanentAssignment assignment = new PermanentAssignment(user, role,
                AssignmentMetadata.now("admin", "reason"));
        when(mockAssignmentManager.getActiveAssignments()).thenReturn(List.of(assignment));

        parser.parseAndExecute("assignment-active", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("john"));
        assertTrue(output.contains("Admin"));
        assertTrue(output.contains("PERMANENT"));
        assertTrue(output.contains("ACTIVE"));
    }

    // ========== assignment-expired tests ==========

    @Test
    @DisplayName("assignment-expired should print expired assignments")
    void assignmentExpired() {
        User user = new User("john", "John", "j@d.com");
        Role role = new Role("Admin", "Admin");
        TemporaryAssignment assignment = new TemporaryAssignment(user, role,
                AssignmentMetadata.now("admin", "reason"), "2020-01-01T00:00:00+00:00", true);
        when(mockAssignmentManager.getExpiredAssignments()).thenReturn(List.of(assignment));

        parser.parseAndExecute("assignment-expired", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("john"));
        assertTrue(output.contains("Admin"));
        assertTrue(output.contains("TEMPORARY"));
        assertTrue(output.contains("INACTIVE") || output.contains("EXPIRED"));
    }

    // ========== assignment-extend tests ==========

    @Test
    @DisplayName("assignment-extend by ID should extend temporary assignment")
    void assignmentExtendById() {
        String assignmentId = "assign123";
        String newDate = "2026-01-01T00:00:00+00:00";
        TemporaryAssignment mockAssignment = mock(TemporaryAssignment.class);
        when(mockAssignment.assignmentId()).thenReturn(assignmentId);
        when(mockAssignmentManager.findById(assignmentId)).thenReturn(Optional.of(mockAssignment));

        String input = newDate + "\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("assignment-extend " + assignmentId, scanner, mockSystem);

        verify(mockAssignmentManager).extendTemporaryAssignment(assignmentId, newDate);
        String output = outContent.toString();
        assertTrue(output.contains("Assignment extended successfully"));
    }

    @Test
    @DisplayName("assignment-extend by username and role should find and extend")
    void assignmentExtendByUsernameRole() {
        String username = "john";
        String roleName = "Admin";
        String newDate = "2026-01-01T00:00:00+00:00";
        User user = new User(username, "John", "j@d.com");
        Role role = new Role(roleName, "Admin");
        TemporaryAssignment assignment = new TemporaryAssignment(user, role,
                AssignmentMetadata.now("admin", "reason"), "2025-01-01T00:00:00+00:00", false);
        when(mockAssignmentManager.findByFilter(any())).thenReturn(List.of(assignment));

        String input = newDate + "\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("assignment-extend " + username + " " + roleName, scanner, mockSystem);

        verify(mockAssignmentManager).extendTemporaryAssignment(assignment.assignmentId(), newDate);
    }

    @Test
    @DisplayName("assignment-extend interactive should prompt for method")
    void assignmentExtendInteractive() {
        String assignmentId = "assign123";
        String newDate = "2026-01-01T00:00:00+00:00";
        TemporaryAssignment assignment = mock(TemporaryAssignment.class);
        when(assignment.assignmentId()).thenReturn(assignmentId);
        when(mockAssignmentManager.findById(assignmentId)).thenReturn(Optional.of(assignment));

        String input = "1\n" + assignmentId + "\n" + newDate + "\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("assignment-extend", scanner, mockSystem);

        verify(mockAssignmentManager).extendTemporaryAssignment(assignmentId, newDate);
    }

    @Test
    @DisplayName("assignment-extend with non-existing ID should print error")
    void assignmentExtendIdNotFound() {
        String assignmentId = "unknown";
        when(mockAssignmentManager.findById(assignmentId)).thenReturn(Optional.empty());

        parser.parseAndExecute("assignment-extend " + assignmentId, new Scanner(""), mockSystem);

        verify(mockAssignmentManager, never()).extendTemporaryAssignment(any(), any());
        String output = outContent.toString();
        assertTrue(output.contains("Assignment with ID 'unknown' not found."));
    }

    @Test
    @DisplayName("assignment-extend with non-temporary assignment should print error")
    void assignmentExtendNotTemporary() {
        String assignmentId = "assign123";
        PermanentAssignment assignment = mock(PermanentAssignment.class);
        when(mockAssignmentManager.findById(assignmentId)).thenReturn(Optional.of(assignment));

        parser.parseAndExecute("assignment-extend " + assignmentId, new Scanner(""), mockSystem);

        verify(mockAssignmentManager, never()).extendTemporaryAssignment(any(), any());
        String output = outContent.toString();
        assertTrue(output.contains("Assignment is not temporary. Cannot extend."));
    }

    @Test
    @DisplayName("assignment-extend with multiple temporary assignments should ask to use ID")
    void assignmentExtendMultiple() {
        String username = "john";
        String roleName = "Admin";
        User user = new User(username, "John", "j@d.com");
        Role role = new Role(roleName, "Admin");
        TemporaryAssignment a1 = new TemporaryAssignment(user, role,
                AssignmentMetadata.now("admin", "r1"), "2025-01-01T00:00:00+00:00", false);
        TemporaryAssignment a2 = new TemporaryAssignment(user, role,
                AssignmentMetadata.now("admin", "r2"), "2025-02-01T00:00:00+00:00", false);
        when(mockAssignmentManager.findByFilter(any())).thenReturn(List.of(a1, a2));

        parser.parseAndExecute("assignment-extend " + username + " " + roleName, new Scanner(""), mockSystem);

        verify(mockAssignmentManager, never()).extendTemporaryAssignment(any(), any());
        String output = outContent.toString();
        assertTrue(output.contains("Multiple temporary assignments found"));
        assertTrue(output.contains("Please use assignment ID"));
    }

    // ========== assignment-search tests ==========

    @Test
    @DisplayName("assignment-search with username filter should print filtered assignments")
    void assignmentSearchByUsername() {
        User user = new User("john", "John", "j@d.com");
        Role role = new Role("Admin", "Admin");
        PermanentAssignment assignment = new PermanentAssignment(user, role,
                AssignmentMetadata.now("admin", "reason"));
        when(mockAssignmentManager.findByFilter(any())).thenReturn(List.of(assignment));

        parser.parseAndExecute("assignment-search username=john", new Scanner(""), mockSystem);

        verify(mockAssignmentManager).findByFilter(any());
        String output = outContent.toString();
        assertTrue(output.contains("john"));
        assertTrue(output.contains("Admin"));
    }

    @Test
    @DisplayName("assignment-search with multiple filters should combine")
    void assignmentSearchMultipleFilters() {
        when(mockAssignmentManager.findByFilter(any())).thenReturn(List.of());

        parser.parseAndExecute("assignment-search username=john role=Admin", new Scanner(""), mockSystem);

        verify(mockAssignmentManager).findByFilter(any());
    }

    @Test
    @DisplayName("assignment-search interactive should collect filters then search")
    void assignmentSearchInteractive() {
        when(mockAssignmentManager.findByFilter(any())).thenReturn(List.of());

        String input = "username\njohn\nsearch\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("assignment-search", scanner, mockSystem);

        verify(mockAssignmentManager).findByFilter(any());
        String output = outContent.toString();
        assertTrue(output.contains("Enter a filter or type \"search\""));
    }

    @Test
    @DisplayName("assignment-search with invalid filter format should print error")
    void assignmentSearchInvalidFormat() {
        parser.parseAndExecute("assignment-search username:john", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Invalid filter format"));
        verifyNoInteractions(mockAssignmentManager);
    }

    @Test
    @DisplayName("assignment-search with unknown filter should print error")
    void assignmentSearchUnknownFilter() {
        parser.parseAndExecute("assignment-search foo=bar", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Unknown filter"));
        verifyNoInteractions(mockAssignmentManager);
    }
}