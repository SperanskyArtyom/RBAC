package io.github.speranskyartyom.rbac.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FormatUtilsTest {

    // ==================== truncate ====================

    @ParameterizedTest
    @CsvSource({
            "LongTextHere, 7, Long...",
            "Short, 10, Short",
            "Exactly10c, 10, Exactly10c",
            ", 5, ''"
    })
    @DisplayName("Should truncate strings correctly based on maxLength")
    void truncate_CorrectlyTruncates(String input, int max, String expected) {
        assertEquals(expected, FormatUtils.truncate(input, max));
    }

    @Test
    @DisplayName("Should handle very small maxLength in truncate")
    void truncate_SmallMaxLength_ReturnsDots() {
        assertEquals("..", FormatUtils.truncate("AnyText", 2));
        assertEquals(".", FormatUtils.truncate("AnyText", 1));
    }

    // ==================== padding ====================

    @ParameterizedTest
    @CsvSource({
            "Hi, 5, '   Hi'",
            "Hello, 5, Hello",
            "LongerThanLength, 5, Lo..."
    })
    @DisplayName("Should pad left (align right)")
    void padLeft_AlignsRight(String input, int length, String expected) {
        assertEquals(expected, FormatUtils.padLeft(input, length));
    }

    @ParameterizedTest
    @CsvSource({
            "Hi, 5, 'Hi   '",
            "Hello, 5, Hello",
            "LongerThanLength, 5, Lo..."
    })
    @DisplayName("Should pad right (align left)")
    void padRight_AlignsLeft(String input, int length, String expected) {
        assertEquals(expected, FormatUtils.padRight(input, length));
    }

    // ==================== formatBox ====================

    @Test
    @DisplayName("Should wrap text in a box with correct Unicode characters")
    void formatBox_ReturnsCorrectBox() {
        String result = FormatUtils.formatBox("OK");

        assertTrue(result.contains("┌──┐"));
        assertTrue(result.contains("│OK│"));
        assertTrue(result.contains("└──┘"));
    }

    // ==================== formatHeader ====================

    @Test
    @DisplayName("Should include ANSI blue color and separators in header")
    void formatHeader_ContainsAnsiAndDecoration() {
        String text = "TITLE";
        String result = FormatUtils.formatHeader(text);

        assertTrue(result.contains("==="));
        assertTrue(result.contains("\u001B[34m")); // ANSI_BLUE
        assertTrue(result.contains(text));
        assertTrue(result.contains("\u001B[0m"));  // ANSI_RESET
    }

    // ==================== formatTable ====================

    @Test
    @DisplayName("Should throw IllegalArgumentException when row column count mismatch")
    void formatTable_MismatchColumnCount_ThrowsException() {
        String[] headers = {"ID", "Name"};
        List<String[]> rows = List.of(
                new String[]{"1", "Admin"},
                new String[]{"2"}
        );

        assertThrows(IllegalArgumentException.class, () -> FormatUtils.formatTable(headers, rows));
    }

    @Test
    @DisplayName("Should return empty string when both headers and rows are null")
    void formatTable_NullData_ReturnsEmptyString() {
        assertEquals("", FormatUtils.formatTable(null, null));
    }

    @Test
    @DisplayName("Should format a valid table correctly")
    void formatTable_ValidInput_ReturnsFormattedTable() {
        String[] headers = {"ID", "Role"};
        List<String[]> rows = List.of(
                new String[]{"1", "ADMIN"},
                new String[]{"22", "USER"}
        );

        String table = FormatUtils.formatTable(headers, rows);

        assertTrue(table.contains("ID"));
        assertTrue(table.contains("Role"));
        assertTrue(table.contains("ADMIN"));
        assertTrue(table.contains("22"));

        assertTrue(table.contains("┼"));
        assertTrue(table.contains("┴"));

        String[] lines = table.split("\n");
        assertEquals(6, lines.length);
    }

    @Test
    @DisplayName("Should format table correctly even without headers")
    void formatTable_NoHeaders_WorksCorrectly() {
        List<String[]> rows = List.<String[]>of(new String[]{"OnlyData"});
        String table = FormatUtils.formatTable(null, rows);

        assertFalse(table.contains("┼"));
        assertTrue(table.contains("OnlyData"));
    }
}