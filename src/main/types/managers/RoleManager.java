package types.managers;

import types.classes.Role;
import types.interfaces.functional.RoleFilter;
import types.records.Permission;

import java.util.*;

public class RoleManager implements Repository<Role> {
    private final Map<String, Role> rolesById = new HashMap<>();
    private final Map<String, Role> rolesByName = new HashMap<>();
    private AssignmentManager assignmentManager;

    @Override
    public void add(Role item) {
        Objects.requireNonNull(item, "Role must not be null");
        if (exists(item.getName())) {
            throw new IllegalArgumentException("Role with name - " + item.getName() + " already exists");
        }
        rolesById.put(item.getId(), item);
        rolesByName.put(item.getName(), item);
    }

    @Override
    public boolean remove(Role item) {
        if (assignmentManager == null) {
            throw new IllegalStateException("AssignmentManager is not initialized");
        }
        if (item == null) return false;
        if (!rolesById.containsKey(item.getId())) return false;
        if (assignmentManager.getActiveAssignments().stream()
                .anyMatch(assignment ->
                        assignment.role().equals(item))) {
            throw new IllegalArgumentException(
                    "You can't remove role that is assigned to at least one user");
        }
        rolesById.remove(item.getId());
        rolesByName.remove(item.getName());
        return true;
    }

    @Override
    public Optional<Role> findById(String id) {
        return Optional.ofNullable(rolesById.get(id));
    }

    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }

    @Override
    public int count() {
        return rolesById.size();
    }

    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }

    public Optional<Role> findByName(String name) {
        return Optional.ofNullable(rolesByName.get(name));
    }

    public List<Role> findByFilter(RoleFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter must not be null");
        }

        return rolesById.values().stream()
                .filter(filter::test)
                .toList();
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter must not be null");
        }
        if (sorter == null) {
            throw new IllegalArgumentException("Sorter must not be null");
        }

        return rolesById.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .toList();
    }

    public boolean exists(String name) {
        return rolesByName.containsKey(name);
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        if (!exists(roleName)) {
            throw new IllegalArgumentException("Role with name - " + roleName + " doesn't exists");
        }
        rolesByName.get(roleName).addPermission(permission);
    }

    public void removePermissionFromRole(String roleName, Permission permission) {
        if (!exists(roleName)) {
            throw new IllegalArgumentException("Role with name - " + roleName + " doesn't exists");
        }
        rolesByName.get(roleName).removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        return rolesById.values().stream()
                .filter(role -> role.hasPermission(permissionName, resource))
                .toList();
    }

    public void setAssignmentManager(AssignmentManager assignmentManager) {
        this.assignmentManager = assignmentManager;
    }
}
