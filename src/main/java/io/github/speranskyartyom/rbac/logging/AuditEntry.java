package io.github.speranskyartyom.rbac.logging;

public record AuditEntry(
        String timestamp,
        String action,
        String performer,
        String target,
        String details
) {
    @Override
    public String toString() {
        return String.format("[%s] %s: %s -> %s | %s",
                timestamp, action, performer, target, details);
    }
}
