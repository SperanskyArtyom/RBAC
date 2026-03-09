package types;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import types.classes.PermanentAssignment;
import types.classes.Role;
import types.interfaces.RoleAssignment;
import types.records.User;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RBACSystemTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
    }

    @Test
    @DisplayName("Constructor should initialize managers and set currentUser to null")
    void constructorShouldInitializeManagers() {
        assertNotNull(system.getUserManager());
        assertNotNull(system.getRoleManager());
        assertNotNull(system.getAssignmentManager());
        assertNull(system.getCurrentUser());
    }

    @Test
    @DisplayName("setCurrentUser should update currentUser when user exists")
    void setCurrentUserShouldUpdateForExistingUser() {
        User testUser = new User("testUser", "Test User", "test@example.com");
        system.getUserManager().add(testUser);

        system.setCurrentUser(testUser.username());

        assertEquals(testUser.username(), system.getCurrentUser());
    }

    @Test
    @DisplayName("setCurrentUser should throw exception for non-existent user")
    void setCurrentUserShouldThrowForNonExistentUser() {
        String nonExistent = "unknown";

        assertThrows(IllegalArgumentException.class,
                () -> system.setCurrentUser(nonExistent),
                "User does not exist: " + nonExistent);
    }

    @Test
    @DisplayName("initialize should set currentUser to admin")
    void initializeShouldSetCurrentUser() {
        system.initialize();
        assertEquals("admin", system.getCurrentUser());
    }

    @Test
    @DisplayName("initialize should create admin user with correct data")
    void initializeShouldCreateAdminUser() {
        system.initialize();
        Optional<User> admin = system.getUserManager().findByUsername("admin");
        assertTrue(admin.isPresent());
        assertEquals("System administrator", admin.get().fullName());
        assertEquals("admin@system.local", admin.get().email());
    }

    @Test
    @DisplayName("initialize should create default roles: Admin, Manager, Viewer")
    void initializeShouldCreateDefaultRoles() {
        system.initialize();
        assertTrue(system.getRoleManager().exists("Admin"));
        assertTrue(system.getRoleManager().exists("Manager"));
        assertTrue(system.getRoleManager().exists("Viewer"));
    }

    @Test
    @DisplayName("initialize should assign Admin role to admin user")
    void initializeShouldAssignAdminRoleToAdmin() {
        system.initialize();
        User admin = system.getUserManager().findByUsername("admin").orElseThrow();
        Role adminRole = system.getRoleManager().findByName("Admin").orElseThrow();
        List<RoleAssignment> assignments = system.getAssignmentManager().findByUser(admin);
        assertEquals(1, assignments.size());
        assertEquals(adminRole, assignments.getFirst().role());
        assertInstanceOf(PermanentAssignment.class, assignments.getFirst());
    }

    @Test
    @DisplayName("initialize should create exactly one active assignment and no expired ones")
    void initializeShouldCreateOneActiveAssignment() {
        system.initialize();
        assertEquals(1, system.getAssignmentManager().getActiveAssignments().size());
        assertEquals(0, system.getAssignmentManager().getExpiredAssignments().size());
    }

    @Test
    @DisplayName("generateStatistics after initialize should return correctly formatted statistics")
    void generateStatisticsAfterInitializeShouldReturnCorrectString() {
        system.initialize();

        String stats = system.generateStatistics();

        String expected = String.format("""
                        System statistics
                            Users: %d
                            Roles: %d
                            Assignments: TOTAL %d, ACTIVE %d, EXPIRED %d
                            Average number of roles per user: %.2f
                            Top roles: %s
                        """,
                1,                     // users
                3,                     // roles (Admin, Manager, Viewer)
                1,                     // total assignments
                1,                     // active assignments
                0,                     // expired assignments
                1.0,                   // avg roles per user
                "Admin"                 // top roles
        );

        assertEquals(expected, stats);
    }

    @Test
    @DisplayName("getCurrentUser should return null before any set")
    void getCurrentUserInitiallyNull() {
        assertNull(system.getCurrentUser());
    }
}