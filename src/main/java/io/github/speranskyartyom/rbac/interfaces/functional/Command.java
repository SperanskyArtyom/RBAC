package io.github.speranskyartyom.rbac.interfaces.functional;

import io.github.speranskyartyom.rbac.core.RBACSystem;

import java.util.Scanner;

@FunctionalInterface
public interface Command {
    void execute(Scanner scanner, RBACSystem system, boolean haveArgs);
}
