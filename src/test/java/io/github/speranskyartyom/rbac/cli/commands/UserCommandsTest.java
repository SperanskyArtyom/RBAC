package io.github.speranskyartyom.rbac.cli.commands;

import io.github.speranskyartyom.rbac.cli.CommandParser;
import io.github.speranskyartyom.rbac.core.RBACSystem;
import io.github.speranskyartyom.rbac.interfaces.RoleAssignment;
import io.github.speranskyartyom.rbac.managers.AssignmentManager;
import io.github.speranskyartyom.rbac.managers.UserManager;
import io.github.speranskyartyom.rbac.models.Role;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserCommandsTest {

    private RBACSystem mockSystem;
    private UserManager mockUserManager;
    private AssignmentManager mockAssignmentManager;
    private CommandParser parser;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        mockUserManager = mock(UserManager.class);
        mockAssignmentManager = mock(AssignmentManager.class);
        mockSystem = mock(RBACSystem.class);
        when(mockSystem.getUserManager()).thenReturn(mockUserManager);
        when(mockSystem.getAssignmentManager()).thenReturn(mockAssignmentManager);

        parser = new CommandParser();
        UserCommands.registerUserCommands(parser);

        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ========== user-list tests ==========

    @Test
    @DisplayName("user-list without arguments should print all users")
    void userListNoArgs() {
        User user1 = new User("john", "John Doe", "john@doe.com");
        User user2 = new User("jane", "Jane Doe", "jane@doe.com");
        when(mockUserManager.findAll()).thenReturn(List.of(user1, user2));

        parser.parseAndExecute("user-list", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("john"));
        assertTrue(output.contains("John Doe"));
        assertTrue(output.contains("john@doe.com"));
        assertTrue(output.contains("jane"));
        assertTrue(output.contains("Jane Doe"));
        assertTrue(output.contains("jane@doe.com"));
        verify(mockUserManager, times(1)).findAll();
    }

    @Test
    @DisplayName("user-list with valid filter username=john should print filtered users")
    void userListWithUsernameFilter() {
        User user1 = new User("john", "John Doe", "john@doe.com");
        when(mockUserManager.findByFilter(any())).thenReturn(List.of(user1));

        parser.parseAndExecute("user-list username=john", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("john"));
        assertFalse(output.contains("jane"));
        verify(mockUserManager, times(1)).findByFilter(any());
    }

    @Test
    @DisplayName("user-list with invalid filter format should print error")
    void userListInvalidFilterFormat() {
        parser.parseAndExecute("user-list username:john", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Invalid filter format"));
        verifyNoInteractions(mockUserManager);
    }

    @Test
    @DisplayName("user-list with unknown filter should print error")
    void userListUnknownFilter() {
        parser.parseAndExecute("user-list foo=bar", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Unknown filter"));
        verifyNoInteractions(mockUserManager);
    }

    @Test
    @DisplayName("user-list with multiple valid filters should combine them")
    void userListMultipleFilters() {
        when(mockUserManager.findByFilter(any())).thenReturn(List.of());

        parser.parseAndExecute("user-list username=john email=doe", new Scanner(""), mockSystem);

        verify(mockUserManager, times(1)).findByFilter(any());
    }

    // ========== user-create tests ==========

    @Test
    @DisplayName("user-create with all arguments should create user")
    void userCreateWithArgs() {
        parser.parseAndExecute("user-create john \"John Doe\" john@doe.com", new Scanner(""), mockSystem);

        verify(mockUserManager, times(1)).add(any(User.class));
        String output = outContent.toString();
        assertTrue(output.contains("User created successfully"));
    }

    @Test
    @DisplayName("user-create with missing arguments should prompt interactively")
    void userCreateInteractive() {
        String input = "john\nJohn Doe\njohn@doe.com\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("user-create", scanner, mockSystem);

        verify(mockUserManager, times(1)).add(argThat(user ->
                user.username().equals("john") &&
                        user.fullName().equals("John Doe") &&
                        user.email().equals("john@doe.com")
        ));
        String output = outContent.toString();
        assertTrue(output.contains("Enter username"));
        assertTrue(output.contains("Enter full name"));
        assertTrue(output.contains("Enter email"));
        assertTrue(output.contains("User created successfully"));
    }

    @Test
    @DisplayName("user-create with extra arguments should warn")
    void userCreateExtraArgs() {
        parser.parseAndExecute("user-create john \"John Doe\" john@doe.com extra stuff", new Scanner(""), mockSystem);

        verify(mockUserManager, times(1)).add(any());
        String output = outContent.toString();
        assertTrue(output.contains("Warning: extra arguments ignored: extra stuff"));
    }

    @Test
    @DisplayName("user-create when add throws exception should print error")
    void userCreateError() {
        doThrow(new IllegalArgumentException("Invalid email")).when(mockUserManager).add(any());

        parser.parseAndExecute("user-create john \"John Doe\" invalid-email", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Error: Email has invalid format."));
    }

    // ========== user-view tests ==========

    @Test
    @DisplayName("user-view with existing user should print user and roles")
    void userViewExistingUser() {
        User user = new User("john", "John Doe", "john@doe.com");
        Role role1 = new Role("Admin", "Administrator");
        Role role2 = new Role("Viewer", "Viewer");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        RoleAssignment assignment1 = mock(RoleAssignment.class);
        when(assignment1.role()).thenReturn(role1);
        RoleAssignment assignment2 = mock(RoleAssignment.class);
        when(assignment2.role()).thenReturn(role2);
        when(mockAssignmentManager.findByUser(user)).thenReturn(List.of(assignment1, assignment2));

        parser.parseAndExecute("user-view john", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains(user.format()));
        assertTrue(output.contains(role1.format()));
        assertTrue(output.contains(role2.format()));
    }

    @Test
    @DisplayName("user-view with non-existing user should print error")
    void userViewNonExistingUser() {
        when(mockUserManager.findByUsername("unknown")).thenReturn(Optional.empty());

        parser.parseAndExecute("user-view unknown", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("No user with username - unknown"));
    }

    @Test
    @DisplayName("user-view interactive should prompt for username")
    void userViewInteractive() {
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(new User("john", "John", "j@d.com")));

        parser.parseAndExecute("user-view", new Scanner("john\n"), mockSystem);

        verify(mockUserManager).findByUsername("john");
        String output = outContent.toString();
        assertTrue(output.contains("Enter username"));
    }

    // ========== user-update tests ==========

    @Test
    @DisplayName("user-update with all arguments should update user")
    void userUpdateWithArgs() {
        parser.parseAndExecute("user-update john \"John Updated\" john.new@doe.com", new Scanner(""), mockSystem);

        verify(mockUserManager, times(1)).update("john", "John Updated", "john.new@doe.com");
        String output = outContent.toString();
        assertTrue(output.contains("User data updated successfully"));
    }

    @Test
    @DisplayName("user-update interactive should prompt for fields")
    void userUpdateInteractive() {
        Scanner scanner = new Scanner("john\nJohn Updated\njohn.new@doe.com\n");

        parser.parseAndExecute("user-update", scanner, mockSystem);

        verify(mockUserManager, times(1)).update("john", "John Updated", "john.new@doe.com");
        String output = outContent.toString();
        assertTrue(output.contains("Enter username to update"));
        assertTrue(output.contains("Enter new full name"));
        assertTrue(output.contains("Enter new email"));
    }

    @Test
    @DisplayName("user-update when update throws should print error")
    void userUpdateError() {
        doThrow(new IllegalArgumentException("User not found")).when(mockUserManager).update(any(), any(), any());

        parser.parseAndExecute("user-update john \"John\" j@d.com", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Error: User not found"));
    }

    // ========== user-delete tests ==========

    @Test
    @DisplayName("user-delete with -y flag should delete without confirmation")
    void userDeleteWithFlag() {
        User user = new User("john", "John", "j@d.com");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockSystem.getCurrentUser()).thenReturn("admin");

        parser.parseAndExecute("user-delete john -y", new Scanner(""), mockSystem);

        verify(mockAssignmentManager).findByUser(user);
        verify(mockUserManager).remove(user);
        String output = outContent.toString();
        assertTrue(output.contains("User john removed successfully"));
    }

    @Test
    @DisplayName("user-delete interactive with confirmation should delete")
    void userDeleteInteractiveConfirmed() {
        User user = new User("john", "John", "j@d.com");
        when(mockUserManager.findByUsername("john")).thenReturn(Optional.of(user));
        when(mockSystem.getCurrentUser()).thenReturn("admin");

        parser.parseAndExecute("user-delete", new Scanner("john\ny\n"), mockSystem);

        verify(mockUserManager).remove(user);
        String output = outContent.toString();
        assertTrue(output.contains("Delete user john?"));
        assertTrue(output.contains("User john removed successfully"));
    }

    @Test
    @DisplayName("user-delete interactive with cancellation should not delete")
    void userDeleteInteractiveCancelled() {
        when(mockSystem.getCurrentUser()).thenReturn("admin");

        parser.parseAndExecute("user-delete", new Scanner("john\nn\n"), mockSystem);

        verify(mockUserManager, never()).remove(any());
        String output = outContent.toString();
        assertTrue(output.contains("Deletion cancelled"));
    }

    @Test
    @DisplayName("user-delete trying to delete self should print error")
    void userDeleteSelf() {
        when(mockSystem.getCurrentUser()).thenReturn("john");

        parser.parseAndExecute("user-delete john", new Scanner(""), mockSystem);

        verifyNoInteractions(mockUserManager);
        String output = outContent.toString();
        assertTrue(output.contains("Error: you can not delete yourself"));
    }

    // ========== user-search tests ==========

    @Test
    @DisplayName("user-search with arguments should delegate to user-list")
    void userSearchWithArgs() {
        User user = new User("john", "John", "j@d.com");
        when(mockUserManager.findByFilter(any())).thenReturn(List.of(user));

        parser.parseAndExecute("user-search username=john", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("john"));
        verify(mockUserManager).findByFilter(any());
    }

    @Test
    @DisplayName("user-search interactive should collect filters then search")
    void userSearchInteractive() {
        User user = new User("john", "John", "john@doe.com");
        when(mockUserManager.findByFilter(any())).thenReturn(List.of(user));

        // Simulate: choose filter "username", enter "john", then "search"
        String input = "1\njohn\n5\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("user-search", scanner, mockSystem);

        verify(mockUserManager).findByFilter(any());
        String output = outContent.toString();
        assertTrue(output.contains("john"));
    }
}