package io.github.speranskyartyom.rbac.cli.commands;

import io.github.speranskyartyom.rbac.cli.CommandParser;
import io.github.speranskyartyom.rbac.filters.AssignmentFilters;
import io.github.speranskyartyom.rbac.filters.RoleFilters;
import io.github.speranskyartyom.rbac.interfaces.RoleAssignment;
import io.github.speranskyartyom.rbac.interfaces.functional.Command;
import io.github.speranskyartyom.rbac.interfaces.functional.RoleFilter;
import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.records.Permission;
import io.github.speranskyartyom.rbac.utils.ConsoleUtils;

import java.util.*;

public class RolesCommands {
    public static void registerRoleCommands(CommandParser parser) {
        parser.registerCommand(
                "role-list",
                "List all roles with name, permission, and ID.",
                roleListCommand()
        );
        parser.registerCommand(
                "role-create",
                "Add role with optional permissions.",
                roleCreateCommand()
        );
        parser.registerCommand(
                "role-view",
                "View role info by role name (case sensitive).",
                roleViewCommand()
        );
        parser.registerCommand(
                "role-update",
                "Update role's name or description.",
                roleUpdateCommand()
        );
        parser.registerCommand(
                "role-delete",
                "Delete role by name if it doesn't assign to any user",
                roleDeleteCommand()
        );
        parser.registerCommand(
                "role-add-permission",
                "Add new permission to role by role's name",
                roleAddPermissionCommand()
        );
        parser.registerCommand(
                "role-remove-permission",
                "Remove one of declared permission from role by role's name",
                roleRemovePermissionCommand()
        );
        parser.registerCommand(
                "role-search",
                "Search roles by one of this filters: " +
                        "name (contains), permission (name,resource), min-permissions. " +
                        "Format: filter=value",
                roleSearchCommand()
        );
    }

    private static Command roleListCommand() {
        return ((_, system, args) -> {
            if (args.length > 0) {
                String extra = String.join(" ", args);
                System.out.println("Warning: extra arguments ignored: " + extra);
            }

            List<Role> roles = system.getRoleManager().findAll();
            if (roles.isEmpty()) {
                System.out.println("System has no roles.");
                return;
            }

            roles.forEach(role -> System.out.println(role.format()));
        });
    }

