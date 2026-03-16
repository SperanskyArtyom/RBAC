package io.github.speranskyartyom.rbac.cli;

import io.github.speranskyartyom.rbac.core.RBACSystem;
import io.github.speranskyartyom.rbac.interfaces.functional.Command;

import java.util.*;

public class CommandParser {
    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, String> commandDescriptions = new HashMap<>();

    private static String getArg(Scanner argLine) {
        String token = argLine.next();
        if (token.startsWith("\"")) {
            StringBuilder sb = new StringBuilder(token);
            if (token.length() == 1) {
                if (!argLine.hasNext()) {
                    throw new RuntimeException("Closing quote missed: " + sb);
                }
                token = argLine.next();
            }
            while (!token.endsWith("\"")) {
                if (!argLine.hasNext()) {
                    throw new RuntimeException("Closing quote missed: " + sb);
                }
                token = argLine.next();
                sb.append(" ").append(token);
            }
            token = sb.substring(1, sb.length() - 1);
        }
        return token;
    }

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

    private void executeCommand(String commandName, Scanner scanner, RBACSystem system, String[] args) {
        commands.get(commandName).execute(scanner, system, args);
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

        List<String> argList = new ArrayList<>();
        if (parts.length == 2) {
            Scanner argsParser = new Scanner(parts[1]);
            while (argsParser.hasNext()) {
                try {
                    argList.add(getArg(argsParser));
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    return;
                }
            }
        }
        String[] args = argList.toArray(new String[0]);

        executeCommand(commandName, scanner, system, args);
    }

    public void printHelp() {
        int maxLength = 0;

        for (var entry : commands.entrySet()) {
            maxLength = Math.max(maxLength, entry.getKey().length());
        }

        String format = "%-" + maxLength + "s - %s%n";

        System.out.println("Commands:");
        commandDescriptions.forEach((command, description) ->
                System.out.printf(format, command, description));
    }
}
