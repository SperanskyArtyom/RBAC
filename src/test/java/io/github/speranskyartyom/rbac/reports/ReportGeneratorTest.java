package io.github.speranskyartyom.rbac.reports;

import io.github.speranskyartyom.rbac.interfaces.RoleAssignment;
import io.github.speranskyartyom.rbac.managers.AssignmentManager;
import io.github.speranskyartyom.rbac.managers.RoleManager;
import io.github.speranskyartyom.rbac.managers.UserManager;
import io.github.speranskyartyom.rbac.models.AbstractRoleAssignment;
import io.github.speranskyartyom.rbac.models.PermanentAssignment;
import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.records.AssignmentMetadata;
import io.github.speranskyartyom.rbac.models.records.Permission;
import io.github.speranskyartyom.rbac.models.records.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportGeneratorTest {

    private UserManager userManager;
    private RoleManager roleManager;
    private AssignmentManager assignmentManager;

    @BeforeEach
    void setUp() {
        userManager = mock(UserManager.class);
        roleManager = mock(RoleManager.class);
        assignmentManager = mock(AssignmentManager.class);
    }

    // ==================== generateUserReport ====================

    @Test
    @DisplayName("Should generate report for users without roles")
    void shouldGenerateUserReportWithoutRoles() {
        User user1 = new User("john_doe", "John Doe", "john@example.com");
        User user2 = new User("jane_smith", "Jane Smith", "jane@example.com");
        when(userManager.findAll()).thenReturn(List.of(user1, user2));
        when(assignmentManager.findByUser(any(User.class))).thenReturn(List.of());

        String report = ReportGenerator.generateUserReport(userManager, assignmentManager);

        String expected = """
                john_doe (John Doe) <john@example.com>
                jane_smith (Jane Smith) <jane@example.com>
                """.replace("\n", System.lineSeparator());
        assertEquals(expected, report);
    }

    @Test
    @DisplayName("Should generate report for users with roles")
    void shouldGenerateUserReportWithRoles() {
        User user = new User("admin_user", "Admin User", "admin@example.com");
        when(userManager.findAll()).thenReturn(List.of(user));

        Role role = new Role("Admin", "full access");
        AbstractRoleAssignment assignment = new PermanentAssignment(user, role, AssignmentMetadata.now("test", "test"));
        when(assignmentManager.findByUser(user)).thenReturn(List.of(assignment));

        String report = ReportGenerator.generateUserReport(userManager, assignmentManager);

        String expected = "%s%n%s%n\t%s%n".formatted(user.format(), "Roles:", assignment.summary());
        assertEquals(expected, report);
    }

    @Test
    @DisplayName("Should handle multiple roles for a user")
    void shouldGenerateUserReportWithMultipleRoles() {
        User user = new User("power_user", "Power User", "power@example.com");
        when(userManager.findAll()).thenReturn(List.of(user));

        Role role1 = new Role("Editor", "can edit");
        Role role2 = new Role("Viewer", "can view");
        AbstractRoleAssignment assignment1 = new PermanentAssignment(user, role1, AssignmentMetadata.now("test", "test"));
        AbstractRoleAssignment assignment2 = new PermanentAssignment(user, role2, AssignmentMetadata.now("test", "test"));
        when(assignmentManager.findByUser(user)).thenReturn(List.of(assignment1, assignment2));

        String report = ReportGenerator.generateUserReport(userManager, assignmentManager);

        String expected = "%s%n%s%n\t%s%n\t%s%n"
                .formatted(user.format(), "Roles:", assignment1.summary(), assignment2.summary());
        assertEquals(expected, report);
    }

    // ==================== generateRoleReport ====================

    @Test
    @DisplayName("Should generate report for roles without assignments")
    void shouldGenerateRoleReportWithoutAssignments() {
        Role role1 = new Role("Admin", "administrator");
        Role role2 = new Role("User", "regular user");
        when(roleManager.findAll()).thenReturn(List.of(role1, role2));
        when(assignmentManager.findByRole(any(Role.class))).thenReturn(List.of());

        String report = ReportGenerator.generateRoleReport(roleManager, assignmentManager);

        String expected = role1.format() + "%nNumber of users with this role: 0%n".formatted() +
                role2.format() + "%nNumber of users with this role: 0%n".formatted();
        assertEquals(expected, report);
    }

    @Test
    @DisplayName("Should generate report for roles with assignments")
    void shouldGenerateRoleReportWithAssignments() {
        Role role = new Role("Admin", "administrator");
        when(roleManager.findAll()).thenReturn(List.of(role));

        when(assignmentManager.findByRole(role)).thenReturn(List.of(
                mock(RoleAssignment.class),
                mock(RoleAssignment.class)
        ));

        String report = ReportGenerator.generateRoleReport(roleManager, assignmentManager);

        String expected = role.format() + "%nNumber of users with this role: 2%n".formatted();
        assertEquals(expected, report);
    }

    // ==================== generatePermissionMatrix ====================

    @Test
    @DisplayName("Should generate permission matrix for users without assignments")
    void shouldGeneratePermissionMatrixNoPermissions() {
        User user = new User("guest", "Guest User", "guest@example.com");
        when(userManager.findAll()).thenReturn(List.of(user));
        when(assignmentManager.findByUser(user)).thenReturn(List.of());

        String matrix = ReportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        // Header
        String header = "username | permission%n";
        String row = "guest    | null%n";
        assertEquals((header + row).formatted(), matrix);
    }

    @Test
    @DisplayName("Should generate permission matrix with one user and one permission")
    void shouldGeneratePermissionMatrixSinglePermission() {
        User user = new User("editor", "Editor", "editor@example.com");
        when(userManager.findAll()).thenReturn(List.of(user));

        Permission permission = new Permission("EDIT", "users", "can edit users");
        Role role = mock(Role.class);
        when(role.getPermissions()).thenReturn(Set.of(permission));

        RoleAssignment assignment = mock(RoleAssignment.class);
        when(assignment.role()).thenReturn(role);
        when(assignmentManager.findByUser(user)).thenReturn(List.of(assignment));

        String matrix = ReportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        String header = "username | permission%n";
        String row = "editor   | " + permission.format() + "%n";
        assertEquals((header + row).formatted(), matrix);
    }

    @Test
    @DisplayName("Should generate permission matrix with multiple permissions per role")
    void shouldGeneratePermissionMatrixMultiplePermissions() {
        User user = new User("manager", "Manager", "manager@example.com");
        when(userManager.findAll()).thenReturn(List.of(user));

        Permission perm1 = new Permission("READ", "users", "can read users");
        Permission perm2 = new Permission("WRITE", "users", "can write users");
        Role role = mock(Role.class);
        when(role.getPermissions()).thenReturn(Set.of(perm1, perm2));

        RoleAssignment assignment = mock(RoleAssignment.class);
        when(assignment.role()).thenReturn(role);
        when(assignmentManager.findByUser(user)).thenReturn(List.of(assignment));

        String matrix = ReportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        String header = "username | permission%n".formatted();
        assertTrue(matrix.contains(header));
        assertTrue(matrix.contains("%-8s | %s%n".formatted(user.username(), perm1.format())));
        assertTrue(matrix.contains("%-8s | %s%n".formatted(user.username(), perm2.format())));
    }

    @Test
    @DisplayName("Should generate permission matrix with multiple users")
    void shouldGeneratePermissionMatrixMultipleUsers() {
        User user1 = new User("alice", "Alice", "alice@example.com");
        User user2 = new User("bob", "Bob", "bob@example.com");
        when(userManager.findAll()).thenReturn(List.of(user1, user2));

        when(assignmentManager.findByUser(user1)).thenReturn(List.of());

        Permission perm = new Permission("VIEW", "users", "View permission");
        Role role = mock(Role.class);
        when(role.getPermissions()).thenReturn(Set.of(perm));
        RoleAssignment assignment = mock(RoleAssignment.class);
        when(assignment.role()).thenReturn(role);
        when(assignmentManager.findByUser(user2)).thenReturn(List.of(assignment));

        String matrix = ReportGenerator.generatePermissionMatrix(userManager, assignmentManager);

        String header = "username | permission";
        String expected = "%s%n%-8s | %s%n%-8s | %s%n"
                .formatted(header, user1.username(), "null", user2.username(), perm.format());
        assertEquals(expected, matrix);
    }

    // ==================== exportToFile ====================

    @Test
    @DisplayName("Should export report to file")
    void shouldExportToFile(@TempDir Path tempDir) {
        Path file = tempDir.resolve("report.txt");
        String report = "Test report content";

        ReportGenerator.exportToFile(report, file.toString());

        assertTrue(Files.exists(file));
        try {
            String content = Files.readString(file);
            assertEquals(report, content);
        } catch (IOException e) {
            fail("Failed to read file", e);
        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Should throw IllegalArgumentException when report is null or blank")
    void shouldThrowWhenReportInvalid(String report) {
        assertThrows(IllegalArgumentException.class,
                () -> ReportGenerator.exportToFile(report, "filename.txt"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Should throw IllegalArgumentException when filename is null or blank")
    void shouldThrowWhenFilenameInvalid(String filename) {
        assertThrows(IllegalArgumentException.class,
                () -> ReportGenerator.exportToFile("report content", filename));
    }

    @Test
    @DisplayName("Should throw RuntimeException when writing to file fails")
    void shouldThrowOnWriteFailure() {
        String invalidPath = "/nonexistent/directory/report.txt";
        assertThrows(RuntimeException.class,
                () -> ReportGenerator.exportToFile("content", invalidPath));
    }
}