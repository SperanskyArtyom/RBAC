package io.github.speranskyartyom.rbac.filters;

import io.github.speranskyartyom.rbac.interfaces.RoleAssignment;
import io.github.speranskyartyom.rbac.interfaces.functional.AssignmentFilter;
import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.TemporaryAssignment;
import io.github.speranskyartyom.rbac.models.records.User;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public final class AssignmentFilters {

    private AssignmentFilters() {
        throw new AssertionError("Utility class");
    }

    public static AssignmentFilter byUser(User user) {
        return assignment ->
                assignment.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        return assignment ->
                assignment.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role) {
        return assignment ->
                assignment.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        return assignment ->
                assignment.role().getName().equals(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly() {
        return assignment ->
                !assignment.isActive();
    }

    public static AssignmentFilter byType(String type) {
        return assignment ->
                assignment.assignmentType().equals(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        return assignment ->
                assignment.metadata().assignedBy().equals(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        OffsetDateTime offsetDateTime = parseDate(date);
        return assignment ->
                parseDate(assignment.metadata().assignedAt()).isAfter(offsetDateTime);
    }

    public static AssignmentFilter expiringBefore(String date) {
        OffsetDateTime offsetDateTime = parseDate(date);
        return assignment ->
                assignment instanceof TemporaryAssignment temporaryAssignment
                        && temporaryAssignment.isExpired(offsetDateTime);
    }

    private static OffsetDateTime parseDate(String date) {
        return OffsetDateTime.parse(date,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
