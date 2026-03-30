package io.github.speranskyartyom.rbac.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.*;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuditLogTest {

    private AuditLog auditLog;
    private PrintStream originalOut;
    private ByteArrayOutputStream outputStreamCaptor;

    @BeforeEach
    void setUp() {
        auditLog = new AuditLog();
    }

    // ==================== Constructor & empty state ====================

    @Test
    @DisplayName("Should create empty audit log")
    void shouldCreateEmptyAuditLog() {
        assertTrue(auditLog.getAll().isEmpty());
    }

    // ==================== log ====================

    @Test
    @DisplayName("Should add an entry to the log")
    void shouldAddEntry() {
        auditLog.log("CREATE_USER", "admin", "john_doe", "User created");

        List<AuditEntry> entries = auditLog.getAll();
        assertEquals(1, entries.size());

        AuditEntry entry = entries.getFirst();
        assertNotNull(entry.timestamp());
        assertEquals("CREATE_USER", entry.action());
        assertEquals("admin", entry.performer());
        assertEquals("john_doe", entry.target());
        assertEquals("User created", entry.details());
    }

    @Test
    @DisplayName("Should generate timestamp in ISO_OFFSET_DATE_TIME format")
    void shouldGenerateValidTimestamp() {
        String before = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        auditLog.log("TEST", "tester", "target", "details");
        String after = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        AuditEntry entry = auditLog.getAll().getFirst();
        String timestamp = entry.timestamp();

        assertTrue(timestamp.compareTo(before) >= 0 && timestamp.compareTo(after) <= 0,
                "Timestamp should be within the expected range");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Should throw IllegalArgumentException when action is null or blank")
    void shouldThrowWhenActionInvalid(String action) {
        assertThrows(IllegalArgumentException.class,
                () -> auditLog.log(action, "performer", "target", "details"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Should throw IllegalArgumentException when performer is null or blank")
    void shouldThrowWhenPerformerInvalid(String performer) {
        assertThrows(IllegalArgumentException.class,
                () -> auditLog.log("ACTION", performer, "target", "details"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Should throw IllegalArgumentException when target is null or blank")
    void shouldThrowWhenTargetInvalid(String target) {
        assertThrows(IllegalArgumentException.class,
                () -> auditLog.log("ACTION", "performer", target, "details"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Should throw IllegalArgumentException when details is null or blank")
    void shouldThrowWhenDetailsInvalid(String details) {
        assertThrows(IllegalArgumentException.class,
                () -> auditLog.log("ACTION", "performer", "target", details));
    }

    // ==================== getAll ====================

    @Test
    @DisplayName("Should return an unmodifiable copy of entries")
    void shouldReturnUnmodifiableList() {
        auditLog.log("ACTION", "user", "target", "details");

        List<AuditEntry> all = auditLog.getAll();
        assertEquals(1, all.size());

        assertThrows(UnsupportedOperationException.class, () -> all.add(null));
    }

    // ==================== getByPerformer ====================

    @Test
    @DisplayName("Should return entries filtered by performer")
    void shouldFilterByPerformer() {
        auditLog.log("ACTION1", "admin", "target1", "details1");
        auditLog.log("ACTION2", "user", "target2", "details2");
        auditLog.log("ACTION3", "admin", "target3", "details3");

        List<AuditEntry> adminEntries = auditLog.getByPerformer("admin");
        assertEquals(2, adminEntries.size());
        assertTrue(adminEntries.stream().allMatch(e -> e.performer().equals("admin")));

        List<AuditEntry> userEntries = auditLog.getByPerformer("user");
        assertEquals(1, userEntries.size());
        assertTrue(userEntries.stream().allMatch(e -> e.performer().equals("user")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Should throw IllegalArgumentException when performer for filter is null or blank")
    void shouldThrowWhenPerformerForFilterInvalid(String performer) {
        assertThrows(IllegalArgumentException.class, () -> auditLog.getByPerformer(performer));
    }

    // ==================== getByAction ====================

    @Test
    @DisplayName("Should return entries filtered by action")
    void shouldFilterByAction() {
        auditLog.log("CREATE", "admin", "target1", "details1");
        auditLog.log("DELETE", "admin", "target2", "details2");
        auditLog.log("CREATE", "user", "target3", "details3");

        List<AuditEntry> createEntries = auditLog.getByAction("CREATE");
        assertEquals(2, createEntries.size());
        assertTrue(createEntries.stream().allMatch(e -> e.action().equals("CREATE")));

        List<AuditEntry> deleteEntries = auditLog.getByAction("DELETE");
        assertEquals(1, deleteEntries.size());
        assertTrue(deleteEntries.stream().allMatch(e -> e.action().equals("DELETE")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Should throw IllegalArgumentException when action for filter is null or blank")
    void shouldThrowWhenActionForFilterInvalid(String action) {
        assertThrows(IllegalArgumentException.class, () -> auditLog.getByAction(action));
    }

    // ==================== printLog ====================

    @BeforeEach
    void setUpStreams() {
        originalOut = System.out;
        outputStreamCaptor = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Should print all entries to console")
    void shouldPrintLog() {
        auditLog.log("ACTION1", "user1", "target1", "details1");
        auditLog.log("ACTION2", "user2", "target2", "details2");

        auditLog.printLog();

        String output = outputStreamCaptor.toString().trim();
        String[] lines = output.split(System.lineSeparator());

        assertEquals(2, lines.length);

        List<AuditEntry> entries = auditLog.getAll();
        assertEquals(entries.get(0).toString(), lines[0]);
        assertEquals(entries.get(1).toString(), lines[1]);
    }

    @Test
    @DisplayName("Should print nothing when log is empty")
    void shouldPrintNothingWhenEmpty() {
        auditLog.printLog();
        assertEquals("", outputStreamCaptor.toString().trim());
    }

    // ==================== saveToFile ====================

    @Test
    @DisplayName("Should save log entries to file")
    void shouldSaveToFile(@TempDir Path tempDir) {
        Path file = tempDir.resolve("audit.log");

        auditLog.log("ACTION1", "user1", "target1", "details1");
        auditLog.log("ACTION2", "user2", "target2", "details2");

        auditLog.saveToFile(file.toString());

        List<String> lines;
        try {
            lines = java.nio.file.Files.readAllLines(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertEquals(2, lines.size());

        List<AuditEntry> entries = auditLog.getAll();
        assertEquals(entries.get(0).toString(), lines.get(0));
        assertEquals(entries.get(1).toString(), lines.get(1));
    }

    @Test
    @DisplayName("Should overwrite existing file")
    void shouldOverwriteFile(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("audit.log");
        java.nio.file.Files.write(file, List.of("old content"));

        auditLog.log("NEW_ENTRY", "user", "target", "details");
        auditLog.saveToFile(file.toString());

        List<String> lines = java.nio.file.Files.readAllLines(file);
        assertEquals(1, lines.size());
        assertEquals(auditLog.getAll().getFirst().toString(), lines.getFirst());
    }

    @Test
    @DisplayName("Should throw RuntimeException when writing fails")
    void shouldThrowOnWriteFailure() {
        // Use an invalid path (e.g., directory that doesn't exist)
        String invalidPath = "/nonexistent/directory/file.log";
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> auditLog.saveToFile(invalidPath));
        assertTrue(exception.getMessage().contains("Failed to save audit log to file"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("Should throw IllegalArgumentException when filename is null or blank")
    void shouldThrowWhenFilenameInvalid(String filename) {
        assertThrows(IllegalArgumentException.class, () -> auditLog.saveToFile(filename));
    }
}