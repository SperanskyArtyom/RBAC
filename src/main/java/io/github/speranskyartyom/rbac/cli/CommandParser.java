package io.github.speranskyartyom.rbac.cli;

import io.github.speranskyartyom.rbac.core.RBACSystem;
import io.github.speranskyartyom.rbac.interfaces.functional.Command;
import io.github.speranskyartyom.rbac.utils.FormatUtils;
import io.github.speranskyartyom.rbac.utils.ValidationUtils;

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
        ValidationUtils.requireNonEmpty(name, "name");
        ValidationUtils.requireNonEmpty(description, "description");
        if (command == null) {
            throw new IllegalArgumentException("command can not be null");
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
        String[] headers = {"Command", "Description"};
        List<String[]> rows = new ArrayList<>();

        commandDescriptions.forEach((command, description) ->
                rows.add(new String[]{command, description}));

        System.out.println(FormatUtils.formatTable(headers, rows));
    }
}
