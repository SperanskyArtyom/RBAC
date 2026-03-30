package io.github.speranskyartyom.rbac.models.records;

import io.github.speranskyartyom.rbac.utils.ValidationUtils;

public record User(
        String username,
        String fullName,
        String email
) {
    public User {
        validate(username, fullName, email);
    }

    public static void validate(String username, String fullName, String email) {
        ValidationUtils.requireNonEmpty(username, "username");
        ValidationUtils.requireNonEmpty(fullName, "fullName");
        ValidationUtils.requireNonEmpty(email, "email");
        if (!ValidationUtils.isValidUsername(username)) {
            throw new IllegalArgumentException(
                    "Username must contain only Latin letters, numbers, " +
                            "and the underscore character. Given: " + username);
        }
        if (!ValidationUtils.isValidEmail(email)) {
            throw new IllegalArgumentException(
                    "Email has invalid format. Given: " + email);
        }
    }

    public String format() {
        return String.format(
                "%s (%s) <%s>",
                username, fullName, email);
    }
}
