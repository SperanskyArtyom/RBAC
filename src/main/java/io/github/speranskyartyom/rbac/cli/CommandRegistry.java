package io.github.speranskyartyom.rbac.cli;

import io.github.speranskyartyom.rbac.cli.commands.*;

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
        UtilityCommands.registerCommands(parser);
    }

    public CommandParser getParser() {
        return parser;
    }
}
