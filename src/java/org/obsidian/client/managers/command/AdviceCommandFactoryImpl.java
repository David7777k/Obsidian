package org.obsidian.client.managers.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.obsidian.client.managers.command.api.AdviceCommandFactory;
import org.obsidian.client.managers.command.api.CommandProvider;
import org.obsidian.client.managers.command.api.Logger;


@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdviceCommandFactoryImpl implements AdviceCommandFactory {

    final Logger logger;

    @Override
    public AdviceCommand adviceCommand(CommandProvider commandProvider) {
        return new AdviceCommand(commandProvider, logger);
    }
}
