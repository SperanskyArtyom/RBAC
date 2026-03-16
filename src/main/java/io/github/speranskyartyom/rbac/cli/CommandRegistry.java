package io.github.speranskyartyom.rbac.cli;

import io.github.speranskyartyom.rbac.cli.commands.AssignmentsCommands;
import io.github.speranskyartyom.rbac.cli.commands.PermissionCommands;
import io.github.speranskyartyom.rbac.cli.commands.RolesCommands;
import io.github.speranskyartyom.rbac.cli.commands.UserCommands;

public class CommandRegistry {
    private final CommandParser parser;

    public CommandRegistry() {
        parser = new CommandParser();
        registerCommands();
    }

    private void registerCommands() {
        UserCommands.registerUserCommands(parser);
        RolesCommands.registerRoleCommands(parser);
        AssignmentsCommands.registerCommands(parser);
        PermissionCommands.registerCommands(parser);
    }

    public CommandParser getParser() {
        return parser;
    }
}
