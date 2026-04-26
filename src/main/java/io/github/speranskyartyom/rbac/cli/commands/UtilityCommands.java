package io.github.speranskyartyom.rbac.cli.commands;

import io.github.speranskyartyom.rbac.cli.CommandParser;
import io.github.speranskyartyom.rbac.interfaces.functional.Command;
import io.github.speranskyartyom.rbac.utils.ConsoleUtils;

public class UtilityCommands {
    public static void registerCommands(CommandParser parser) {
        parser.registerCommand(
                "help",
                "Show this help message with all available commands.",
                helpCommand(parser)
        );
        parser.registerCommand(
                "stats",
                "Display system statistics: users, roles, assignments, " +
                        "average roles per user, top roles.",
                statsCommand()
        );
        parser.registerCommand(
                "clear",
                "Clear the console screen.",
                clearCommand()
        );
        parser.registerCommand(
                "exit",
                "Exit the program. Use -y or to skip confirmation.",
                exitCommand()
        );
        parser.registerCommand(
                "audit-log",
                "Print logs",
                auditLogCommand()
        );
    }

    private static Command helpCommand(CommandParser parser) {
        return (_, _, _) -> parser.printHelp();
    }

    private static Command statsCommand() {
        return (_, system, _) -> System.out.println(system.generateStatistics());
    }

    private static Command clearCommand() {
        return (_, _, _) -> {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        };
    }

    private static Command exitCommand() {
        return (scanner, _, args) -> {
            boolean forced = false;
            for (String arg : args) {
                if (arg.equals("-y")) {
                    forced = true;
                } else {
                    System.out.println("Warning: unknown argument ignored: " + arg);
                }
            }

            if (!forced) {
                if (!ConsoleUtils.promptYesNo(scanner, "Are you sure you want to exit?")) {
                    System.out.println("Exit cancelled.");
                    return;
                }
            }

            //TODO: offer to save data

            System.exit(0);
        };
    }

    private static Command auditLogCommand() {
        return (_, system, _) -> system.getLogger().printLog();
    }

    //TODO: implement saveCommand()
    //TODO: implement loadCommand()
}
