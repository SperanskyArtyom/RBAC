package io.github.speranskyartyom.rbac.models.records;

import io.github.speranskyartyom.rbac.utils.ValidationUtils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    public AssignmentMetadata {
        validate(assignedBy, assignedAt);
    }

    private static void validate(String assignedBy, String assignedAt) {
        ValidationUtils.requireNonEmpty(assignedBy, "assignedBy");
        ValidationUtils.requireNonEmpty(assignedAt, "assignedAt");
        if (!ValidationUtils.isValidDate(assignedAt)) {
            throw new IllegalArgumentException("assignedAt must be ISO offset format");
        }
    }

    public static AssignmentMetadata now(String assignedBy, String reason) {
        return new AssignmentMetadata(
                assignedBy,
                OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                reason);
    }

    public String format() {
        StringBuilder result = new StringBuilder();
        result.append(String.format(
                "Assigned Metadata: by - %s, at - %s",
                assignedBy, assignedAt));

        if (reason != null)
            result.append(String.format(", reason - %s", reason));

        return result.toString();
    }
}