package io.github.speranskyartyom.rbac.cli.commands;

import io.github.speranskyartyom.rbac.cli.CommandParser;
import io.github.speranskyartyom.rbac.filters.UserFilters;
import io.github.speranskyartyom.rbac.interfaces.RoleAssignment;
import io.github.speranskyartyom.rbac.interfaces.functional.Command;
import io.github.speranskyartyom.rbac.interfaces.functional.UserFilter;
import io.github.speranskyartyom.rbac.managers.UserManager;
import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.records.User;

import java.util.*;
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
        return ((_, system, args) -> {
            UserFilter filter = null;

            for (String arg : args) {
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

            UserManager manager = system.getUserManager();
            List<User> users;

            if (filter == null) {
                users = manager.findAll();
            } else {
                users = manager.findByFilter(filter);
            }

            printUserTable(users);
        });
    }

    private static Command userCreateCommand() {
        return ((scanner, system, args) -> {
            String username, fullName, email;

            if (args.length < 1) {
                System.out.print("Enter username: ");
                username = scanner.nextLine();
            } else {
                username = args[0];
            }

            if (args.length < 2) {
                System.out.print("Enter full name: ");
                fullName = scanner.nextLine();
            } else {
                fullName = args[1];
            }

            if (args.length < 3) {
                System.out.print("Enter email: ");
                email = scanner.nextLine();
            } else {
                email = args[2];
            }

            if (args.length > 3) {
                String extra = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                System.out.println("Warning: extra arguments ignored: " + extra);
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
        return ((scanner, system, args) -> {
            String username;

            if (args.length < 1) {
                System.out.print("Enter username: ");
                username = scanner.nextLine();
            } else {
                username = args[0];
            }

            if (args.length > 1) {
                String extra = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                System.out.println("Warning: extra arguments ignored: " + extra);
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
                System.out.println("User has no roles or permissions.");
            } else {
                for (var role : roles) {
                    System.out.println(role.format());
                }
            }
        });
    }

    private static Command userUpdateCommand() {
        return ((scanner, system, args) -> {
            String username, fullName, email;

            if (args.length < 1) {
                System.out.print("Enter username to update: ");
                username = scanner.nextLine();
            } else {
                username = args[0];
            }

            if (args.length < 2) {
                System.out.print("Enter new full name: ");
                fullName = scanner.nextLine();
            } else {
                fullName = args[1];
            }

            if (args.length < 3) {
                System.out.print("Enter new email: ");
                email = scanner.nextLine();
            } else {
                email = args[2];
            }

            if (args.length > 3) {
                String extra = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                System.out.println("Warning: extra arguments ignored: " + extra);
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
        return ((scanner, system, args) -> {
            String username;

            if (args.length < 1) {
                System.out.print("Enter username to delete: ");
                username = scanner.nextLine();
            } else {
                username = args[0];
            }

            if (username.equals(system.getCurrentUser())) {
                System.out.println("Error: you can not delete yourself.");
                return;
            }

            boolean isConfirmed = false;

            if (args.length > 1) {
                if (args[1].equals("-y")) {
                    isConfirmed = true;
                    if (args.length > 2) {
                        String extra = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                        System.out.println("Warning: extra arguments ignored: " + extra);
                    }
                } else {
                    String extra = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    System.out.println("Warning: extra arguments ignored: " + extra);
                }
            }

            if (!isConfirmed) {
                while (true) {
                    System.out.printf("Delete user %s? (y/n): ", username);
                    String answer = scanner.nextLine().toLowerCase();

                    if (answer.equals("yes") || answer.equals("y")) {
                        break;
                    } else if (answer.equals("no") || answer.equals("n")) {
                        System.out.println("Deletion cancelled.");
                        return;
                    }

                    System.out.println("Invalid option. Type \"yes\" to confirm or \"no\" to cancel deletion.");
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
        return ((scanner, system, args) -> {
            if (args.length > 0) {
                userListCommand().execute(scanner, system, args);
                return;
            }

            System.out.print("""
                    Choose filters:
                    username - by username (contains)
                    email    - by email (contains)
                    domain   - by domain (exact match)
                    fullname - by full name (contains)
                    """);

            List<String> argList = new ArrayList<>();

            while (true) {
                StringBuilder sb = new StringBuilder();
                System.out.println("Enter a filter or type \"search\"");
                String option = scanner.nextLine();

                switch (option) {
                    case "username", "email", "domain", "fullname" -> {
                        sb.append(option);
                        sb.append("=");
                        System.out.print(option + "=");
                        sb.append(scanner.nextLine());
                        argList.add(sb.toString());
                    }
                    case "search" -> {
                        args = argList.toArray(new String[0]);
                        userListCommand().execute(
                                scanner,
                                system,
                                args
                        );
                        return;
                    }
                    default -> System.out.println("Invalid option. Try again.");
                }
            }
        });
    }
}
