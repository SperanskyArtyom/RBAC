package types.managers;

import types.interfaces.functional.UserFilter;
import types.records.User;

import java.util.*;

public class UserManager implements Repository<User> {
    private final Map<String, User> users = new HashMap<>();

    @Override
    public void add(User item) {
        Objects.requireNonNull(item, "User must not be null");
        if (exists(item.username())) {
            throw new IllegalArgumentException("User with username - " + item.username() + " already exists");
        }
        users.put(item.username(), item);
    }

    @Override
    public boolean remove(User item) {
        if (item == null) return false;
        return users.remove(item.username()) != null;
    }

    @Override
    public Optional<User> findById(String id) {
        return findByUsername(id);
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public int count() {
        return users.size();
    }

    @Override
    public void clear() {
        users.clear();
    }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(users.get(username));
    }

    public Optional<User> findByEmail(String email) {
        return users.values().stream()
                .filter(user -> user.email().equals(email))
                .findFirst();
    }

    public List<User> findByFilter(UserFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter must not be null");
        }

        return users.values().stream()
                .filter(filter::test)
                .toList();
    }

    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        if (filter == null) {
            throw new IllegalArgumentException("Filter must not be null");
        }
        if (sorter == null) {
            throw new IllegalArgumentException("Sorter must not be null");
        }

        return users.values().stream()
                .filter(filter::test)
                .sorted(sorter)
                .toList();
    }

    public boolean exists(String username) {
        return users.containsKey(username);
    }

    public void update(String username, String newFullName, String newEmail) {
        if (!exists(username)) {
            throw new IllegalArgumentException("User with username - " + username + " doesn't exists");
        }
        users.replace(username, new User(username, newFullName, newEmail));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserManager that)) return false;
        return Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(users);
    }
}
