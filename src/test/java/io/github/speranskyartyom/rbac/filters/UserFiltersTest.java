package io.github.speranskyartyom.rbac.filters;

import io.github.speranskyartyom.rbac.interfaces.functional.UserFilter;
import io.github.speranskyartyom.rbac.models.records.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserFiltersTest {

    private final User user =
            new User("john_doe", "John Doe", "john.doe@company.com");

    @Test
    void byUsername_exactMatch_true() {
        UserFilter filter = UserFilters.byUsername("john_doe");

        assertTrue(filter.test(user));
    }

    @Test
    void byUsername_exactMatch_false() {
        UserFilter filter = UserFilters.byUsername("admin");

        assertFalse(filter.test(user));
    }

    @Test
    void byUsernameContains_ignoreCase_true() {
        UserFilter filter = UserFilters.byUsernameContains("JOHN");

        assertTrue(filter.test(user));
    }

    @Test
    void byUsernameContains_ignoreCase_false() {
        UserFilter filter = UserFilters.byUsernameContains("admin");

        assertFalse(filter.test(user));
    }

    @Test
    void byEmail_exactMatch_true() {
        UserFilter filter = UserFilters.byEmail("john.doe@company.com");

        assertTrue(filter.test(user));
    }

    @Test
    void byEmail_exactMatch_false() {
        UserFilter filter = UserFilters.byEmail("john@other.com");

        assertFalse(filter.test(user));
    }

    @Test
    void byEmailDomain_match_true() {
        UserFilter filter = UserFilters.byEmailDomain("company.com");

        assertTrue(filter.test(user));
    }

    @Test
    void byEmailDomain_match_false() {
        UserFilter filter = UserFilters.byEmailDomain("gmail.com");

        assertFalse(filter.test(user));
    }

    @Test
    void byFullNameContains_ignoreCase_true() {
        UserFilter filter = UserFilters.byFullNameContains("john");

        assertTrue(filter.test(user));
    }

    @Test
    void byFullNameContains_ignoreCase_false() {
        UserFilter filter = UserFilters.byFullNameContains("admin");

        assertFalse(filter.test(user));
    }

    @Test
    void and_shouldReturnTrue_whenBothMatch() {
        UserFilter filter =
                UserFilters.byUsername("john_doe")
                        .and(UserFilters.byEmailDomain("company.com"));

        assertTrue(filter.test(user));
    }

    @Test
    void and_shouldReturnFalse_whenOneDoesNotMatch() {
        UserFilter filter =
                UserFilters.byUsername("john_doe")
                        .and(UserFilters.byEmailDomain("@gmail.com"));

        assertFalse(filter.test(user));
    }

    @Test
    void or_shouldReturnTrue_whenOneMatches() {
        UserFilter filter =
                UserFilters.byUsername("admin")
                        .or(UserFilters.byEmailDomain("company.com"));

        assertTrue(filter.test(user));
    }

    @Test
    void or_shouldReturnFalse_whenNoneMatch() {
        UserFilter filter =
                UserFilters.byUsername("admin")
                        .or(UserFilters.byEmailDomain("@gmail.com"));

        assertFalse(filter.test(user));
    }
}