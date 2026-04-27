package io.github.speranskyartyom.rbac.cli.commands;

import io.github.speranskyartyom.rbac.cli.CommandParser;
import io.github.speranskyartyom.rbac.filters.RoleFilters;
import io.github.speranskyartyom.rbac.interfaces.functional.Command;
import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.records.Permission;
import io.github.speranskyartyom.rbac.models.records.User;
import io.github.speranskyartyom.rbac.utils.ConsoleUtils;
import io.github.speranskyartyom.rbac.utils.FormatUtils;

import java.util.*;

public class PermissionCommands {
    public static void registerCommands(CommandParser parser) {
        parser.registerCommand(
                "permissions-user",
                "List user's permissions grouped by resource.",
                permissionsUserCommand()
        );
        parser.registerCommand(
                "permissions-check",
                "Check if user has permission",
                permissionCheck()
        );
    }

    private static Command permissionsUserCommand() {
        return ((scanner, system, args) -> {
            String username;

            if (args.length < 1) {
                username = ConsoleUtils.promptString(scanner, "Enter username", true);
            } else {
                username = args[0];
            }

            if (args.length > 1) {
                String extra = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                System.out.println("Warning: extra arguments ignored: " + extra);
            }

            Optional<User> userOpt = system.getUserManager().findByUsername(username);

            if (userOpt.isEmpty()) {
                System.out.println("No such user.");
                return;
            }

            User user = userOpt.get();

            Set<Permission> permissions = system.getAssignmentManager().getUserPermissions(user);

            if (permissions.isEmpty()) {
                System.out.println("User has no permissions.");
                return;
            }

            String[] header = {"User's permissions"};

            List<String[]> rows = new ArrayList<>();
            permissions.stream()
                    .sorted(Comparator.comparing(Permission::resource))
                    .forEach(permission ->
                            rows.add(new String[]{permission.format()})
                    );

            System.out.println(FormatUtils.formatTable(header, rows));
        });
    }

    private static Command permissionCheck() {
        return ((scanner, system, args) -> {
            String username, permissionName, permissionResource;

            if (args.length < 1) {
                username = ConsoleUtils.promptString(scanner, "Enter username", true);
            } else {
                username = args[0];
            }

            if (args.length < 2) {
                permissionName = ConsoleUtils.promptString(
                        scanner,
                        "Enter permission name",
                        true
                );
            } else {
                permissionName = args[1];
            }

            if (args.length < 3) {
                System.out.print("Enter permission's resource: ");
                permissionResource = ConsoleUtils.promptString(
                        scanner,
                        "Enter permission's resource",
                        true
                );
            } else {
                permissionResource = args[2];
            }

            if (args.length > 3) {
                String extra = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                System.out.println("Warning: extra arguments ignored: " + extra);
            }

            Optional<User> userOpt = system.getUserManager().findByUsername(username);

            if (userOpt.isEmpty()) {
                System.out.println("No such user.");
                return;
            }

            User user = userOpt.get();

            boolean hasPermission = system.getAssignmentManager()
                    .userHasPermission(user, permissionName, permissionResource);

            System.out.printf("User '%s' %s permission %s on '%s'\n",
                    username, hasPermission ? "has" : "doesn't have",
                    permissionName, permissionResource);
            if (hasPermission) {
                List<Role> roles = system.getRoleManager().findByFilter(
                        RoleFilters.byName(username)
                                .and(RoleFilters.hasPermission(permissionName, permissionResource))
                );

                StringBuilder sb = new StringBuilder();
                for (var role : roles) {
                    sb.append(role.getName()).append(", ");
                }

                System.out.printf("Granted by roles: %s.\n", sb.substring(0, sb.length() - 2));
            }
        });
    }
}
