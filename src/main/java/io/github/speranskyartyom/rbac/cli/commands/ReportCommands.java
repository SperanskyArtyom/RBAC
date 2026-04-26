package io.github.speranskyartyom.rbac.cli.commands;

import io.github.speranskyartyom.rbac.cli.CommandParser;
import io.github.speranskyartyom.rbac.interfaces.functional.Command;
import io.github.speranskyartyom.rbac.reports.ReportGenerator;
import io.github.speranskyartyom.rbac.utils.ConsoleUtils;

import java.util.Arrays;

public class ReportCommands {
    public static void registerCommands(CommandParser parser) {
        parser.registerCommand(
                "report-users",
                "generate users report and print it on screen " +
                        "or save to file (--file/-f <filename>).",
                reportUsersCommand()
        );
        parser.registerCommand(
                "report-roles",
                "generate roles report and print it on screen " +
                        "or save to file (--file/-f <filename>).",
                reportRolesCommand()
        );
        parser.registerCommand(
                "report-matrix",
                "generate users permissions matrix and print it on screen " +
                        "or save to file (--file/-f <filename>).",
                reportMatrixCommand()
        );
    }

    private static Command printOrSaveReport(String report) {
        return (scanner, system, args) -> {
            String filename = null;

            if (args.length > 0) {
                if (args[0].equals("--file") || args[0].equals("-f")) {
                    if (args.length > 1) {
                        filename = args[1];

                        if (args.length > 2) {
                            String extra = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                            System.out.println("Warning: extra arguments ignored: " + extra);
                        }
                    }

                } else {
                    System.out.println("Error! Unexpected token: " + args[0]);
                    System.out.println("Expected --file/-f <filename>");
                    return;
                }
            }

            if (filename == null) {
                filename = ConsoleUtils.promptString(
                        scanner,
                        "Enter filename to save report " +
                                "or type \"Enter\" to print it on screen",
                        false
                );
            }

            if (filename.isBlank()) {
                System.out.println(report);
            } else {
                try {
                    ReportGenerator.exportToFile(report, filename);
                    System.out.println("Report saved to file - " + filename);
                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());
                }
            }
        };
    }

    private static Command reportUsersCommand() {
        return (scanner, system, args) -> {
            String report = ReportGenerator.generateUserReport(
                    system.getUserManager(),
                    system.getAssignmentManager()
            );

            printOrSaveReport(report).execute(scanner, system, args);
        };
    }

    private static Command reportRolesCommand() {
        return (scanner, system, args) -> {
            String report = ReportGenerator.generateRoleReport(
                    system.getRoleManager(),
                    system.getAssignmentManager()
            );

            printOrSaveReport(report).execute(scanner, system, args);
        };
    }

    private static Command reportMatrixCommand() {
        return (scanner, system, args) -> {
            String report = ReportGenerator.generatePermissionMatrix(
                    system.getUserManager(),
                    system.getAssignmentManager()
            );

            printOrSaveReport(report).execute(scanner, system, args);
        };
    }
}
