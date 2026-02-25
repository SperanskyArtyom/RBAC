package types.classes;

import types.interfaces.RoleAssignment;
import types.records.AssignmentMetadata;
import types.records.User;

import java.util.Objects;
import java.util.UUID;

public abstract class AbstractRoleAssignment implements RoleAssignment {
    private final String assignmentId;
    private final User user;
    private final Role role;
    private final AssignmentMetadata metadata;

    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        this.assignmentId = "assignment_" + UUID.randomUUID();
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override
    public String assignmentId() {
        return assignmentId;
    }

    @Override
    public User user() {
        return user;
    }

    @Override
    public Role role() {
        return role;
    }

    @Override
    public AssignmentMetadata metadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof AbstractRoleAssignment roleAssignment)) return false;

        return Objects.equals(assignmentId, roleAssignment.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(assignmentId);
    }

    public String summary() {
        return String.format(
                "[%s] %s assigned to %s by %s at %s%n" +
                        "Reason: %s%n" +
                        "Status: %s%n",
                assignmentType(),
                role.getName(),
                user.username(),
                metadata.assignedBy(),
                metadata.assignedAt(),
                metadata.reason() == null ? "Not specified" : metadata.reason(),
                isActive() ? "ACTIVE" : "INACTIVE"
        );
    }
}
