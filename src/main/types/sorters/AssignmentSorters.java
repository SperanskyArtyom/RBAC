package types.sorters;

import types.interfaces.RoleAssignment;

import java.util.Comparator;

public final class AssignmentSorters {

    private AssignmentSorters() {
        throw new AssertionError("Utility class");
    }

    public static Comparator<RoleAssignment> byUsername() {
        return Comparator.comparing(RoleAssignment::user, UserSorters.byUsername());
    }

    public static Comparator<RoleAssignment> byRoleName() {
        return Comparator.comparing(RoleAssignment::role, RoleSorters.byName());
    }

    public static Comparator<RoleAssignment> byAssignmentDate() {
        return Comparator.comparing(
                assignment -> assignment.metadata().assignedAt());
    }
}
