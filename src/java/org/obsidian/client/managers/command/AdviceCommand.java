package org.obsidian.client.managers.command;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.managers.command.api.*;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdviceCommand implements Command {

    final CommandProvider commandProvider;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String commandName = parameters.asString(0)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Ошибка: Вы не указали имя команды."));

        if (commandName.isEmpty()) {
            throw new CommandException(TextFormatting.RED + "Ошибка: Имя команды не может быть пустым.");
        }

        Command command = commandProvider.command(commandName);

        if (!(command instanceof CommandWithAdvice commandWithAdvice)) {
            throw new CommandException(TextFormatting.RED + "К данной команде не предусмотрены советы!");
        }

        logger.log(TextFormatting.GREEN + "Пример использования команды \""
                + TextFormatting.RED + commandName + TextFormatting.GREEN + "\":");

        for (String advice : commandWithAdvice.adviceMessage()) {
            logger.log(TextFormatting.GRAY + "- " + advice);
        }
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String description() {
        return "Предоставляет советы по использованию других команд.";
    }
}
