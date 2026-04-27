package io.github.speranskyartyom.rbac.cli;

import io.github.speranskyartyom.rbac.core.RBACSystem;
import io.github.speranskyartyom.rbac.interfaces.functional.Command;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandParserTest {

    private CommandParser parser;
    private RBACSystem mockSystem;
    private ByteArrayOutputStream outContent;
    private PrintStream originalOut;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        mockSystem = mock(RBACSystem.class);
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Should register valid command")
    void shouldRegisterCommand() {
        Command mockCommand = mock(Command.class);
        parser.registerCommand("test", "Test command", mockCommand);

        parser.printHelp();
        String output = outContent.toString();
        assertTrue(output.contains("test"));
        assertTrue(output.contains("Test command"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Should throw exception when registering with invalid name")
    void shouldThrowWhenNameInvalid(String invalidName) {
        Command mockCommand = mock(Command.class);
        assertThrows(IllegalArgumentException.class,
                () -> parser.registerCommand(invalidName, "desc", mockCommand));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Should throw exception when registering with invalid description")
    void shouldThrowWhenDescriptionInvalid(String invalidDescription) {
        Command mockCommand = mock(Command.class);
        assertThrows(IllegalArgumentException.class,
                () -> parser.registerCommand("test", invalidDescription, mockCommand));
    }

    @Test
    @DisplayName("Should throw exception when registering null command")
    void shouldThrowWhenCommandNull() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.registerCommand("test", "desc", null));
    }

    @Test
    @DisplayName("Should ignore null or blank input")
    void shouldIgnoreBlankInput() {
        Command mockCommand = mock(Command.class);
        parser.registerCommand("test", "desc", mockCommand);

        parser.parseAndExecute(null, new Scanner(System.in), mockSystem);
        parser.parseAndExecute("", new Scanner(System.in), mockSystem);
        parser.parseAndExecute("   ", new Scanner(System.in), mockSystem);

        verifyNoInteractions(mockCommand);
    }

    @Test
    @DisplayName("Should print error for unknown command and not execute")
    void shouldHandleUnknownCommand() {
        Command mockCommand = mock(Command.class);
        parser.registerCommand("known", "desc", mockCommand);

        parser.parseAndExecute("unknown arg1 arg2", new Scanner(System.in), mockSystem);

        String output = outContent.toString();
        assertTrue(output.contains("Command: 'unknown' not found"));
        assertTrue(output.contains("Use 'help' to see command list"));
        verifyNoInteractions(mockCommand);
    }

    @Test
    @DisplayName("Should execute command without arguments")
    void shouldExecuteCommandWithoutArgs() {
        Command mockCommand = mock(Command.class);
        parser.registerCommand("test", "desc", mockCommand);
        Scanner originalScanner = new Scanner(System.in);

        parser.parseAndExecute("test", originalScanner, mockSystem);

        verify(mockCommand, times(1)).execute(originalScanner, mockSystem, new String[0]);
    }

    @Test
    @DisplayName("Should execute command with arguments")
    void shouldExecuteCommandWithArgs() {
        Command testCommand = (_, _, args) -> {
            assertEquals("arg1", args[0]);
            assertEquals("arg2", args[1]);
            assertEquals(2, args.length);
        };
        parser.registerCommand("test", "desc", testCommand);
        Scanner scanner = new Scanner(System.in);

        parser.parseAndExecute("test arg1 arg2", scanner, mockSystem);
    }

    @Test
    @DisplayName("printHelp should list all registered commands")
    void printHelpShouldShowCommands() {
        parser.registerCommand("cmd1", "Description 1", mock(Command.class));
        parser.registerCommand("cmd2", "Description 2", mock(Command.class));

        parser.printHelp();

        String output = outContent.toString();
        assertTrue(output.contains("cmd1"));
        assertTrue(output.contains("Description 1"));
        assertTrue(output.contains("cmd2"));
        assertTrue(output.contains("Description 2"));
    }
}