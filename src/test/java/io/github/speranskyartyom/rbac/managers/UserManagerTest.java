package io.github.speranskyartyom.rbac.managers;

import io.github.speranskyartyom.rbac.models.records.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserManagerTest {

    private UserManager userManager;

    @BeforeEach
    void setUp() {
        userManager = new UserManager();
    }

    @Test
    void add_shouldAddUser() {
        User user = new User("john", "John Doe", "john@mail.com");

        userManager.add(user);

        assertEquals(1, userManager.count());
        assertTrue(userManager.exists("john"));
    }

    @Test
    void add_duplicateUsername_shouldThrow() {
        User user = new User("john", "John Doe", "john@mail.com");

        userManager.add(user);

        assertThrows(IllegalArgumentException.class,
                () -> userManager.add(user));
    }

    @Test
    void findByUsername_existingUser() {
        User user = new User("john", "John Doe", "john@mail.com");
        userManager.add(user);

        assertTrue(userManager.findByUsername("john").isPresent());
    }

    @Test
    void findByEmail_existingUser() {
        User user = new User("john", "John Doe", "john@mail.com");
        userManager.add(user);

        assertTrue(userManager.findByEmail("john@mail.com").isPresent());
    }

    @Test
    void update_existingUser_shouldUpdateData() {
        User user = new User("john", "John Doe", "john@mail.com");
        userManager.add(user);

        userManager.update("john", "Johnny", "new@mail.com");

        User updated = userManager.findByUsername("john").orElseThrow();
        assertEquals("Johnny", updated.fullName());
        assertEquals("new@mail.com", updated.email());
    }

    @Test
    void update_nonExistingUser_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> userManager.update("ghost", "X", "x@mail.com"));
    }

    @Test
    void findAll_withFilterAndSorter() {
        userManager.add(new User("aaa", "A", "a@mail.com"));
        userManager.add(new User("bbb", "B", "b@mail.com"));

        List<User> result = userManager.findAll(
                _ -> true,
                Comparator.comparing(User::username)
        );

        assertEquals("aaa", result.get(0).username());
        assertEquals("bbb", result.get(1).username());
    }
}