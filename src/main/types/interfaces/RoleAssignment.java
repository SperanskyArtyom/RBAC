package types.interfaces;

import types.classes.Role;
import types.records.AssignmentMetadata;
import types.records.User;

public interface RoleAssignment {

    String assignmentId();
    User user();
    Role role();
    AssignmentMetadata metadata();
    boolean isActive();
    String assignmentType();
}
