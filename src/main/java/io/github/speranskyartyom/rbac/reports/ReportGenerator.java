package io.github.speranskyartyom.rbac.reports;

import io.github.speranskyartyom.rbac.interfaces.RoleAssignment;
import io.github.speranskyartyom.rbac.managers.AssignmentManager;
import io.github.speranskyartyom.rbac.managers.RoleManager;
import io.github.speranskyartyom.rbac.managers.UserManager;
import io.github.speranskyartyom.rbac.models.AbstractRoleAssignment;
import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.records.Permission;
import io.github.speranskyartyom.rbac.models.records.User;
import io.github.speranskyartyom.rbac.utils.ValidationUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ReportGenerator {
    public static String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        List<User> users = userManager.findAll();

        StringBuilder sb = new StringBuilder();
        for (User user : users) {
            sb.append("%s%n".formatted(user.format()));
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);

            if (!assignments.isEmpty()) {
                sb.append("Roles:%n".formatted());

                for (RoleAssignment assignment : assignments) {
                    AbstractRoleAssignment abstractRoleAssignment = (AbstractRoleAssignment) assignment;
                    sb.append("\t%s%n".formatted(abstractRoleAssignment.summary()));
                }
            }
        }

        return sb.toString();
    }

    public static String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        List<Role> roles = roleManager.findAll();
        StringBuilder sb = new StringBuilder();

        for (Role role : roles) {
            List<RoleAssignment> assignments = assignmentManager.findByRole(role);
            sb.append("%s%nNumber of users with this role: %d%n".formatted(role.format(), assignments.size()));
        }

        return sb.toString();
    }

    public static String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        List<User> users = userManager.findAll();
        StringBuilder sb = new StringBuilder();

        int maxUsernameLength = "username".length();
        for (User user : users) {
            maxUsernameLength = Math.max(maxUsernameLength, user.username().length());
        }

        String rowFormat = "%-" + maxUsernameLength + "s | %s%n";

        sb.append(rowFormat.formatted("username", "permission"));

        for (User user : users) {
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);

            if (assignments.isEmpty()) {
                sb.append(String.format(rowFormat, user.username(), "null"));
            } else {
                for (RoleAssignment assignment : assignments) {
                    for (Permission permission : assignment.role().getPermissions()) {
                        sb.append(String.format(rowFormat, user.username(), permission.format()));
                    }
                }
            }
        }

        return sb.toString();
    }

    public static void exportToFile(String report, String filename) {
        ValidationUtils.requireNonEmpty(report, "report");
        ValidationUtils.requireNonEmpty(filename, "filename");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(report);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save report to file: " + filename, e);
        }
    }
}
