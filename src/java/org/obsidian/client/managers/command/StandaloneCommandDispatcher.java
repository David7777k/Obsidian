package org.obsidian.client.managers.command;

import lombok.AccessLevel;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.managers.command.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class StandaloneCommandDispatcher implements CommandDispatcher, CommandProvider {
    private static final String DELIMITER = " ";
    final Prefix prefix;
    final ParametersFactory parametersFactory;
    final Logger logger;
    final Map<String, Command> aliasToCommandMap;


    public StandaloneCommandDispatcher(List<Command> commands,
                                       AdviceCommandFactory adviceCommandFactory,
                                       Prefix prefix,
                                       ParametersFactory parametersFactory,
                                       Logger logger) {
        this.prefix = prefix;
        this.parametersFactory = parametersFactory;
        this.logger = logger;
        aliasToCommandMap = commandsToAliasToCommandMap(commandsWithAdviceCommand(adviceCommandFactory, commands));
    }

    @Override
    public DispatchResult dispatch(String message) {
        String prefix = this.prefix.get();

        if (!message.startsWith(prefix)) {
            return DispatchResult.NOT_DISPATCHED;
        }

        String[] split = message.split(DELIMITER);
        String commandName = split[0].substring(prefix.length());
        Command command = aliasToCommandMap.get(commandName);

        try {
            String parameters = extractParametersFromMessage(message, split);
            command.execute(parametersFactory.createParameters(parameters, DELIMITER));
        } catch (Exception e) {
            handleCommandException(e, command);
        }
        return DispatchResult.DISPATCHED;
    }


    @Override
    public Command command(String alias) {
        return aliasToCommandMap.get(alias);
    }


    private String extractParametersFromMessage(String message, String[] split) {
        return message.substring((split.length != 1 ? DELIMITER.length() : 0) + split[0].length());
    }

    private Map<String, Command> commandsToAliasToCommandMap(List<Command> commands) {
        return commands.stream().flatMap(this::commandToWrappedCommandStream).collect(Collectors.toMap(FlatMapCommand::getAlias, FlatMapCommand::getCommand));
    }

    private Stream<FlatMapCommand> commandToWrappedCommandStream(Command command) {
        Stream<FlatMapCommand> wrappedCommandStream = Stream.of(new FlatMapCommand(command.name(), command));
        if (command instanceof MultiNamedCommand multiNamedCommand) {
            return Stream.concat(wrappedCommandStream, multiNamedCommand.aliases().stream().map(alias -> new FlatMapCommand(alias, command)));
        }
        return wrappedCommandStream;
    }

    private void handleCommandException(Exception exception, Command command) {
        if (exception instanceof CommandException) {
            logger.log(exception.getMessage());
        } else {
            handleErrorMessage(exception);
        }
        if (command instanceof CommandWithAdvice) {
            logger.log(String.format(TextFormatting.GRAY + "Введите %shelp %s", prefix.get(), command.name()));
        }
    }

    public void handleErrorMessage(Exception exception) {
        logger.log("An error occurred while executing the command!");
        String details = "Error details: ";
        String errorMessage;

        if (exception instanceof NullPointerException) {
            errorMessage = "There is no such command.";
        } else {
            errorMessage = exception.getMessage();
        }

        logger.log(details.concat(errorMessage));
    }


    private List<Command> commandsWithAdviceCommand(AdviceCommandFactory adviceCommandFactory, List<Command> commands) {
        List<Command> commandsWithAdvices = new ArrayList<>(commands);
        commandsWithAdvices.add(adviceCommandFactory.adviceCommand(this));
        return commandsWithAdvices;
    }

    @Value
    private static class FlatMapCommand {
        String alias;
        Command command;
    }
}
