package io.github.speranskyartyom.rbac.managers;

import io.github.speranskyartyom.rbac.filters.AssignmentFilters;
import io.github.speranskyartyom.rbac.interfaces.RoleAssignment;
import io.github.speranskyartyom.rbac.interfaces.functional.AssignmentFilter;
import io.github.speranskyartyom.rbac.models.PermanentAssignment;
import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.TemporaryAssignment;
import io.github.speranskyartyom.rbac.models.records.Permission;
import io.github.speranskyartyom.rbac.models.records.User;

import java.util.*;

public class AssignmentManager implements Repository<RoleAssignment> {
    private final Map<String, RoleAssignment> assignments = new HashMap<>();
    private final UserManager userManager;
    private final RoleManager roleManager;

    public AssignmentManager(UserManager userManager, RoleManager roleManager) {
        this.userManager = Objects.requireNonNull(userManager,
                "User manager must not be null");
        this.roleManager = Objects.requireNonNull(roleManager,
                "Role manager must not be null");
        roleManager.setAssignmentManager(this);
    }

    @Override
    public void add(RoleAssignment item) {
        Objects.requireNonNull(item, "Role assignment must not be null");
        if (!userManager.exists(item.user().username())) {
            throw new IllegalArgumentException(
                    "User " + item.user().username() + " doesn't exist");
        }
        if (!roleManager.exists(item.role().getName())) {
            throw new IllegalArgumentException(
                    "Role " + item.role().getName() + " doesn't exist");
        }
        if (userHasRole(item.user(), item.role())) {
            throw new IllegalArgumentException(
                    "User " + item.user().username()
                            + " already have role " + item.role().getName());
        }
        if (assignments.containsKey(item.assignmentId())) {
            throw new IllegalArgumentException(
                    "Role assignment with id - " + item.assignmentId() + " already exists");
        }
        assignments.put(item.assignmentId(), item);
    }

    @Override
    public boolean remove(RoleAssignment item) {
        if (item == null) return false;
        return assignments.remove(item.assignmentId()) != null;
    }

    @Override
    public Optional<RoleAssignment> findById(String id) {
        return Optional.ofNullable(assignments.get(id));
    }

    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignments.values());
    }

    @Override
    public int count() {
        return assignments.size();
    }

    @Override
    public void clear() {
        assignments.clear();
    }

    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter must not be null");
        }

        return assignments.values().stream()
                .filter(filter::test)
                .toList();
    }

    public List<RoleAssignment> findByUser(User user) {
        return findByFilter(AssignmentFilters.byUser(user));
    }

    public List<RoleAssignment> findByRole(Role role) {
        return findByFilter(AssignmentFilters.byRole(role));
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter must not be null");
        }
        if (sorter == null) {
            throw new IllegalArgumentException("Sorter must not be null");
        }

        return assignments.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .toList();
    }

    public List<RoleAssignment> getActiveAssignments() {
        return findByFilter(AssignmentFilters.activeOnly());
    }

    public List<RoleAssignment> getExpiredAssignments() {
        return findByFilter(AssignmentFilters.inactiveOnly());
    }

    public boolean userHasRole(User user, Role role) {
        return getActiveAssignments().stream()
                .filter(assignment ->
                        assignment.user().equals(user))
                .anyMatch(assignment ->
                        assignment.role().equals(role)
                );
    }

    public boolean userHasPermission(User user, String permissionName, String resource) {
        return getActiveAssignments().stream()
                .filter(assignment ->
                        assignment.user().equals(user))
                .anyMatch(assignment ->
                        assignment.role().hasPermission(permissionName, resource));
    }

    public Set<Permission> getUserPermissions(User user) {
        Set<Permission> permissions = new HashSet<>();
        getActiveAssignments().stream()
                .filter(assignment ->
                        assignment.user().equals(user))
                .forEach(assignment ->
                        permissions.addAll(assignment.role().getPermissions()));
        return permissions;
    }

    public void revokeAssignment(String assignmentId) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment not found");
        }
        if (assignment instanceof PermanentAssignment permanentAssignment) {
            permanentAssignment.revoke();
        } else {
            remove(assignment);
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        RoleAssignment assignment = assignments.get(assignmentId);
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment not found");
        }
        if (assignment instanceof TemporaryAssignment temporaryAssignment) {
            temporaryAssignment.extend(newExpirationDate);
        } else {
            throw new IllegalArgumentException("Role assignment with id - "
                    + assignmentId
                    + " is not temporary assignment");
        }
    }
}
