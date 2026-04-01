package io.github.speranskyartyom.rbac.cli.commands;

import io.github.speranskyartyom.rbac.cli.CommandParser;
import io.github.speranskyartyom.rbac.core.RBACSystem;
import io.github.speranskyartyom.rbac.managers.AssignmentManager;
import io.github.speranskyartyom.rbac.managers.RoleManager;
import io.github.speranskyartyom.rbac.managers.UserManager;
import io.github.speranskyartyom.rbac.reports.ReportGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ReportCommandsTest {

    @TempDir
    Path tempDir;
    private RBACSystem mockSystem;
    private UserManager mockUserManager;
    private RoleManager mockRoleManager;
    private AssignmentManager mockAssignmentManager;
    private CommandParser parser;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        mockUserManager = mock(UserManager.class);
        mockRoleManager = mock(RoleManager.class);
        mockAssignmentManager = mock(AssignmentManager.class);
        mockSystem = mock(RBACSystem.class);
        when(mockSystem.getUserManager()).thenReturn(mockUserManager);
        when(mockSystem.getRoleManager()).thenReturn(mockRoleManager);
        when(mockSystem.getAssignmentManager()).thenReturn(mockAssignmentManager);

        parser = new CommandParser();
        ReportCommands.registerCommands(parser);

        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ========== report-users tests ==========

    @Test
    @DisplayName("report-users with blank input prints report")
    void reportUsersPrint() {
        String report = "User report content";
        try (MockedStatic<ReportGenerator> mocked = mockStatic(ReportGenerator.class)) {
            mocked.when(() -> ReportGenerator.generateUserReport(mockUserManager, mockAssignmentManager))
                    .thenReturn(report);
            mocked.when(() -> ReportGenerator.exportToFile(anyString(), anyString()))
                    .thenAnswer(_ -> {
                        throw new RuntimeException("should not be called");
                    });

            String input = "\n";
            Scanner scanner = new Scanner(input);
            parser.parseAndExecute("report-users", scanner, mockSystem);

            String output = outContent.toString();
            assertTrue(output.contains(report));
            mocked.verify(() -> ReportGenerator.exportToFile(any(), any()), never());
        }
    }

    @Test
    @DisplayName("report-users with filename input saves to file")
    void reportUsersSaveToFile() {
        String report = "User report content";
        String filename = tempDir.resolve("users.txt").toString();
        try (MockedStatic<ReportGenerator> mocked = mockStatic(ReportGenerator.class)) {
            mocked.when(() -> ReportGenerator.generateUserReport(mockUserManager, mockAssignmentManager))
                    .thenReturn(report);
            mocked.when(() -> ReportGenerator.exportToFile(eq(report), eq(filename)))
                    .thenAnswer(_ -> null);

            String input = filename + "\n";
            Scanner scanner = new Scanner(input);
            parser.parseAndExecute("report-users", scanner, mockSystem);

            mocked.verify(() -> ReportGenerator.exportToFile(report, filename));
            String output = outContent.toString();
            assertTrue(output.contains("Enter filename"));
            assertFalse(output.contains(report));
        }
    }

    @Test
    @DisplayName("report-users with --file argument saves to file")
    void reportUsersWithFileArg() {
        String report = "User report content";
        String filename = tempDir.resolve("users.txt").toString();
        try (MockedStatic<ReportGenerator> mocked = mockStatic(ReportGenerator.class)) {
            mocked.when(() -> ReportGenerator.generateUserReport(mockUserManager, mockAssignmentManager))
                    .thenReturn(report);
            mocked.when(() -> ReportGenerator.exportToFile(eq(report), eq(filename)))
                    .thenAnswer(_ -> null);

            parser.parseAndExecute("report-users --file " + filename, new Scanner(""), mockSystem);

            mocked.verify(() -> ReportGenerator.exportToFile(report, filename));
            String output = outContent.toString();
            assertEquals(output, "Report saved to file - " + filename + System.lineSeparator());
        }
    }

    @Test
    @DisplayName("report-users with -f short argument saves to file")
    void reportUsersWithShortFileArg() {
        String report = "User report content";
        String filename = tempDir.resolve("users.txt").toString();
        try (MockedStatic<ReportGenerator> mocked = mockStatic(ReportGenerator.class)) {
            mocked.when(() -> ReportGenerator.generateUserReport(mockUserManager, mockAssignmentManager))
                    .thenReturn(report);
            mocked.when(() -> ReportGenerator.exportToFile(eq(report), eq(filename)))
                    .thenAnswer(_ -> null);

            parser.parseAndExecute("report-users -f " + filename, new Scanner(""), mockSystem);

            mocked.verify(() -> ReportGenerator.exportToFile(report, filename));
        }
    }

    @Test
    @DisplayName("report-users with extra arguments after filename warns")
    void reportUsersExtraArgs() {
        String report = "User report content";
        String filename = tempDir.resolve("users.txt").toString();
        try (MockedStatic<ReportGenerator> mocked = mockStatic(ReportGenerator.class)) {
            mocked.when(() -> ReportGenerator.generateUserReport(mockUserManager, mockAssignmentManager))
                    .thenReturn(report);
            mocked.when(() -> ReportGenerator.exportToFile(eq(report), eq(filename)))
                    .thenAnswer(_ -> null);

            parser.parseAndExecute("report-users --file " + filename + " extra stuff", new Scanner(""), mockSystem);

            mocked.verify(() -> ReportGenerator.exportToFile(report, filename));
            String output = outContent.toString();
            assertTrue(output.contains("Warning: extra arguments ignored: extra stuff"));
        }
    }

    @Test
    @DisplayName("report-users with invalid file path prints error")
    void reportUsersInvalidPath() {
        String report = "User report content";
        String invalidPath = "/nonexistent/dir/users.txt";
        try (MockedStatic<ReportGenerator> mocked = mockStatic(ReportGenerator.class)) {
            mocked.when(() -> ReportGenerator.generateUserReport(mockUserManager, mockAssignmentManager))
                    .thenReturn(report);
            mocked.when(() -> ReportGenerator.exportToFile(eq(report), eq(invalidPath)))
                    .thenThrow(new RuntimeException("Failed to save report"));

            parser.parseAndExecute("report-users --file " + invalidPath, new Scanner(""), mockSystem);

            String output = outContent.toString();
            assertTrue(output.contains("Failed to save report"));
        }
    }

    @Test
    @DisplayName("report-users with unexpected token prints error")
    void reportUsersUnexpectedToken() {
        parser.parseAndExecute("report-users --invalid", new Scanner(""), mockSystem);
        String output = outContent.toString();
        assertTrue(output.contains("Error! Unexpected token: --invalid"));
        assertTrue(output.contains("Expected --file/-f <filename>"));
    }

    // ========== report-roles tests ==========

    @Test
    @DisplayName("report-roles with blank input prints report")
    void reportRolesPrint() {
        String report = "Roles report content";
        try (MockedStatic<ReportGenerator> mocked = mockStatic(ReportGenerator.class)) {
            mocked.when(() -> ReportGenerator.generateRoleReport(mockRoleManager, mockAssignmentManager))
                    .thenReturn(report);
            mocked.when(() -> ReportGenerator.exportToFile(anyString(), anyString()))
                    .thenAnswer(_ -> {
                        throw new RuntimeException("should not be called");
                    });

            String input = "\n";
            Scanner scanner = new Scanner(input);
            parser.parseAndExecute("report-roles", scanner, mockSystem);

            String output = outContent.toString();
            assertTrue(output.contains(report));
            mocked.verify(() -> ReportGenerator.exportToFile(any(), any()), never());
        }
    }

    @Test
    @DisplayName("report-roles with --file argument saves to file")
    void reportRolesWithFileArg() {
        String report = "Roles report content";
        String filename = tempDir.resolve("roles.txt").toString();
        try (MockedStatic<ReportGenerator> mocked = mockStatic(ReportGenerator.class)) {
            mocked.when(() -> ReportGenerator.generateRoleReport(mockRoleManager, mockAssignmentManager))
                    .thenReturn(report);
            mocked.when(() -> ReportGenerator.exportToFile(eq(report), eq(filename)))
                    .thenAnswer(_ -> null);

            parser.parseAndExecute("report-roles -f " + filename, new Scanner(""), mockSystem);

            mocked.verify(() -> ReportGenerator.exportToFile(report, filename));
        }
    }

    // ========== report-matrix tests ==========

    @Test
    @DisplayName("report-matrix with blank input prints report")
    void reportMatrixPrint() {
        String report = "Permission matrix content";
        try (MockedStatic<ReportGenerator> mocked = mockStatic(ReportGenerator.class)) {
            mocked.when(() -> ReportGenerator.generatePermissionMatrix(mockUserManager, mockAssignmentManager))
                    .thenReturn(report);
            mocked.when(() -> ReportGenerator.exportToFile(anyString(), anyString()))
                    .thenAnswer(_ -> {
                        throw new RuntimeException("should not be called");
                    });

            String input = "\n";
            Scanner scanner = new Scanner(input);
            parser.parseAndExecute("report-matrix", scanner, mockSystem);

            String output = outContent.toString();
            assertTrue(output.contains(report));
            mocked.verify(() -> ReportGenerator.exportToFile(any(), any()), never());
        }
    }

    @Test
    @DisplayName("report-matrix with --file argument saves to file")
    void reportMatrixWithFileArg() {
        String report = "Permission matrix content";
        String filename = tempDir.resolve("matrix.txt").toString();
        try (MockedStatic<ReportGenerator> mocked = mockStatic(ReportGenerator.class)) {
            mocked.when(() -> ReportGenerator.generatePermissionMatrix(mockUserManager, mockAssignmentManager))
                    .thenReturn(report);
            mocked.when(() -> ReportGenerator.exportToFile(eq(report), eq(filename)))
                    .thenAnswer(_ -> null);

            parser.parseAndExecute("report-matrix -f " + filename, new Scanner(""), mockSystem);

            mocked.verify(() -> ReportGenerator.exportToFile(report, filename));
        }
    }

    // ========== Common behavior ==========

    @Test
    @DisplayName("report-users should use correct managers")
    void reportUsersUsesCorrectManagers() {
        try (MockedStatic<ReportGenerator> mocked = mockStatic(ReportGenerator.class)) {
            mocked.when(() -> ReportGenerator.generateUserReport(mockUserManager, mockAssignmentManager))
                    .thenReturn("report");
            mocked.when(() -> ReportGenerator.exportToFile(any(), any()))
                    .thenAnswer(_ -> null);

            parser.parseAndExecute("report-users --file test.txt", new Scanner(""), mockSystem);

            mocked.verify(() -> ReportGenerator.generateUserReport(mockUserManager, mockAssignmentManager));
        }
    }

    @Test
    @DisplayName("report-roles should use correct managers")
    void reportRolesUsesCorrectManagers() {
        try (MockedStatic<ReportGenerator> mocked = mockStatic(ReportGenerator.class)) {
            mocked.when(() -> ReportGenerator.generateRoleReport(mockRoleManager, mockAssignmentManager))
                    .thenReturn("report");
            mocked.when(() -> ReportGenerator.exportToFile(any(), any()))
                    .thenAnswer(_ -> null);

            parser.parseAndExecute("report-roles --file test.txt", new Scanner(""), mockSystem);

            mocked.verify(() -> ReportGenerator.generateRoleReport(mockRoleManager, mockAssignmentManager));
        }
    }

    @Test
    @DisplayName("report-matrix should use correct managers")
    void reportMatrixUsesCorrectManagers() {
        try (MockedStatic<ReportGenerator> mocked = mockStatic(ReportGenerator.class)) {
            mocked.when(() -> ReportGenerator.generatePermissionMatrix(mockUserManager, mockAssignmentManager))
                    .thenReturn("report");
            mocked.when(() -> ReportGenerator.exportToFile(any(), any()))
                    .thenAnswer(_ -> null);

            parser.parseAndExecute("report-matrix --file test.txt", new Scanner(""), mockSystem);

            mocked.verify(() -> ReportGenerator.generatePermissionMatrix(mockUserManager, mockAssignmentManager));
        }
    }
}