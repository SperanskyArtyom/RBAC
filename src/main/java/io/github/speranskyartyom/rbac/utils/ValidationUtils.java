package io.github.speranskyartyom.rbac.utils;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9]+([._-][a-zA-Z0-9]+)*" +
                    "@[a-zA-Z0-9]+([.-][a-zA-Z0-9]+)*" +
                    "\\.[A-Za-z]{2,}$");


    private ValidationUtils() {
        throw new AssertionError("Utility class");
    }

    public static boolean isValidUsername(String username) {
        return USERNAME_PATTERN.matcher(username).matches();
    }

    public static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidDate(String date) {
        try {
            OffsetDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }

    public static String normalizeString(String input, boolean toUpperCase) {
        String normalized = input.trim();
        if (toUpperCase) {
            normalized = normalized.toUpperCase();
        } else {
            normalized = normalized.toLowerCase();
        }
        return normalized;
    }

    public static String normalizeString(String input) {
        return normalizeString(input, false);
    }

    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
    }
}
