package io.github.speranskyartyom.rbac.cli.commands;

import io.github.speranskyartyom.rbac.cli.CommandParser;
import io.github.speranskyartyom.rbac.filters.AssignmentFilters;
import io.github.speranskyartyom.rbac.interfaces.RoleAssignment;
import io.github.speranskyartyom.rbac.interfaces.functional.AssignmentFilter;
import io.github.speranskyartyom.rbac.interfaces.functional.Command;
import io.github.speranskyartyom.rbac.models.AbstractRoleAssignment;
import io.github.speranskyartyom.rbac.models.PermanentAssignment;
import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.TemporaryAssignment;
import io.github.speranskyartyom.rbac.models.records.AssignmentMetadata;
import io.github.speranskyartyom.rbac.models.records.User;
import io.github.speranskyartyom.rbac.utils.ConsoleUtils;
import io.github.speranskyartyom.rbac.utils.FormatUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class AssignmentsCommands {
    public static void registerCommands(CommandParser parser) {
        parser.registerCommand(
                "assign-role",
                "Assign an existing role to existing user. " +
                        "Assignment can be permanent or temporary.",
                assignRoleCommand()
        );
        parser.registerCommand(
                "revoke-role",
                "Revoke role assignment from user by username.",
                revokeRoleCommand()
        );
        parser.registerCommand(
                "assignment-list",
                "Print table of all assignments in system",
                assignmentListCommand()
        );
        parser.registerCommand(
                "assignment-list-user",
                "Print information of all user's assignments",
                assignmentListUserCommand()
        );
        parser.registerCommand(
                "assignment-list-role",
                "List all users with entered role",
                assignmentListRoleCommand()
        );
        parser.registerCommand(
                "assignment-active",
                "Print table of all active assignments",
                assignmentActiveCommand()
        );
        parser.registerCommand(
                "assignment-expired",
                "Print table of all expired temporary assignments",
                assignmentExpiredCommand()
        );
        parser.registerCommand(
                "assignment-extend",
                "Extend a temporary assignment. " +
                        "Provide assignment ID, or username and role name.",
                assignmentExtendCommand()
        );
        parser.registerCommand(
                "assignment-search",
                "Search assignments by filters." +
                        "Available filters: " +
                        "username, role, type (PERMANENT/TEMPORARY), " +
                        "status (ACTIVE/INACTIVE), assignedAfter (ISO format)," +
                        "expiresBefore (ISO format)",
                assignmentSearch()
        );
    }

    private static Command assignRoleCommand() {
        return ((scanner, system, args) -> {
            String username;

            if (args.length < 1) {
                username = ConsoleUtils.promptString(scanner, "Enter username", true);
            } else {
                username = args[0];
            }

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("Error: No such user.");
                return;
            }

            User user = userOpt.get();

            Role role;

            if (args.length < 2) {
                List<Role> roles = system.getRoleManager().findAll();

                if (roles.isEmpty()) {
                    System.out.println("There are no roles registered in the system. Assigning denied.");
                    return;
                }
                List<String> rolesName = new ArrayList<>();
                for (var r : roles) {
                    rolesName.add(r.getName());
                }
                String roleName = ConsoleUtils.promptChoice(scanner, "Choose role you want to assign", rolesName);
                role = system.getRoleManager().findByName(roleName).get();

            } else {
                Optional<Role> roleOpt = system.getRoleManager().findByName(args[1]);

                if (roleOpt.isEmpty()) {
                    System.out.println("Error: no such role.");
                    return;
                }
                role = roleOpt.get();
            }

            String type;
            if (args.length < 3) {
                List<String> types = List.of("permanent", "temporary");
                type = ConsoleUtils.promptChoice(scanner, "Choose assignment type", types);

            } else {
                type = args[2];
                if (!type.equals("permanent") && !type.equals("temporary")) {
                    System.out.println("Error: Invalid assignment type.");
                    System.out.println("Expected: permanent or temporary. Given: " + type);
                    return;
                }
            }

            RoleAssignment assignment;
            if (type.equals("permanent")) {
                String reason;

                if (args.length < 4) {
                    reason = ConsoleUtils.promptString(scanner, "Enter assignment reason", true);
                } else {
                    reason = args[3];
                }

                if (args.length > 4) {
                    String extra = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                    System.out.println("Warning: extra arguments ignored: " + extra);
                }

                assignment = new PermanentAssignment(
                        user,
                        role,
                        AssignmentMetadata.now(system.getCurrentUser(), reason)
                );
            } else {
                String expiresAt;

                if (args.length < 4) {
                    while (true) {
                        expiresAt = ConsoleUtils.promptString(scanner, "Enter expiration date in ISO format", true);

                        try {
                            OffsetDateTime.parse(expiresAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                            break;
                        } catch (DateTimeParseException e) {
                            System.out.println(expiresAt + " is not an ISO date format.");
                            System.out.println("ISO format: yyyy-MM-ddTHH:mm:ss±HH:mm. " +
                                    "'T' - is delimiter between date and time");
                            System.out.println("Try again.");
                        }
                    }
                } else {
                    expiresAt = args[3];

                    try {
                        OffsetDateTime.parse(expiresAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                    } catch (DateTimeParseException e) {
                        System.out.println("Error: " + expiresAt + " is not an ISO date format.");
                        System.out.println("ISO format: yyyy-MM-ddTHH:mm:ss±HH:mm. " +
                                "'T' - is delimiter between date and time");
                        return;
                    }
                }

                String reason;

                if (args.length < 5) {
                    reason = ConsoleUtils.promptString(scanner, "Enter assignment reason", true);
                } else {
                    reason = args[4];
                }

                if (args.length > 5) {
                    String extra = String.join(" ", Arrays.copyOfRange(args, 5, args.length));
                    System.out.println("Warning: extra arguments ignored: " + extra);
                }

                assignment = new TemporaryAssignment(
                        user,
                        role,
                        AssignmentMetadata.now(
                                system.getCurrentUser(),
                                reason
                        ),
                        expiresAt,
                        false
                );
            }

            try {
                system.getAssignmentManager().add(assignment);
                System.out.println("Role assigned successfully.");
                system.getLogger().log(
                        "ASSIGN ROLE",
                        system.getCurrentUser(),
                        "assignments",
                        "assigned role - " + assignment.role().format() +
                                " to user - " + assignment.user().format()
                );
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }

    private static Command revokeRoleCommand() {
        return ((scanner, system, args) -> {
            String username;

            if (args.length < 1) {
                System.out.print("Enter the username of the user whose assignment you want to revoke: ");
                username = ConsoleUtils.promptString(
                        scanner,
                        "Enter the username of the user whose assignment you want to revoke",
                        true
                );
            } else {
                username = args[0];
            }

            if (args.length > 1) {
                String extra = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                System.out.println("Warning: extra arguments ignored: " + extra);
            }

            if (!system.getUserManager().exists(username)) {
                System.out.println("No such user.");
                return;
            }

            List<RoleAssignment> assignments = system.getAssignmentManager()
                    .findByFilter(AssignmentFilters.byUsername(username).and(AssignmentFilters.activeOnly()));

            if (assignments.isEmpty()) {
                System.out.println("User has no assignments.");
                return;
            }

            System.out.println("Assignments:");
            for (int i = 0; i < assignments.size(); i++) {
                System.out.println((i + 1) + ". " + assignments.get(i).toString());
            }

            RoleAssignment assignment = ConsoleUtils.promptChoice(scanner, "Choose assignment to revoke", assignments);

            if (assignment instanceof PermanentAssignment permanentAssignment) {
                permanentAssignment.revoke();
            } else {
                system.getAssignmentManager().remove(assignment);
            }

            System.out.println("Assignment revoked successfully.");
            system.getLogger().log(
                    "REVOKE ROLE",
                    system.getCurrentUser(),
                    "assignments",
                    "revoked role - " + assignment.role().format() +
                            "from user - " + assignment.user().format()
            );
        });
    }

    private static Command assignmentListCommand() {
        return ((_, system, args) -> {
            if (args.length > 0) {
                String extra = String.join(" ", args);
                System.out.println("Warning: extra arguments ignored: " + extra);
            }

            List<RoleAssignment> assignments = system.getAssignmentManager().findAll();
            printAssignmentsTable(assignments);
        });
    }

    private static Command assignmentListUserCommand() {
        return ((scanner, system, args) -> {
            String username;

            if (args.length < 1) {
                username = ConsoleUtils.promptString(scanner, "Enter username", true);
            } else {
                username = args[0];
            }

            if (args.length > 1) {
                String extra = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                System.out.println("Warning: extra arguments ignored:" + extra);
            }

            Optional<User> userOpt = system.getUserManager().findByUsername(username);
            if (userOpt.isEmpty()) {
                System.out.println("No such user.");
                return;
            }

            User user = userOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(user);
            if (assignments.isEmpty()) {
                System.out.println("User has no role assignments");
                return;
            }

            System.out.println("Role assignments:");
            for (var assignment : assignments) {
                AbstractRoleAssignment abstractRoleAssignment = (AbstractRoleAssignment) assignment;
                System.out.println(abstractRoleAssignment.summary());
            }

        });
    }

    private static Command assignmentListRoleCommand() {
        return ((scanner, system, args) -> {
            String roleName;

            if (args.length < 1) {
                roleName = ConsoleUtils.promptString(scanner, "Enter role name", true);
            } else {
                roleName = args[0];
            }

            if (args.length > 1) {
                String extra = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                System.out.println("Warning: extra arguments ignored:" + extra);
            }

            Optional<Role> roleOpt = system.getRoleManager().findByName(roleName);
            if (roleOpt.isEmpty()) {
                System.out.println("No such role.");
                return;
            }

            Role role = roleOpt.get();
            List<RoleAssignment> assignments = system.getAssignmentManager().findByRole(role);
            if (assignments.isEmpty()) {
                System.out.println("The role is not assigned to any user");
                return;
            }

            System.out.print("Users with role " + roleName + ":");

            StringBuilder sb = new StringBuilder();
            assignments.forEach(assignment ->
                    sb.append(" ").append(assignment.user().username()).append(","));
            System.out.println(sb.substring(0, sb.length() - 1) + ".");
        });
    }

    private static Command assignmentActiveCommand() {
        return ((_, system, args) -> {
            if (args.length != 0) {
                String extra = String.join(" ", args);
                System.out.println("Warning: extra arguments ignored: " + extra);
            }

            List<RoleAssignment> assignments = system.getAssignmentManager().getActiveAssignments();
            printAssignmentsTable(assignments);
        });
    }

    private static Command assignmentExpiredCommand() {
        return ((_, system, args) -> {
            if (args.length != 0) {
                String extra = String.join(" ", args);
                System.out.println("Warning: extra arguments ignored: " + extra);
            }

            List<RoleAssignment> assignments = system.getAssignmentManager().getExpiredAssignments();
            printAssignmentsTable(assignments);
        });
    }

    private static Command assignmentExtendCommand() {
        return ((scanner, system, args) -> {
            String assignmentId = null;
            String username = null;
            String roleName = null;

            if (args.length == 1) {
                assignmentId = args[0];
            } else if (args.length >= 2) {
                username = args[0];
                roleName = args[1];
                if (args.length > 2) {
                    String extra = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    System.out.println("Warning: extra arguments ignored: " + extra);
                }
            } else {
                List<String> idMethods = List.of("by assignment ID", "by username and role name");
                String method = ConsoleUtils.promptChoice(scanner, "Choose identification method", idMethods);

                switch (method) {
                    case "by assignment ID" -> {
                        assignmentId = ConsoleUtils.promptString(scanner, "Enter assignment ID", true);
                    }
                    case "by username and role name" -> {
                        username = ConsoleUtils.promptString(scanner, "Enter username", true);
                        roleName = ConsoleUtils.promptString(scanner, "Enter role name", true);
                    }
                }
            }

            RoleAssignment assignment;
            if (assignmentId != null) {
                Optional<RoleAssignment> opt = system.getAssignmentManager().findById(assignmentId);
                if (opt.isEmpty()) {
                    System.out.println("Assignment with ID '" + assignmentId + "' not found.");
                    return;
                }
                assignment = opt.get();
            } else {

                List<RoleAssignment> assignments = system.getAssignmentManager().findByFilter(
                        AssignmentFilters.byUsername(username)
                                .and(AssignmentFilters.byRoleName(roleName))
                                .and(AssignmentFilters.byType("TEMPORARY"))
                );

                if (assignments.isEmpty()) {
                    System.out.println("No temporary assignment found for user '" + username + "' with role '" + roleName + "'.");
                    return;
                }
                if (assignments.size() > 1) {
                    System.out.println("Multiple temporary assignments found for this user and role:");
                    assignments.forEach(roleAssignment -> {
                        AbstractRoleAssignment abstractRoleAssignment = (AbstractRoleAssignment) roleAssignment;
                        System.out.println(" " + abstractRoleAssignment.assignmentId());
                    });
                    System.out.println("Please use assignment ID.");
                    return;
                }
                assignment = assignments.getFirst();
            }

            if (!(assignment instanceof TemporaryAssignment)) {
                System.out.println("Assignment is not temporary. Cannot extend.");
                return;
            }

            System.out.print("Enter new expiration date in ISO format (yyyy-MM-ddTHH:mm:ss±HH:mm): ");
            String newDate = ConsoleUtils.promptString(
                    scanner,
                    "Enter new expiration date in ISO format (yyyy-MM-ddTHH:mm:ss±HH:mm)",
                    true);

            try {
                system.getAssignmentManager().extendTemporaryAssignment(assignment.assignmentId(), newDate);
                System.out.println("Assignment extended successfully.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        });
    }

    private static Command assignmentSearch() {
        return ((scanner, system, args) -> {
            AssignmentFilter filter = null;

            for (String arg : args) {
                String[] tokens = arg.split("=", 2);

                if (tokens.length != 2) {
                    System.out.println("Invalid filter format: " + arg + ". Expected key=value");
                    return;
                }

                String key = tokens[0].toLowerCase();
                String value = tokens[1];
                AssignmentFilter currentFilter;

                switch (key) {
                    case "username" -> currentFilter = AssignmentFilters.byUsername(value);
                    case "role" -> currentFilter = AssignmentFilters.byRoleName(value);
                    case "type" -> currentFilter = AssignmentFilters.byType(value.toUpperCase());
                    case "status" -> currentFilter = value.equalsIgnoreCase("ACTIVE") ?
                            AssignmentFilters.activeOnly() :
                            AssignmentFilters.inactiveOnly();
                    case "assignedafter" -> currentFilter = AssignmentFilters.assignedAfter(value);
                    case "expiresbefore" -> currentFilter = AssignmentFilters.expiringBefore(value);
                    default -> {
                        System.out.println("Unknown filter: " + key + ".\n" +
                                "Available filters: " +
                                "username, role, type (PERMANENT/TEMPORARY), " +
                                "status (ACTIVE/INACTIVE), assignedAfter (ISO format)," +
                                "expiresBefore (ISO format)");
                        return;
                    }
                }

                filter = filter == null ? currentFilter : filter.and(currentFilter);
            }

            if (args.length == 0) {
                System.out.print("""
                        Choose filters:
                        username      - by username
                        role          - by role name
                        type          - PERMANENT / TEMPORARY
                        status        - ACTIVE / INACTIVE
                        assignedAfter - ISO date format (yyyy-MM-ddTHH:mm:ss±HH:mm)
                        expiresBefore - ISO date format
                        """);

                List<String> filters = List.of(
                        "username", "role", "type", "status", "assignedAfter", "expiresBefore", "stop adding filters"
                );

                List<String> argList = new ArrayList<>();

                while (true) {
                    StringBuilder sb = new StringBuilder();

                    System.out.println("Enter a filter or type \"search\"");
                    String option = ConsoleUtils.promptChoice(scanner, "Choose filter", filters);

                    switch (option) {
                        case "username", "role", "type",
                             "status", "assignedAfter", "expiresBefore" -> {
                            sb.append(option);
                            sb.append("=");
                            String value = ConsoleUtils.promptString(scanner, "Enter value of " + option, false);
                            sb.append(value);
                            argList.add(sb.toString());
                        }
                        case "stop adding filters" -> {
                            args = argList.toArray(new String[0]);
                            assignmentSearch().execute(
                                    scanner,
                                    system,
                                    args
                            );
                            return;
                        }
                    }
                }
            }

            if (filter == null) {
                printAssignmentsTable(system.getAssignmentManager().findAll());
            } else {
                printAssignmentsTable(system.getAssignmentManager().findByFilter(filter));
            }
        });
    }

    private static void printAssignmentsTable(List<RoleAssignment> assignments) {
        if (assignments.isEmpty()) {
            System.out.println("No assignments found.");
            return;
        }

        String[] headers = {"Username", "Role", "Type", "Status", "Assigned at"};
        List<String[]> rows = new ArrayList<>();

        for (RoleAssignment assignment : assignments) {
            String[] row = {
                    assignment.user().username(),
                    assignment.role().getName(),
                    assignment.assignmentType(),
                    assignment.isActive() ? "ACTIVE" : "INACTIVE",
                    assignment.metadata().assignedAt()
            };
            rows.add(row);
        }

        System.out.println(FormatUtils.formatTable(headers, rows));
    }
}