    private static Command roleCreateCommand() {
        return ((scanner, system, args) -> {
            String name, description;

            if (args.length < 1) {
                name = ConsoleUtils.promptString(scanner, "Enter role name", true);
            } else {
                name = args[0];
            }

            if (args.length < 2) {
                description = ConsoleUtils.promptString(scanner, "Enter role description", true);
            } else {
                description = args[1];
            }

            if (args.length > 2) {
                String extra = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                System.out.println("Warning: extra arguments ignored: " + extra);
            }

            Role role;
            try {
                role = new Role(name, description);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                return;
            }

            while (ConsoleUtils.promptYesNo(scanner, "Do you want to add permission?")) {

                String permissionName = ConsoleUtils.promptString(
                        scanner, "Enter permission name", true
                );
                String permissionResource = ConsoleUtils.promptString(
                        scanner, "Enter permission's resource", true
                );
                String permissionDescription = ConsoleUtils.promptString(
                        scanner, "Enter permission's description", true
                );

                try {
                    Permission permission = new Permission(
                            permissionName,
                            permissionResource,
                            permissionDescription
                    );
                    role.addPermission(permission);
                    System.out.printf("Permission: %s added successfully.\n", permission.format());
                    System.out.println("To stop adding permissions type \"stop\"");
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    return;
                }
            }

            try {
                system.getRoleManager().add(role);
                System.out.println("Role " + role.getName() + " added successfully.");
                system.getLogger().log(
                        "ROLE CREATE",
                        system.getCurrentUser(),
                        "roles",
                        "created role - " + role.format()
                );
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }

    private static Command roleViewCommand() {
        return ((scanner, system, args) -> {
            String roleName;

            if (args.length < 1) {
                roleName = ConsoleUtils.promptString(scanner, "Enter role name", true);
            } else {
                roleName = args[0];
            }

            if (args.length > 1) {
                String extra = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                System.out.println("Warning: extra arguments ignored: " + extra);
            }

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);

            if (roleOpt.isEmpty()) {
                System.out.println("No such role - \"" + roleName + "\".");
                return;
            }

            Role role = roleOpt.get();
            System.out.println(role.format());
        });
    }

    private static Command roleUpdateCommand() {
        return ((scanner, system, args) -> {
            String name, newName, newDescription;

            if (args.length < 1) {
                name = ConsoleUtils.promptString(scanner, "Enter role name", true);
            } else {
                name = args[0];
            }

            if (args.length < 2) {
                newName = ConsoleUtils.promptString(
                        scanner, "Enter new name (press enter to keep the same)", false
                );
            } else {
                newName = args[1];
            }

            if (args.length < 3) {
                newDescription = ConsoleUtils.promptString(
                        scanner, "Enter new description (press enter to keep the same", false
                );
            } else {
                newDescription = args[2];
            }

            if (args.length > 3) {
                String extra = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                System.out.println("Warning: extra arguments ignored: " + extra);
            }

            try {
                system.getRoleManager().update(name, newName, newDescription);
                System.out.println("Role updated successfully.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }

    private static Command roleDeleteCommand() {
        return ((scanner, system, args) -> {
            String name;

            if (args.length < 1) {
                name = ConsoleUtils.promptString(scanner, "Enter role name", true);
            } else {
                name = args[0];
            }

            Optional<Role> roleOpt = system.getRoleManager().findByName(name);
            if (roleOpt.isEmpty()) {
                System.out.println("No such role - \"" + name + "\".");
                return;
            }
            Role role = roleOpt.get();

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

            List<RoleAssignment> activeAssignments = system.getAssignmentManager().getActiveAssignments().stream()
                    .filter(AssignmentFilters.byRoleName(name)::test).toList();
            if (!isConfirmed && !activeAssignments.isEmpty()) {
                System.out.print("Warning! these role is assigned to users:");
                activeAssignments.forEach(assignment -> System.out.print(" " + assignment.user().username()));
                System.out.println(".\nIf you confirm deletion, this assignments will be revoked");
            }

            if (!isConfirmed) {
                if (!ConsoleUtils.promptYesNo(scanner, "Delete role " + name + "?")) {
                    System.out.println("Deletion cancelled");
                    return;
                }
            }

            try {
                for (var assignment : activeAssignments) {
                    system.getAssignmentManager().remove(assignment);
                    System.out.printf("Assignment for user %s is removed.\n", assignment.user().username());
                }
                system.getRoleManager().remove(role);
                System.out.println("Role removed successfully.");
                system.getLogger().log(
                        "DELETE ROLE",
                        system.getCurrentUser(),
                        "roles",
                        "deleted role - " + role.format()
                );
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }

    private static Command roleAddPermissionCommand() {
        return ((scanner, system, args) -> {
            String roleName, permissionName, permissionResource, permissionDescription;

            if (args.length < 1) {
                roleName = ConsoleUtils.promptString(scanner, "Enter role name", true);
            } else {
                roleName = args[0];
            }

            if (args.length < 2) {
                permissionName = ConsoleUtils.promptString(
                        scanner, "Enter new permission's name", true
                );
            } else {
                permissionName = args[1];
            }

            if (args.length < 3) {
                permissionResource = ConsoleUtils.promptString(
                        scanner, "Enter new permission's resource", true
                );
            } else {
                permissionResource = args[2];
            }

            if (args.length < 4) {
                System.out.print("Enter new permission's description: ");
                permissionDescription = ConsoleUtils.promptString(
                        scanner, "Enter new permission's description", true
                );
            } else {
                permissionDescription = args[3];
            }

            if (args.length > 4) {
                String extra = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                System.out.println("Warning: extra arguments ignored: " + extra);
            }

            try {
                Permission permission = new Permission(
                        permissionName,
                        permissionResource,
                        permissionDescription
                );
                system.getRoleManager().addPermissionToRole(roleName, permission);
                System.out.println("Permission added successfully.");

            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }

    private static Command roleRemovePermissionCommand() {
        return ((scanner, system, args) -> {
            String roleName;

            if (args.length < 1) {
                roleName = ConsoleUtils.promptString(scanner, "Enter role name", true);
            } else {
                roleName = args[0];
            }

            if (args.length > 1) {
                String extra = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                System.out.println("Warning: extra arguments ignored: " + extra);
            }

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);

            if (roleOpt.isEmpty()) {
                System.out.println("No such role - \"" + roleName + "\".");
                return;
            }

            Role role = roleOpt.get();

            Set<Permission> rolePermissions = role.getPermissions();

            if (rolePermissions.isEmpty()) {
                System.out.println("Role \"" + roleName + "\" doesn't have any permission.");
                return;
            }

            List<Permission> permissionList = new ArrayList<>(rolePermissions);

            Permission permission = ConsoleUtils.promptChoice(
                    scanner, "Choose permission to delete", permissionList
            );

            if (!ConsoleUtils.promptYesNo(
                    scanner, "Do you want to delete " + permission.format() + "permission"
            )) {
                System.out.println("Deletion cancelled");
                return;
            }

            system.getRoleManager().removePermissionFromRole(roleName, permission);
            System.out.printf("Permission %s removed successfully.", permission.format());
        });
    }

    private static Command roleSearchCommand() {
        return ((scanner, system, args) -> {
            RoleFilter filter;

            if (args.length > 0) {
                String[] tokens = args[0].split("=", 2);
                if (tokens.length != 2) {
                    System.out.println("Invalid filter format: " + args[0] + ". Expected key=value");
                    return;
                }

                String key = tokens[0].toLowerCase();
                String value = tokens[1];

                switch (key) {
                    case "role-name" -> filter = RoleFilters.byName(value);
                    case "permission" -> {
                        String[] permissionData = value.split(",");
                        if (permissionData.length == 1) {
                            filter = RoleFilters.hasPermission(permissionData[0], null);
                        } else if (permissionData.length == 2) {
                            filter = RoleFilters.hasPermission(permissionData[0], permissionData[1]);
                        } else {
                            System.out.println("Error: Invalid permission filter. Usage: \"permission=name\" or \"permission=name,resource\"");
                            return;
                        }
                    }
                    case "min-permissions" -> {
                        try {
                            int min = Integer.parseInt(value);
                            filter = RoleFilters.hasAtLeastNPermissions(min);
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Invalid min-permission filter is not a number: " + value);
                            return;
                        }
                    }
                    default -> {
                        System.out.println("Unknown filter: " + key + ".\n" +
                                "Available filters: " +
                                "role-name (contains), " +
                                "permission (name,resource), " +
                                "min-permissions.");
                        return;
                    }
                }
            } else {

                List<String> filters = List.of(
                        "by role name (contains)",
                        "by permission (name, resource)",
                        "by minimum number of permissions"
                );

                String answer = ConsoleUtils.promptChoice(scanner, "Choose filter", filters);
                switch (answer) {
                    case "by role name (contains)" -> {
                        String roleName = ConsoleUtils.promptString(scanner, "Enter role name", false);
                        filter = RoleFilters.byNameContains(roleName);
                    }
                    case "by permission (name, resource)" -> {
                        String permissionName = ConsoleUtils.promptString(
                                scanner, "Enter permission's name", false
                        );
                        String permissionResource = ConsoleUtils.promptString(
                                scanner, "Enter permission's resource", false
                        );
                        filter = RoleFilters.hasPermission(permissionName, permissionResource);
                    }
                    case "by minimum number of permissions" -> {
                        int minNumber = ConsoleUtils.promptInt(
                                scanner, "Enter minimum number of permissions", 0, Integer.MAX_VALUE);
                        filter = RoleFilters.hasAtLeastNPermissions(minNumber);
                    }
                    default -> filter = null;
                }
            }
            if (args.length > 1) {
                String extra = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                System.out.println("Warning: extra arguments ignored: " + extra);
            }

            List<Role> roles = system.getRoleManager().findByFilter(filter);

            if (roles.isEmpty()) {
                System.out.println("No roles found.");
                return;
            }

            System.out.println("Found roles:");
            roles.forEach(role -> System.out.println(role.format()));
        });
    }
}
