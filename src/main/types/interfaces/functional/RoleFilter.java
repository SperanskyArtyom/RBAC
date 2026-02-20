package types.interfaces.functional;

import types.classes.Role;

@FunctionalInterface
public interface RoleFilter {
    boolean test(Role role);

    default RoleFilter and(RoleFilter other) {
        if (other == null) {
            throw new IllegalArgumentException("Other filter must not be null");
        }

        return role -> test(role) && other.test(role);
    }

    default RoleFilter or(RoleFilter other) {
        if (other == null) {
            throw new IllegalArgumentException("Other filter must not be null");
        }

        return role -> test(role) || other.test(role);
    }
}
