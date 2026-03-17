package io.github.speranskyartyom.rbac.cli.commands;

import io.github.speranskyartyom.rbac.cli.CommandParser;
import io.github.speranskyartyom.rbac.core.RBACSystem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class UtilityCommandsTest {

    private RBACSystem mockSystem;
    private CommandParser parser;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        mockSystem = mock(RBACSystem.class);
        parser = new CommandParser();
        UtilityCommands.registerCommands(parser);

        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ========== help command tests ==========

    @Test
    @DisplayName("help command should print help message")
    void helpCommand() {
        parser.parseAndExecute("help", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("help"));
        assertTrue(output.contains("Show this help message"));
        assertTrue(output.contains("stats"));
        assertTrue(output.contains("Display system statistics"));
        assertTrue(output.contains("clear"));
        assertTrue(output.contains("Clear the console screen"));
        assertTrue(output.contains("exit"));
        assertTrue(output.contains("Exit the program"));
    }

    // ========== stats command tests ==========

    @Test
    @DisplayName("stats command should print system statistics")
    void statsCommand() {
        String statsString = """
                System statistics
                    Users: 5
                    Roles: 3
                    Assignments: TOTAL 10, ACTIVE 7, EXPIRED 3
                    Average number of roles per user: 1.40
                    Top roles: Admin, Viewer, Manager
                """;
        when(mockSystem.generateStatistics()).thenReturn(statsString);

        parser.parseAndExecute("stats", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains(statsString));
        verify(mockSystem, times(1)).generateStatistics();
    }

    @Test
    @DisplayName("stats command with extra arguments should ignore them")
    void statsCommandWithExtraArgs() {
        String statsString = "dummy stats";
        when(mockSystem.generateStatistics()).thenReturn(statsString);

        parser.parseAndExecute("stats extra args", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains(statsString));
        verify(mockSystem, times(1)).generateStatistics();
    }

    // ========== clear command tests ==========

    @Test
    @DisplayName("clear command should print ANSI escape codes")
    void clearCommand() {
        parser.parseAndExecute("clear", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertEquals("\033[H\033[2J", output);
    }

    @Test
    @DisplayName("clear command with extra arguments should ignore them")
    void clearCommandWithExtraArgs() {
        parser.parseAndExecute("clear extra stuff", new Scanner(""), mockSystem);

        String output = outContent.toString();
        assertEquals("\033[H\033[2J", output);
    }

    // ========== exit command tests ==========

    @Test
    @DisplayName("exit command with negative answer should not exit")
    void exitCommandNoFlagNegativeAnswer() {
        String input = "n\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("exit", scanner, mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Are you sure you want to exit (y/n): "));
        assertTrue(output.contains("Exit cancelled."));
    }

    @Test
    @DisplayName("exit command with unknown arguments should print warnings")
    void exitCommandWithUnknownArgs() {
        String input = "n\n";
        Scanner scanner = new Scanner(input);

        parser.parseAndExecute("exit foo bar", scanner, mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Warning: unknown argument ignored: foo"));
        assertTrue(output.contains("Warning: unknown argument ignored: bar"));
    }
}