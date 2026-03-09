package io.github.speranskyartyom.rbac.cli;

import io.github.speranskyartyom.rbac.core.RBACSystem;
import io.github.speranskyartyom.rbac.interfaces.functional.Command;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandParser {
    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, String> commandDescriptions = new HashMap<>();

    public void registerCommand(String name, String description, Command command) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Command name can not be null or blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description can not be null or blank");
        }
        if (command == null) {
            throw new IllegalArgumentException("Command can not be null");
        }

        commands.put(name, command);
        commandDescriptions.put(name, description);
    }

    private void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        commands.get(commandName).execute(scanner, system);
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.isBlank()) return;

        String[] parts = input.trim().split("\\s+", 2);
        String commandName = parts[0];
        if (!commands.containsKey(commandName)) {
            System.out.println("Command: '" + commandName + "' not found\n" +
                    "Use 'help' to see command list");
            return;
        }

        if (parts.length == 2) {
            try (Scanner argsScanner = new Scanner(parts[1])) {
                executeCommand(commandName, argsScanner, system);
            }
        } else {
            executeCommand(commandName, scanner, system);
        }
    }

    public void printHelp() {
        System.out.println("Commands:");
        commandDescriptions.forEach((command, description) ->
                System.out.println(command + "\t" + description));
    }
}
