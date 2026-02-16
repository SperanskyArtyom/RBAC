package types.records;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    public AssignmentMetadata {
        validate(assignedBy, assignedAt);
    }

    private static void validate(String assignedBy, String assignedAt) {
        if (assignedBy == null || assignedBy.isBlank()) {
            throw new IllegalArgumentException("assignedBy must not be null or blank");
        }

        if (assignedAt == null || assignedAt.isBlank()) {
            throw new IllegalArgumentException("assignedAt must not be null or blank");
        }


        try {
            OffsetDateTime.parse(assignedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
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
        result.append(String.format("Assigned Metadata: by - %s, at - %s", assignedBy, assignedAt));

        if (reason != null)
            result.append(String.format(", reason - %s", reason));

        return result.toString();
    }
}