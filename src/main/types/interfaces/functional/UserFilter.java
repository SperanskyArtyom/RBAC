package types.interfaces.functional;

import types.records.User;

@FunctionalInterface
public interface UserFilter {
    boolean test(User user);
}
