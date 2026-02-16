package types.records;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    @DisplayName("Should create valid user")
    void shouldCreateValidUser() {
        String username = "John_Smith";
        String fullName = "John Smith";
        String email = "john.smith@example.com";

        User user = new User(username, fullName, email);

        assertEquals(username, user.username());
        assertEquals(fullName, user.fullName());
        assertEquals(email, user.email());
    }

    @Test
    @DisplayName("Should throw exception when username is null")
    void shouldThrowIfUsernameNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new User(null, "John Smith", "john.smith@example.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    @DisplayName("Should throw exception when username is blank")
    void shouldThrowIfUsernameBlank(String username) {
        assertThrows(IllegalArgumentException.class,
                () -> new User(username, "John Smith", "john.smith@example.com"));
    }

    @Test
    @DisplayName("Should throw exception if username is too short")
    void shouldThrowIfUsernameTooShort() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("Jo", "John Smith", "john.smith@example.com"));
    }

    @Test
    @DisplayName("Should throw exception if username is too long")
    void shouldThrowIfUsernameTooLong() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("John__________________Smith", "John Smith", "john.smith@example.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"John!", "John ", "Джон"})
    @DisplayName("Should throw exception if username has invalid characters")
    void shouldThrowIfUsernameInvalidChars(String username) {
        assertThrows(IllegalArgumentException.class,
                () -> new User(username, "John Smith", "john.smith@example.com"));
    }

    @Test
    @DisplayName("Should throw exception when full name is null")
    void shouldThrowIfFullNameNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("John_Smith", null, "john.smith@example.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    @DisplayName("Should throw exception when full name is blank")
    void shouldThrowIfFullNameBlank(String fullName) {
        assertThrows(IllegalArgumentException.class,
                () -> new User("John_Smith", fullName, "john.smith@example.com"));
    }

    @Test
    @DisplayName("Should throw exception when email is null")
    void shouldThrowIfEmailNull() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("John_Smith", "John Smith", null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\n"})
    @DisplayName("Should throw exception when email is blank")
    void shouldThrowIfEmailBlank(String email) {
        assertThrows(IllegalArgumentException.class,
                () -> new User("John_Smith", "John Smith", email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"john smith@example.com", "john.smith.example.com",
            "john.smith@com", "john.smith@example.com1"})
    @DisplayName("Should throw exception if email format is invalid")
    void shouldThrowIfEmailInvalid(String email) {
        assertThrows(IllegalArgumentException.class,
                () -> new User("john_123", "John Smith", email));
    }

    @Test
    @DisplayName("Should return correct formatted string")
    void shouldFormatCorrectly() {
        String username = "John_Smith";
        String fullName = "John Smith";
        String email = "john.smith@example.com";

        User user = new User(username, fullName, email);
        String result = user.format();

        assertEquals("John_Smith (John Smith) <john.smith@example.com>", result);
    }

    @Test
    @DisplayName("Two identical records should be equal")
    void shouldEquals() {
        String username = "John_Smith";
        String fullName = "John Smith";
        String email = "john.smith@example.com";

        User user1 = new User(username, fullName, email);
        User user2 = new User(username, fullName, email);

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
    }
}
