package types.classes;

import types.records.AssignmentMetadata;
import types.records.User;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TemporaryAssignment extends AbstractRoleAssignment{
    private String expiresAt;
    private final boolean autoRenew;

    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata, String expiresAt, boolean autoRenew) {
        super(user, role, metadata);

        try {
            OffsetDateTime.parse(expiresAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("expiresAt must be ISO offset format");
        }

        this.expiresAt = expiresAt;
        this.autoRenew = autoRenew;
    }

    @Override
    public boolean isActive() {
        return !isExpired();
    }

    @Override
    public String assignmentType() {
        return "TEMPORARY";
    }

    public void extend(String newExpirationDate) {
        try {
            OffsetDateTime.parse(newExpirationDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("expiresAt must be ISO offset format");
        }

        expiresAt = newExpirationDate;
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(OffsetDateTime.parse(expiresAt,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    public boolean isExpired(OffsetDateTime now) {
        return now.isAfter(OffsetDateTime.parse(expiresAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    public String getTimeRemaining() {
        OffsetDateTime expiresAtDateTime = OffsetDateTime.parse(expiresAt,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Duration duration = Duration.between(OffsetDateTime.now(), expiresAtDateTime);

        if (duration.isNegative() || duration.isZero()) {
            return "Expired";
        }

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        return String.format("%d days %d hours %d minutes %d seconds", days, hours, minutes, seconds);
    }

    @Override
    public String summary() {
        return String.format("%sRemaining time: %s%n", super.summary(), getTimeRemaining());
    }

}
