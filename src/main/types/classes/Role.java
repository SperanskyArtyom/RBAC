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
        this.id = "role_" + UUID.randomUUID();
        this.permissions = new HashSet<>();
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
        return permissions.stream()
                .anyMatch(permission -> permission.matches(permissionName, resource));
    }

    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
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
        StringBuilder result = new StringBuilder();
        result.append(String.format("Role: %s [ID: %s]%n", name, id));
        result.append(String.format("Description: %s%n", description));
        result.append(String.format("Permissions (%d):%n", permissions.size()));

        permissions.forEach(permission ->
                result.append(String.format("\t- %s%n", permission.format())));

        return result.toString();
    }
}
