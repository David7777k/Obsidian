package org.obsidian.client.managers.command.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.Obsidian;
import org.obsidian.client.managers.command.CommandException;
import org.obsidian.client.managers.command.api.*;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.impl.client.ClickGui;
import org.obsidian.client.managers.module.settings.Setting;
import org.obsidian.client.managers.module.settings.impl.BindSetting;
import org.obsidian.client.utils.keyboard.Keyboard;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BindCommand implements Command, CommandWithAdvice {

    final Prefix prefix;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");

        if (commandType.isEmpty() || commandType.equals("help")) {
            for (String line : adviceMessage()) {
                logger.log(line);
            }
            return;
        }

        switch (commandType) {
            case "add" -> addBindToModule(parameters, logger);
            case "remove" -> removeBindFromModule(parameters, logger);
            case "clear" -> clearAllBindings(logger);
            case "list" -> listBoundKeys(logger);
            default ->
                    throw new CommandException(TextFormatting.RED + "Неверная команда. Используйте " + prefix.get() + "bind help для списка команд.");
        }
    }

    @Override
    public String name() {
        return "bind";
    }

    @Override
    public String description() {
        return "Позволяет забиндить функцию на определенную клавишу";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(
                TextFormatting.YELLOW + "Управление биндами:",
                TextFormatting.GRAY + "Использование: " + commandPrefix + "bind <команда> [аргументы]",
                TextFormatting.GRAY + "Доступные команды:",
                TextFormatting.GREEN + "  add <function> <key> " + TextFormatting.GRAY + "- Привязать клавишу к модулю.",
                TextFormatting.GREEN + "  remove <function> <key> " + TextFormatting.GRAY + "- Отвязать клавишу от модуля.",
                TextFormatting.GREEN + "  list " + TextFormatting.GRAY + "- Показать список всех модулей с привязанными клавишами.",
                TextFormatting.GREEN + "  clear " + TextFormatting.GRAY + "- Отвязать все клавиши от всех модулей.",
                TextFormatting.GRAY + "Примеры:",
                TextFormatting.RED + "  " + commandPrefix + "bind add KillAura R",
                TextFormatting.RED + "  " + commandPrefix + "bind remove KillAura R",
                TextFormatting.RED + "  " + commandPrefix + "bind list",
                TextFormatting.GRAY + "Примечание: <key> - это название клавиши, например, 'R', 'SPACE', 'LSHIFT' и т.д."
        );
    }

    private void addBindToModule(Parameters parameters, Logger logger) {
        String functionName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название модуля для привязки!"));
        String keyName = parameters.asString(2)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите клавишу для привязки!"));

        Module module = null;

        for (Module func : Obsidian.inst().moduleManager().values()) {
            if (func.getName().toLowerCase(Locale.ROOT).equals(functionName.toLowerCase(Locale.ROOT))) {
                module = func;
                break;
            }
        }

        int key = Keyboard.keyCode(keyName.toUpperCase());

        if (module == null) {
            logger.log(TextFormatting.RED + "Модуль " + functionName + " не был найден");
            return;
        }

        module.setKey(key);
        logger.log(TextFormatting.GREEN + "Бинд " + TextFormatting.RED
                + keyName.toUpperCase() + TextFormatting.GREEN
                + " был установлен для модуля " + TextFormatting.RED + functionName);
    }

    private void removeBindFromModule(Parameters parameters, Logger logger) {
        String moduleName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название модуля для отвязки!"));
        String keyName = parameters.asString(2)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите клавишу для отвязки!"));

        Obsidian.inst().moduleManager().values().stream()
                .filter(module -> module.getName().equalsIgnoreCase(moduleName))
                .forEach(module -> {
                    module.setKey(Keyboard.KEY_NONE.getKey());
                    logger.log(TextFormatting.GREEN + "Клавиша " + TextFormatting.RED + keyName.toUpperCase()
                            + TextFormatting.GREEN + " была отвязана от модуля " + TextFormatting.RED + module.getName());
                });
    }

    private void clearAllBindings(Logger logger) {
        Obsidian.inst().moduleManager().values().forEach(module -> {
            if (!(module instanceof ClickGui)) {
                module.setKey(Keyboard.KEY_NONE.getKey());
                for (Setting<?> setting : module.getSettings()) {
                    if (setting instanceof BindSetting bindSetting) {
                        bindSetting.set(Keyboard.KEY_NONE.getKey());
                    }
                }
            }
        });
        logger.log(TextFormatting.GREEN + "Все клавиши были отвязаны от модулей");
    }

    private void listBoundKeys(Logger logger) {
        List<Module> boundModules = Obsidian.inst().moduleManager().values().stream()
                .filter(module -> module.getKey() != Keyboard.KEY_NONE.getKey())
                .toList();

        if (boundModules.isEmpty()) {
            logger.log(TextFormatting.RED + "Нет модулей с привязанными клавишами");
            return;
        }

        logger.log(TextFormatting.GRAY + "Список всех модулей с привязанными клавишами:");
        boundModules.forEach(module -> {
            String keyName = Keyboard.keyName(module.getKey());
            keyName = keyName != null ? keyName : "";
            logger.log(String.format("%s [%s%s%s]", module.getName(), TextFormatting.GRAY, keyName, TextFormatting.WHITE));
        });
    }
}