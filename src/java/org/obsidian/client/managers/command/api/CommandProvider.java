package org.obsidian.client.managers.command.api;

public interface CommandProvider {
    Command command(String alias);
}
