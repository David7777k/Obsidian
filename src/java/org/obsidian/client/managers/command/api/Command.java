package org.obsidian.client.managers.command.api;

public interface Command {
    void execute(Parameters parameters);

    String name();

    String description();
}
