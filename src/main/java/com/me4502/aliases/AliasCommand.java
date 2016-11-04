package com.me4502.aliases;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

class AliasCommand {

    private String alias;

    private String permission;

    private List<String> commands;

    AliasCommand(String alias, String permission, List<String> commands) {
        this.alias = alias;
        this.permission = permission;
        this.commands = commands;
    }

    List<String> getCommands() {
        return this.commands;
    }

    String getPermission() {
        return this.permission;
    }

    Map<String, String> mapToCommand(String command) throws IllegalArgumentException {
        Map<String, String> argumentMap = Maps.newHashMap();

        String[] inputArgs = command.split(" ");
        String[] aliasArgs = alias.split(" ");

        if (inputArgs.length != aliasArgs.length) {
            throw new IllegalArgumentException("Wrong command format. Use " + alias);
        }

        for (int i = 1; i < inputArgs.length; i++) {
            argumentMap.put(aliasArgs[i], inputArgs[i]);
        }

        return argumentMap;
    }
}
