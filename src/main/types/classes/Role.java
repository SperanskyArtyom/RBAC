package types.classes;

import types.records.Permission;

import java.util.*;

public class Role {
    private final String id;
    private final String name;
    private final String description;
    private final Set<Permission> permissions;

    public Role(String name, String description) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be null or blank");
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description must not be null or blank");
        }

        this.name = name;
        this.description = description;

        id = "role_" + UUID.randomUUID();
        permissions = new HashSet<>();
    }

    public void addPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException(
                    "Can not add null to Role permissions");
        }
        permissions.add(permission);
    }

    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public boolean hasPermission(String permissionName, String resource) {
        return permissions.stream().anyMatch(
                permission -> permission.matches(permissionName, resource)
        );
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Role role)) return false;

        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("Role { id: %s, name: %s, description: %s, permissions: %s}",
                id, name, description, permissions);
    }

    public String format() {
        StringBuilder result = new StringBuilder("Role: " + name + "[ID: " + id + "]\n"
                + "Description: " + description + "\n"
                + "Permissions (" + permissions.size() + "):\n");

        for (var permission : permissions) {
            result.append("\t- ").append(permission.format()).append("\n");
        }

        return result.toString();
    }
}
