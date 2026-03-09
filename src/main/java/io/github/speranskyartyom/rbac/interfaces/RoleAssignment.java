package io.github.speranskyartyom.rbac.interfaces;

import io.github.speranskyartyom.rbac.models.Role;
import io.github.speranskyartyom.rbac.models.records.AssignmentMetadata;
import io.github.speranskyartyom.rbac.models.records.User;

public interface RoleAssignment {

    String assignmentId();

    User user();

    Role role();

    AssignmentMetadata metadata();

    boolean isActive();

    String assignmentType();
}
