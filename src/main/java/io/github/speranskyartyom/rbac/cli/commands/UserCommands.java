package io.github.speranskyartyom.rbac.cli.commands;

import io.github.speranskyartyom.rbac.cli.CommandParser;
import io.github.speranskyartyom.rbac.filters.UserFilters;
import io.github.speranskyartyom.rbac.interfaces.RoleAssignment;
import io.github.speranskyartyom.rbac.interfaces.functional.Command;
import io.github.speranskyartyom.rbac.interfaces.functional.UserFilter;
import io.github.speranskyartyom.rbac.managers.UserManager;
import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.records.User;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class UserCommands {
    public static void registerUserCommands(CommandParser parser) {
        parser.registerCommand(
                "user-list",
                "List all users. Optional filters: username, email, fullname, domain (key=value format).",
                userListCommand()
        );
        parser.registerCommand(
                "user-create",
                "Create new user with input username, full name and email.",
                userCreateCommand()
        );
        parser.registerCommand(
                "user-view",
                "View user info (user, roles, permissions).",
                userViewCommand()
        );
        parser.registerCommand(
                "user-update",
                "Update user's full name and email.",
                userUpdateCommand()
        );
        parser.registerCommand(
                "user-delete",
                "Delete user by username.",
                userDeleteCommand()
        );
        parser.registerCommand(
                "user-search",
                "Search users by filters.",
                userSearchCommand()
        );
    }

    private static void printUserTable(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }

        int maxUser = "Username".length();
        int maxFull = "Full Name".length();
        int maxEmail = "Email".length();

        for (User user : users) {
            maxUser = Math.max(maxUser, user.username().length());
            maxFull = Math.max(maxFull, user.fullName().length());
            maxEmail = Math.max(maxEmail, user.email().length());
        }

        String format = "| %-" + maxUser + "s | %-" + maxFull + "s | %-" + maxEmail + "s |%n";
        String separator = "+-" + "-".repeat(maxUser) + "-+-" + "-".repeat(maxFull) + "-+-" + "-".repeat(maxEmail) + "-+";

        System.out.println(separator);
        System.out.printf(format, "Username", "Full Name", "Email");
        System.out.println(separator);

        for (User u : users) {
            System.out.printf(format, u.username(), u.fullName(), u.email());
        }

        System.out.println(separator);
    }

    private static Command userListCommand() {
        return ((scanner, system, haveArgs) -> {
            UserManager manager = system.getUserManager();
            List<User> users;

            if (!haveArgs) {
                users = manager.findAll();
                printUserTable(users);
                return;
            }

            UserFilter filter = null;

            while (scanner.hasNext()) {
                String arg = scanner.next();
                String[] tokens = arg.split("=", 2);
                if (tokens.length != 2) {
                    System.out.println("Invalid filter format: " + arg + ". Expected key=value");
                    return;
                }

                String key = tokens[0].toLowerCase();
                String value = tokens[1];
                UserFilter currentFilter;

                switch (key) {
                    case "username" -> currentFilter = UserFilters.byUsernameContains(value);
                    case "email" -> currentFilter = UserFilters.byEmailContains(value);
                    case "domain" -> currentFilter = UserFilters.byEmailDomain(value);
                    case "fullname" -> currentFilter = UserFilters.byFullNameContains(value);
                    default -> {
                        System.out.println("Unknown filter: " + key + ".\n" +
                                "Available filters: " +
                                "username (contains), " +
                                "email (contains), " +
                                "fullname (contains), " +
                                "domain (exact match)");
                        return;
                    }
                }
                filter = filter == null ? currentFilter : filter.and(currentFilter);
            }
            if (filter == null) {
                users = manager.findAll();
                printUserTable(users);
                return;
            }
            users = manager.findByFilter(filter);
            printUserTable(users);
        });
    }

    private static Command userCreateCommand() {
        return ((scanner, system, haveArgs) -> {
            String username, fullName, email;

            if (!haveArgs) {
                System.out.print("Enter username: ");
                username = scanner.nextLine();
                System.out.print("Enter full name: ");
                fullName = scanner.nextLine();
                System.out.print("Enter email: ");
                email = scanner.nextLine();
            } else {
                if (!scanner.hasNext()) {
                    System.out.println("Error: missing username, full name and email");
                    return;
                }
                username = scanner.next();
                if (!scanner.hasNextLine()) {
                    System.out.println("Error: missing fullName and email");
                    return;
                }
                String rest = scanner.nextLine().trim();

                if (rest.startsWith("\"")) {
                    int closingQuote = rest.indexOf("\"", 1);
                    if (closingQuote == -1) {
                        System.out.println("Error: missing closing quote for fullName");
                        return;
                    }
                    fullName = rest.substring(1, closingQuote);

                    scanner = new Scanner(rest.substring(closingQuote + 1).trim());
                } else {
                    scanner = new Scanner(rest);
                    fullName = scanner.next();
                }

                if (!scanner.hasNext()) {
                    System.out.println("Error: missing email");
                    return;
                }
                email = scanner.next();

                if (scanner.hasNext()) {
                    String extra = scanner.nextLine();
                    System.out.println("Warning: extra arguments ignored: " + extra);
                }
            }

            try {
                system.getUserManager().add(new User(username, fullName, email));
                System.out.println("User created successfully.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }

    private static Command userViewCommand() {
        return ((scanner, system, haveArgs) -> {
            if (!haveArgs) {
                System.out.print("Enter username: ");
            }
            String username = scanner.next();
            if (haveArgs && scanner.hasNext()) {
                System.out.println("Warning: extra arguments ignored: " + scanner.nextLine());
            }

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("No user with username - " + username);
                return;
            }

            User user = userOpt.get();
            Set<Role> roles = system.getAssignmentManager().findByUser(user).stream()
                    .map(RoleAssignment::role)
                    .collect(Collectors.toSet());

            System.out.println(user.format());
            if (roles.isEmpty()) {
                System.out.println("User don't have any role or permission.");
            } else {
                for (var role : roles) {
                    System.out.println(role.format());
                }
            }
        });
    }

    private static Command userUpdateCommand() {
        return ((scanner, system, haveArgs) -> {
            String username, fullName, email;

            if (!haveArgs) {
                System.out.print("Enter username to update: ");
                username = scanner.nextLine();
                System.out.print("Enter new full name: ");
                fullName = scanner.nextLine();
                System.out.print("Enter new email: ");
                email = scanner.nextLine();
            } else {
                if (!scanner.hasNext()) {
                    System.out.println("Error: missing username, full name and email");
                    return;
                }
                username = scanner.next();
                if (!scanner.hasNextLine()) {
                    System.out.println("Error: missing fullName and email");
                    return;
                }
                String rest = scanner.nextLine().trim();

                if (rest.startsWith("\"")) {
                    int closingQuote = rest.indexOf("\"", 1);
                    if (closingQuote == -1) {
                        System.out.println("Error: missing closing quote for fullName");
                        return;
                    }
                    fullName = rest.substring(1, closingQuote);

                    scanner = new Scanner(rest.substring(closingQuote + 1).trim());
                } else {
                    scanner = new Scanner(rest);
                    fullName = scanner.next();
                }

                if (!scanner.hasNext()) {
                    System.out.println("Error: missing email");
                    return;
                }
                email = scanner.next();

                if (scanner.hasNext()) {
                    String extra = scanner.nextLine();
                    System.out.println("Warning: extra arguments ignored: " + extra);
                }
            }

            try {
                system.getUserManager().update(username, fullName, email);
                System.out.println("User data updated successfully.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }

    private static Command userDeleteCommand() {
        return ((scanner, system, haveArgs) -> {
            if (!haveArgs) {
                System.out.print("Enter username to delete: ");
            }
            String username = scanner.next();

            if (username.equals(system.getCurrentUser())) {
                System.out.println("Error: you can not delete yourself.");
                return;
            }

            if (!haveArgs) {
                System.out.printf("Delete user %s? (y/n): ", username);
                if (!scanner.next().equalsIgnoreCase("yes") &&
                        !scanner.next().equalsIgnoreCase("y")) {
                    System.out.println("Deletion cancelled.");
                    return;
                }
            } else {
                if (!scanner.hasNext() || !scanner.next().equals("-y")) {
                    System.out.println("Deletion cancelled.\nUse -y flag to delete without confirmation.");
                    return;
                }
            }

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Error: no such user.");
                return;
            }
            User user = userOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            assignments.forEach(roleAssignment ->
                    system.getAssignmentManager().remove(roleAssignment));

            try {
                system.getUserManager().remove(user);
                System.out.println("User " + username + " removed successfully.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }

    private static Command userSearchCommand() {
        return ((scanner, system, haveArgs) -> {
            if (haveArgs) {
                userListCommand().execute(scanner, system, true);
                return;
            }
            StringBuilder sb = new StringBuilder();

            System.out.print("""
                    Choose filters:
                    username - by username (contains)
                    email    - by email (contains)
                    domain   - by domain (exact match)
                    fullname - by full name (contains)
                    """);

            while (true) {
                System.out.println("Enter a filter or type \"search\"");
                String option = scanner.nextLine();
                switch (option) {
                    case "username", "email", "domain", "fullname" -> {
                        sb.append(option);
                        sb.append("=");
                        System.out.print(option + "=");
                        sb.append(scanner.nextLine());
                        sb.append(" ");
                    }
                    case "search" -> {
                        userListCommand().execute(
                                new Scanner(sb.toString().trim()),
                                system,
                                true
                        );
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            }
        });
    }
}
