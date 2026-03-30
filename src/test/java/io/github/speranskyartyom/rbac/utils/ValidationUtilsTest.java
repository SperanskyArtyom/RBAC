package io.github.speranskyartyom.rbac.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {
    // ==================== isValidUsername ====================

    @ParameterizedTest
    @ValueSource(strings = {"abc", "user_123", "USER_NAME", "a1_b2_c3", "validUsername20"})
    @DisplayName("Should return true for valid usernames")
    void shouldReturnTrueForValidUsernames(String username) {
        assertTrue(ValidationUtils.isValidUsername(username));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "a", "", " ", "veryLongUsernameThatExceedsTwentyChars", "user@name", "user name", "user-name", "user.name"})
    @DisplayName("Should return false for invalid usernames")
    void shouldReturnFalseForInvalidUsernames(String username) {
        assertFalse(ValidationUtils.isValidUsername(username));
    }

    // ==================== isValidEmail ====================

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "user.name@example.co.uk",
            "user-name@example.org",
            "user_name@example.net",
            "123@example.com",
            "a@b.cd"
    })
    @DisplayName("Should return true for valid email addresses")
    void shouldReturnTrueForValidEmails(String email) {
        assertTrue(ValidationUtils.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "user@",
            "@example.com",
            "user@example",
            "user@.com",
            "user@example.",
            "user name@example.com",
            "user@example..com",
            "user@example.c",
            "user@example.com.",
            "user@example..com",
            "user..name@example.com"
    })
    @DisplayName("Should return false for invalid email addresses")
    void shouldReturnFalseForInvalidEmails(String email) {
        assertFalse(ValidationUtils.isValidEmail(email));
    }

    // ==================== isValidDate ====================

    @ParameterizedTest
    @ValueSource(strings = {
            "2024-12-25T10:30:00+03:00",
            "2024-12-25T10:30:00Z",
            "2024-12-25T10:30:00+00:00",
            "2024-12-25T10:30:00-05:00"
    })
    @DisplayName("Should return true for valid ISO 8601 offset date-time strings")
    void shouldReturnTrueForValidDates(String date) {
        assertTrue(ValidationUtils.isValidDate(date));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            " ",
            "2024-12-25",
            "2024-12-25T10:30:00",
            "2024-12-25T10:30:00Z+",
            "not-a-date"
    })
    @DisplayName("Should return false for invalid date strings")
    void shouldReturnFalseForInvalidDates(String date) {
        assertFalse(ValidationUtils.isValidDate(date));
    }

    // ==================== normalizeString ====================

    @ParameterizedTest
    @CsvSource({
            "'  Hello  ', 'hello'",
            "'  HELLO  ', 'hello'",
            "'  MiXeD  ', 'mixed'",
            "'  trim  ', 'trim'",
            "'', ''",
            "'   ', ''"
    })
    @DisplayName("Should normalize string to lowercase, trimmed")
    void shouldNormalizeToLowercase(String input, String expected) {
        assertEquals(expected, ValidationUtils.normalizeString(input));
    }

    @ParameterizedTest
    @CsvSource({
            "'  Hello  ', true, 'HELLO'",
            "'  Hello  ', false, 'hello'",
            "'  MiXeD  ', true, 'MIXED'",
            "'  MiXeD  ', false, 'mixed'",
            "'  trim  ', true, 'TRIM'",
            "'', true, ''",
            "'   ', false, ''"
    })
    @DisplayName("Should normalize string with case control")
    void shouldNormalizeWithCase(String input, boolean toUpperCase, String expected) {
        assertEquals(expected, ValidationUtils.normalizeString(input, toUpperCase));
    }

    // ==================== requireNonEmpty ====================

    @ParameterizedTest
    @ValueSource(strings = {"value", "some text", "   not empty after trim  "})
    @DisplayName("Should not throw when value is non-empty")
    void shouldNotThrowForNonEmptyValue(String value) {
        assertDoesNotThrow(() -> ValidationUtils.requireNonEmpty(value, "field"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when value is null")
    void shouldThrowWhenValueIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty(null, "username"));
        assertEquals("username must not be null or blank", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n", "   "})
    @DisplayName("Should throw IllegalArgumentException when value is blank")
    void shouldThrowWhenValueIsBlank(String blankValue) {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> ValidationUtils.requireNonEmpty(blankValue, "email"));
        assertEquals("email must not be null or blank", ex.getMessage());
    }
}