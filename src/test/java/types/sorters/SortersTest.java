package types.sorters;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import types.classes.Role;
import types.classes.TemporaryAssignment;
import types.interfaces.RoleAssignment;
import types.records.AssignmentMetadata;
import types.records.User;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SortersTest {

    @Test
    @Tag("UserSorters")
    void byUsername_sortsCorrectly() {
        List<User> users = new ArrayList<>();
        users.add(new User("zeta", "Zeta User", "zeta@mail.com"));
        users.add(new User("alpha", "Alpha User", "alpha@mail.com"));

        users.sort(UserSorters.byUsername());

        assertEquals("alpha", users.get(0).username());
        assertEquals("zeta", users.get(1).username());
    }

    @Test
    @Tag("UserSorters")
    void byFullName_sortsCorrectly() {
        List<User> users = new ArrayList<>();
        users.add(new User("user1", "Zeta User", "user1@mail.com"));
        users.add(new User("user2", "Alpha User", "user2@mail.com"));

        users.sort(UserSorters.byFullName());

        assertEquals("Alpha User", users.get(0).fullName());
        assertEquals("Zeta User", users.get(1).fullName());
    }

    @Test
    @Tag("UserSorters")
    void byEmail_sortsCorrectly() {
        List<User> users = new ArrayList<>();
        users.add(new User("user1", "User One", "zeta@mail.com"));
        users.add(new User("user2", "User Two", "alpha@mail.com"));

        users.sort(UserSorters.byEmail());

        assertEquals("alpha@mail.com", users.get(0).email());
        assertEquals("zeta@mail.com", users.get(1).email());
    }

    @Test
    @Tag("RoleSorters")
    void byName_sortsCorrectly() {
        List<Role> roles = new ArrayList<>();
        roles.add(new Role("Zeta", "desc1"));
        roles.add(new Role("Alpha", "desc2"));

        roles.sort(RoleSorters.byName());

        assertEquals("Alpha", roles.get(0).getName());
        assertEquals("Zeta", roles.get(1).getName());
    }

    @Test
    @Tag("RoleSorters")
    void byPermissionCount_sortsCorrectly() {
        Role r1 = new Role("Role1", "desc1");
        Role r2 = new Role("Role2", "desc2");

        r1.addPermission(new types.records.Permission("READ", "users", "desc"));
        r1.addPermission(new types.records.Permission("WRITE", "users", "desc"));

        r2.addPermission(new types.records.Permission("READ", "users", "desc"));

        List<Role> roles = new ArrayList<>();
        roles.add(r1);
        roles.add(r2);

        roles.sort(RoleSorters.byPermissionCount());

        assertEquals(r2, roles.get(0)); // меньше прав — первая
        assertEquals(r1, roles.get(1)); // больше прав — последняя
    }

    @Test
    @Tag("AssignmentSorters")
    void Assignment_byUsername_sortsCorrectly() {
        User u1 = new User("bob", "Bob", "bob@mail.com");
        User u2 = new User("Alice", "Alice", "alice@mail.com");

        Role role = new Role("Admin", "desc");

        RoleAssignment a1 = new TemporaryAssignment(
                u1, role,
                new AssignmentMetadata(
                        "admin",
                        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        "reason"),
                OffsetDateTime.now().plusDays(1).toString(),
                false
        );

        RoleAssignment a2 = new TemporaryAssignment(
                u2, role,
                new AssignmentMetadata(
                        "admin",
                        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        "reason"),
                OffsetDateTime.now().plusDays(2).toString(),
                false
        );

        List<RoleAssignment> assignments = new ArrayList<>();
        assignments.add(a1);
        assignments.add(a2);

        assignments.sort(AssignmentSorters.byUsername());

        assertEquals("Alice", assignments.get(0).user().username());
        assertEquals("bob", assignments.get(1).user().username());
    }

    @Test
    @Tag("AssignmentSorters")
    void byRoleName_sortsCorrectly() {
        User user = new User("user", "User", "user@mail.com");

        Role r1 = new Role("Admin", "desc");
        Role r2 = new Role("Manager", "desc");

        RoleAssignment a1 = new TemporaryAssignment(
                user, r2,
                new AssignmentMetadata("admin",
                        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        "reason"),
                OffsetDateTime.now().plusDays(1).toString(),
                false
        );

        RoleAssignment a2 = new TemporaryAssignment(
                user, r1,
                new AssignmentMetadata("admin",
                        OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        "reason"),
                OffsetDateTime.now().plusDays(2).toString(),
                false
        );

        List<RoleAssignment> assignments = new ArrayList<>();
        assignments.add(a1);
        assignments.add(a2);

        assignments.sort(AssignmentSorters.byRoleName());

        assertEquals("Admin", assignments.get(0).role().getName());
        assertEquals("Manager", assignments.get(1).role().getName());
    }

    @Test
    @Tag("AssignmentSorters")
    void byAssignmentDate_sortsCorrectly() {
        User user = new User("user", "User", "user@mail.com");
        Role role = new Role("Admin", "desc");

        String date1 = OffsetDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String date2 = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        RoleAssignment a1 = new TemporaryAssignment(
                user, role,
                new AssignmentMetadata("admin", date2, "reason"),
                OffsetDateTime.now().plusDays(1).toString(),
                false
        );

        RoleAssignment a2 = new TemporaryAssignment(
                user, role,
                new AssignmentMetadata("admin", date1, "reason"),
                OffsetDateTime.now().plusDays(2).toString(),
                false
        );

        List<RoleAssignment> assignments = new ArrayList<>();
        assignments.add(a1);
        assignments.add(a2);

        assignments.sort(AssignmentSorters.byAssignmentDate());

        assertEquals(date1, assignments.get(0).metadata().assignedAt());
        assertEquals(date2, assignments.get(1).metadata().assignedAt());
    }
}