package io.github.speranskyartyom.rbac.cli;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandRegistryTest {

    private CommandRegistry registry;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        registry = new CommandRegistry();
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Constructor should create non-null parser")
    void constructorCreatesParser() {
        assertNotNull(registry.getParser());
    }

    @Test
    @DisplayName("Registry should register all user commands")
    void userCommandsRegistered() {
        registry.getParser().printHelp();
        String output = outContent.toString();
        assertTrue(output.contains("user-list"));
        assertTrue(output.contains("user-create"));
        assertTrue(output.contains("user-view"));
        assertTrue(output.contains("user-update"));
        assertTrue(output.contains("user-delete"));
        assertTrue(output.contains("user-search"));
    }

    @Test
    @DisplayName("Registry should register all role commands")
    void roleCommandsRegistered() {
        registry.getParser().printHelp();
        String output = outContent.toString();
        assertTrue(output.contains("role-list"));
        assertTrue(output.contains("role-create"));
        assertTrue(output.contains("role-view"));
        assertTrue(output.contains("role-update"));
        assertTrue(output.contains("role-delete"));
        assertTrue(output.contains("role-add-permission"));
        assertTrue(output.contains("role-remove-permission"));
        assertTrue(output.contains("role-search"));
    }

    @Test
    @DisplayName("Registry should register all assignment commands")
    void assignmentCommandsRegistered() {
        registry.getParser().printHelp();
        String output = outContent.toString();
        assertTrue(output.contains("assign-role"));
        assertTrue(output.contains("revoke-role"));
        assertTrue(output.contains("assignment-list"));
        assertTrue(output.contains("assignment-list-user"));
        assertTrue(output.contains("assignment-list-role"));
        assertTrue(output.contains("assignment-active"));
        assertTrue(output.contains("assignment-expired"));
        assertTrue(output.contains("assignment-extend"));
        assertTrue(output.contains("assignment-search"));
    }

    @Test
    @DisplayName("Registry should register all permission commands")
    void permissionCommandsRegistered() {
        registry.getParser().printHelp();
        String output = outContent.toString();
        assertTrue(output.contains("permissions-user"));
        assertTrue(output.contains("permissions-check"));
    }

    @Test
    @DisplayName("Registry should register all utility commands")
    void utilityCommandsRegistered() {
        registry.getParser().printHelp();
        String output = outContent.toString();
        assertTrue(output.contains("help"));
        assertTrue(output.contains("stats"));
        assertTrue(output.contains("clear"));
        assertTrue(output.contains("exit"));
    }

    @Test
    @DisplayName("Registry should register all report commands")
    void reportCommandsRegistered() {
        registry.getParser().printHelp();
        String output = outContent.toString();
        assertTrue(output.contains("report-users"));
        assertTrue(output.contains("report-roles"));
        assertTrue(output.contains("report-matrix"));
    }
}