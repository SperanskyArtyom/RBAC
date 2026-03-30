package io.github.speranskyartyom.rbac.core;

import io.github.speranskyartyom.rbac.interfaces.RoleAssignment;
import io.github.speranskyartyom.rbac.logging.AuditLog;
import io.github.speranskyartyom.rbac.managers.AssignmentManager;
import io.github.speranskyartyom.rbac.managers.RoleManager;
import io.github.speranskyartyom.rbac.managers.UserManager;
import io.github.speranskyartyom.rbac.models.PermanentAssignment;
import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.records.AssignmentMetadata;
import io.github.speranskyartyom.rbac.models.records.Permission;
import io.github.speranskyartyom.rbac.models.records.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RBACSystem {
    private final UserManager userManager = new UserManager();
    private final RoleManager roleManager = new RoleManager();
    private final AssignmentManager assignmentManager = new AssignmentManager(userManager, roleManager);
    private final AuditLog logger = new AuditLog();

    private String currentUser;

    public RBACSystem() {
        roleManager.setAssignmentManager(assignmentManager);
    }

    private static List<Permission> createStandardPermissions() {
        List<Permission> permissions = new ArrayList<>();

        final String[] permissionNames = {"READ", "WRITE", "DELETE"};
        final String[] permissionResources = {"users", "roles", "assignments", "settings", "reports"};

        for (String name : permissionNames) {
            for (String resource : permissionResources) {
                permissions.add(new Permission(
                        name,
                        resource,
                        "Standard permission"
                ));
            }
        }
        return permissions;
    }

    private static Role createAdminRole(List<Permission> permissions) {
        Role admin = new Role("Admin", "Full system access");
        permissions.forEach(admin::addPermission);
        return admin;
    }

    private static Role createManagerRole(List<Permission> permissions) {
        Role manager = new Role("Manager", "Access to everything except settings");
        permissions.stream()
                .filter(permission -> !permission.resource().equals("settings"))
                .forEach(manager::addPermission);
        return manager;
    }

    private static Role createViewerRole(List<Permission> permissions) {
        Role viewer = new Role("Viewer", "READ everything except settings + WRITE reports");
        permissions.stream()
                .filter(permission -> (permission.name().equals("READ") &&
                        !permission.resource().equals("settings")) ||
                        permission.matches("WRITE", "reports"))
                .forEach(viewer::addPermission);
        return viewer;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RoleManager getRoleManager() {
        return roleManager;
    }

    public AssignmentManager getAssignmentManager() {
        return assignmentManager;
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(String username) {
        if (!userManager.exists(username)) {
            throw new IllegalArgumentException("User does not exist: " + username);
        }
        currentUser = username;
    }

    public AuditLog getLogger() {
        return logger;
    }

    public String generateStatistics() {
        int users = userManager.count();
        int roles = roleManager.count();
        int totalAssignments = assignmentManager.count();
        int activeAssignments = assignmentManager.getActiveAssignments().size();
        int expiredAssignments = assignmentManager.getExpiredAssignments().size();
        double avgRolesPerUser = (double) activeAssignments / users;
        String topRoles = String.join(", ", getTopRoles(3));

        return String.format("""
                        System statistics
                            Users: %d
                            Roles: %d
                            Assignments: TOTAL %d, ACTIVE %d, EXPIRED %d
                            Average number of roles per user: %.2f
                            Top roles: %s
                        """,
                users,
                roles,
                totalAssignments,
                activeAssignments,
                expiredAssignments,
                avgRolesPerUser,
                topRoles);
    }

    public void initialize() {
        List<Permission> permissions = createStandardPermissions();

        Role admin = createAdminRole(permissions);
        Role manager = createManagerRole(permissions);
        Role viewer = createViewerRole(permissions);

        roleManager.add(admin);
        roleManager.add(manager);
        roleManager.add(viewer);

        User adminUser = new User(
                "admin",
                "System administrator",
                "admin@system.local"
        );
        userManager.add(adminUser);

        assignmentManager.add(new PermanentAssignment(
                adminUser,
                admin,
                AssignmentMetadata.now("system", "initial setup")
        ));

        setCurrentUser(adminUser.username());
    }

    private List<String> getTopRoles(int limit) {
        return assignmentManager.getActiveAssignments().stream()
                .collect(Collectors.groupingBy(
                        RoleAssignment::role,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(entry -> entry.getKey().getName())
                .toList();
    }

}
