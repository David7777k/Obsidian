package org.obsidian.client.managers.command.api;


import org.obsidian.client.managers.command.AdviceCommand;

public interface AdviceCommandFactory {
    AdviceCommand adviceCommand(CommandProvider commandProvider);
}
