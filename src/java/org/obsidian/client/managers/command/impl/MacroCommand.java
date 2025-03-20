package org.obsidian.client.managers.command.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.managers.command.CommandException;
import org.obsidian.client.managers.command.api.*;
import org.obsidian.client.managers.other.macros.MacrosManager;
import org.obsidian.client.utils.keyboard.Keyboard;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MacroCommand implements Command, MultiNamedCommand, CommandWithAdvice {

    final MacrosManager macroManager;
    final Prefix prefix;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElseThrow();
        switch (commandType) {
            case "add" -> addMacro(parameters);
            case "remove" -> removeMacro(parameters);
            case "clear" -> clearMacros();
            case "list" -> printMacrosList();
            default -> throw new CommandException(TextFormatting.RED + "Укажите тип команды: "
                    + TextFormatting.GRAY + "add, remove, clear, list");
        }
    }

    @Override
    public String name() {
        return "macro";
    }

    @Override
    public String description() {
        return "Позволяет управлять макросами";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(
                commandPrefix + "macro add <name> <key> <message> - Добавить новый макрос. "
                        + "Пример: " + TextFormatting.RED + commandPrefix + "macro add home H /home home",
                commandPrefix + "macro remove <name> - Удалить макрос по имени. "
                        + "Пример: " + TextFormatting.RED + commandPrefix + "macro remove home",
                commandPrefix + "macro list - Получить список макросов.",
                commandPrefix + "macro clear - Очистить список макросов."
        );
    }

    @Override
    public List<String> aliases() {
        return List.of("macros");
    }

    private void addMacro(Parameters parameters) {
        String macroName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.GRAY + "Укажите название макроса."));
        String macroKey = parameters.asString(2)
                .orElseThrow(() -> new CommandException(TextFormatting.GRAY + "Укажите клавишу для макроса (например, H)."));

        String macroMessage = parameters.collectMessage(3);

        if (macroMessage.isEmpty()) {
            throw new CommandException(TextFormatting.RED + "Укажите сообщение, которое будет отправлять макрос.");
        }

        int key = Keyboard.keyCode(macroKey);

        checkMacroExist(macroName);

        macroManager.addMacro(macroName, key, macroMessage);

        logger.log(TextFormatting.GREEN + "Добавлен макрос с названием "
                + TextFormatting.RED + macroName
                + TextFormatting.GREEN + ", кнопка: "
                + TextFormatting.RED + macroKey
                + TextFormatting.GREEN + ", сообщение: "
                + TextFormatting.RED + macroMessage);
    }

    private void removeMacro(Parameters parameters) {
        String macroName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.GRAY + "Укажите название макроса."));

        macroManager.removeMacro(macroName);

        logger.log(TextFormatting.GREEN + "Макрос " + TextFormatting.RED + macroName + TextFormatting.GREEN + " успешно удален!");
    }

    private void clearMacros() {
        macroManager.clearMacros();
        logger.log(TextFormatting.GREEN + "Все макросы были удалены.");
    }

    private void printMacrosList() {
        if (macroManager.isEmpty()) {
            logger.log(TextFormatting.RED + "Список макросов пуст.");
            return;
        }
        macroManager.forEach(macro -> logger.log(TextFormatting.WHITE + "Название: "
                + TextFormatting.GRAY + macro.getName()
                + TextFormatting.WHITE + ", Команда: "
                + TextFormatting.GRAY + macro.getMessage()
                + TextFormatting.WHITE + ", Кнопка: "
                + TextFormatting.GRAY + Keyboard.keyName(macro.getKey())));
    }

    private void checkMacroExist(String macroName) {
        if (macroManager.hasMacro(macroName)) {
            throw new CommandException(TextFormatting.RED + "Макрос с таким именем уже существует!");
        }
    }
}
