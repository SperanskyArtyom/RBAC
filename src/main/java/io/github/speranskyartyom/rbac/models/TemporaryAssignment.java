package io.github.speranskyartyom.rbac.models;

import io.github.speranskyartyom.rbac.models.records.AssignmentMetadata;
import io.github.speranskyartyom.rbac.models.records.User;
import io.github.speranskyartyom.rbac.utils.ValidationUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class TemporaryAssignment extends AbstractRoleAssignment {
    private final boolean autoRenew;
    private String expiresAt;

    public TemporaryAssignment(
            User user,
            Role role,
            AssignmentMetadata metadata,
            String expiresAt,
            boolean autoRenew) {
        if (!ValidationUtils.isValidDate(expiresAt)) {
            throw new IllegalArgumentException("expiresAt must be ISO offset format");
        }
        super(user, role, metadata);
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
        if (!ValidationUtils.isValidDate(newExpirationDate)) {
            throw new IllegalArgumentException("expiresAt must be ISO offset format");
        }
        expiresAt = newExpirationDate;
    }

    private OffsetDateTime getExpiresAtDateTime() {
        return OffsetDateTime.parse(expiresAt,
                DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public boolean isExpired() {
        return OffsetDateTime.now().isAfter(getExpiresAtDateTime());
    }

    public boolean isExpired(OffsetDateTime now) {
        return now.isAfter(getExpiresAtDateTime());
    }

    public String getTimeRemaining() {
        Duration duration = Duration.between(OffsetDateTime.now(), getExpiresAtDateTime());

        if (duration.isNegative() || duration.isZero()) {
            return "Expired";
        }

        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        return String.format(
                "%d days %d hours %d minutes %d seconds",
                days, hours, minutes, seconds);
    }

    @Override
    public String summary() {
        return String.format("%sRemaining time: %s%n",
                super.summary(), getTimeRemaining());
    }
}
