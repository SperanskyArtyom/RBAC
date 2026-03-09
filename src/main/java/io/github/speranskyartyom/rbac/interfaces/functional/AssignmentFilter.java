package io.github.speranskyartyom.rbac.interfaces.functional;

import io.github.speranskyartyom.rbac.interfaces.RoleAssignment;

@FunctionalInterface
public interface AssignmentFilter {
    boolean test(RoleAssignment assignment);

    default AssignmentFilter and(AssignmentFilter other) {
        if (other == null) {
            throw new IllegalArgumentException("Other filter must not be null");
        }

        return assignment -> test(assignment) && other.test(assignment);
    }

    default AssignmentFilter or(AssignmentFilter other) {
        if (other == null) {
            throw new IllegalArgumentException("Other filter must not be null");
        }

        return assignment -> test(assignment) || other.test(assignment);
    }
}
