package io.github.speranskyartyom.rbac.logging;

import io.github.speranskyartyom.rbac.utils.ValidationUtils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AuditLog {
    private final List<AuditEntry> entries = new ArrayList<>();

    public void log(String action, String performer, String target, String details) {
        ValidationUtils.requireNonEmpty(action, "action");
        ValidationUtils.requireNonEmpty(performer, "performer");
        ValidationUtils.requireNonEmpty(target, "target");
        ValidationUtils.requireNonEmpty(details, "details");

        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        AuditEntry entry = new AuditEntry(
                timestamp,
                action,
                performer,
                target,
                details
        );
        entries.add(entry);
    }

    public List<AuditEntry> getAll() {
        return List.copyOf(entries);
    }

    public List<AuditEntry> getByPerformer(String performer) {
        ValidationUtils.requireNonEmpty(performer, "performer");
        return entries.stream()
                .filter(entry -> entry.performer().equals(performer))
                .toList();
    }

    public List<AuditEntry> getByAction(String action) {
        ValidationUtils.requireNonEmpty(action, "action");
        return entries.stream()
                .filter(entry -> entry.action().equals(action))
                .toList();
    }

    public void printLog() {
        entries.forEach(System.out::println);
    }

    public void saveToFile(String filename) {
        ValidationUtils.requireNonEmpty(filename, "filename");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (AuditEntry entry : entries) {
                writer.write(entry.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save audit log to file: " + filename, e);
        }
    }
}
