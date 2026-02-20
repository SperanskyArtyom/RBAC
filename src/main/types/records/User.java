package types.records;

import java.util.regex.Pattern;

public record User(
        String username,
        String fullName,
        String email
) {
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
            "^\\w{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[\\w-.]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");


    public User {
        validate(username, fullName, email);
    }

    public static void validate(String username, String fullName, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                    "Username must not be null or blank. Given: " + username);
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                    "Full name must not be null or blank. Given: " + fullName);
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email must not be null or blank. Given: " + email);
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException(
                    "Username must contain only Latin letters, numbers, " +
                            "and the underscore character. Given: " + username);
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
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
