package io.github.speranskyartyom.rbac.cli.commands;

import io.github.speranskyartyom.rbac.cli.CommandParser;
import io.github.speranskyartyom.rbac.core.RBACSystem;
import io.github.speranskyartyom.rbac.managers.AssignmentManager;
import io.github.speranskyartyom.rbac.managers.RoleManager;
import io.github.speranskyartyom.rbac.managers.UserManager;
import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.records.Permission;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PermissionCommandsTest {

    private RBACSystem mockSystem;
    private UserManager mockUserManager;
    private AssignmentManager mockAssignmentManager;
    private RoleManager mockRoleManager;
    private CommandParser parser;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        mockUserManager = mock(UserManager.class);
        mockAssignmentManager = mock(AssignmentManager.class);
        mockRoleManager = mock(RoleManager.class);
        mockSystem = mock(RBACSystem.class);
        when(mockSystem.getUserManager()).thenReturn(mockUserManager);
        when(mockSystem.getAssignmentManager()).thenReturn(mockAssignmentManager);
        when(mockSystem.getRoleManager()).thenReturn(mockRoleManager);

        parser = new CommandParser();
        PermissionCommands.registerCommands(parser);

        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ========== permissions-user tests ==========

    @Test
    @DisplayName("permissions-user with username argument should print user's permissions")
    void permissionsUserWithArgs() {
        User user = new User("john", "John Doe", "john@doe.com");
        Permission perm1 = new Permission("READ", "users", "Read users");
        Permission perm2 = new Permission("WRITE", "users", "Write users");
        Permission perm3 = new Permission("READ", "reports", "Read reports");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockAssignmentManager.getUserPermissions(user)).thenReturn(Set.of(perm1, perm2, perm3));

        parser.parseAndExecute("permissions-user john", new Scanner(""), mockSystem);

        String output = outContent.toString();

        assertTrue(output.contains("READ on users: Read users"));
        assertTrue(output.contains("WRITE on users: Write users"));
        assertTrue(output.contains("READ on reports: Read reports"));
        assertTrue(output.indexOf("reports") < output.indexOf("users") || output.indexOf("users") < output.indexOf("reports"));
    }

    @Test
    @DisplayName("permissions-user interactive should prompt for username")
    void permissionsUserInteractive() {
        User user = new User("john", "John Doe", "john@doe.com");
        Permission perm = new Permission("READ", "users", "Read users");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockAssignmentManager.getUserPermissions(user)).thenReturn(Set.of(perm));

        String input = "john\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("permissions-user", scanner, mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Enter username"));
        assertTrue(output.contains("READ on users: Read users"));
    }

    @Test
    @DisplayName("permissions-user with non-existing user should print error")
    void permissionsUserUserNotFound() {
        when(mockUserManager.findByUsername("unknown")).thenReturn(Optional.empty());

        parser.parseAndExecute("permissions-user unknown", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("No such user."));
        verify(mockAssignmentManager, never()).getUserPermissions(any());
    }

    @Test
    @DisplayName("permissions-user when user has no permissions should print message")
    void permissionsUserNoPermissions() {
        User user = new User("john", "John Doe", "john@doe.com");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockAssignmentManager.getUserPermissions(user)).thenReturn(Set.of());

        parser.parseAndExecute("permissions-user john", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("User has no permissions."));
    }

    @Test
    @DisplayName("permissions-user with extra arguments should warn")
    void permissionsUserExtraArgs() {
        User user = new User("john", "John Doe", "john@doe.com");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockAssignmentManager.getUserPermissions(user)).thenReturn(Set.of());

        parser.parseAndExecute("permissions-user john extra stuff", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Warning: extra arguments ignored: extra stuff"));
    }

    // ========== permissions-check tests ==========

    @Test
    @DisplayName("permissions-check with all arguments and permission exists should print success and roles")
    void permissionsCheckWithArgsPermissionExists() {
        User user = new User("john", "John Doe", "john@doe.com");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockAssignmentManager.userHasPermission(user, "READ", "users")).thenReturn(true);

        Role role1 = new Role("Viewer", "Can view");
        Role role2 = new Role("Manager", "Can manage");
        when(mockRoleManager.findByFilter(any())).thenReturn(List.of(role1, role2));

        parser.parseAndExecute("permissions-check john READ users", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("User 'john' has permission READ on 'users'"));
        assertTrue(output.contains("Granted by roles: Viewer, Manager."));
        verify(mockRoleManager).findByFilter(any());
    }

    @Test
    @DisplayName("permissions-check with all arguments and permission does not exist should print not have")
    void permissionsCheckWithArgsPermissionNotExists() {
        User user = new User("john", "John Doe", "john@doe.com");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockAssignmentManager.userHasPermission(user, "DELETE", "users")).thenReturn(false);

        parser.parseAndExecute("permissions-check john DELETE users", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("User 'john' doesn't have permission DELETE on 'users'"));
        assertFalse(output.contains("Granted by roles"));
        verify(mockRoleManager, never()).findByFilter(any());
    }

    @Test
    @DisplayName("permissions-check interactive should prompt for username, name, resource")
    void permissionsCheckInteractive() {
        User user = new User("john", "John Doe", "john@doe.com");
        Role role = new Role("Viewer", "test role");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockAssignmentManager.userHasPermission(user, "READ", "users")).thenReturn(true);
        when(mockRoleManager.findByFilter(any())).thenReturn(List.of(role));

        String input = "john\nREAD\nusers\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("permissions-check", scanner, mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Enter username"));
        assertTrue(output.contains("Enter permission name"));
        assertTrue(output.contains("Enter permission's resource"));
        assertTrue(output.contains("User 'john' has permission READ on 'users'"));
    }

    @Test
    @DisplayName("permissions-check with missing arguments should prompt for missing ones")
    void permissionsCheckPartialArgs() {
        User user = new User("john", "John Doe", "john@doe.com");
        Role role = new Role("Viewer", "test role");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockAssignmentManager.userHasPermission(user, "READ", "users")).thenReturn(true);
        when(mockRoleManager.findByFilter(any())).thenReturn(List.of(role));

        String input = "READ\nusers\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("permissions-check john", scanner, mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Enter permission name"));
        assertTrue(output.contains("Enter permission's resource"));
        assertTrue(output.contains("User 'john' has permission READ on 'users'"));
    }

    @Test
    @DisplayName("permissions-check with non-existing user should print error")
    void permissionsCheckUserNotFound() {
        when(mockUserManager.findByUsername("unknown")).thenReturn(Optional.empty());

        parser.parseAndExecute("permissions-check unknown READ users", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("No such user."));
        verify(mockAssignmentManager, never()).userHasPermission(any(), any(), any());
    }

    @Test
    @DisplayName("permissions-check with extra arguments should warn")
    void permissionsCheckExtraArgs() {
        User user = new User("john", "John Doe", "john@doe.com");
        Role role = new Role("Viewer", "test role");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockAssignmentManager.userHasPermission(user, "READ", "users")).thenReturn(true);
        when(mockRoleManager.findByFilter(any())).thenReturn(List.of(role));

        parser.parseAndExecute("permissions-check john READ users extra stuff", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Warning: extra arguments ignored: extra stuff"));
    }

    @Test
    @DisplayName("permissions-check should call roleManager.findByFilter with correct filter when permission exists")
    void permissionsCheckCallsRoleFilter() {
        User user = new User("john", "John Doe", "john@doe.com");
        Role role = new Role("Viewer", "test role");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockAssignmentManager.userHasPermission(user, "READ", "users")).thenReturn(true);
        when(mockRoleManager.findByFilter(any())).thenReturn(List.of(role));

        parser.parseAndExecute("permissions-check john READ users", new Scanner(""), mockSystem);

        verify(mockRoleManager).findByFilter(any());
    }
}