package types.interfaces.functional;

import types.records.User;

@FunctionalInterface
public interface UserFilter {
    boolean test(User user);

    default UserFilter and(UserFilter other) {
        if (other == null) {
            throw new IllegalArgumentException("Other filter must not be null");
        }

        return user -> test(user) && other.test(user);
    }

    default UserFilter or(UserFilter other) {
        if (other == null) {
            throw new IllegalArgumentException("Other filter must not be null");
        }

        return user -> test(user) || other.test(user);
    }
}
