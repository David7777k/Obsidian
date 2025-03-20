package org.obsidian.client.managers.command.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.obsidian.client.api.client.Constants;
import org.obsidian.client.managers.command.CommandException;
import org.obsidian.client.managers.command.api.*;
import org.obsidian.client.managers.module.impl.client.ClickGui;
import org.obsidian.client.managers.other.config.ConfigFile;
import org.obsidian.client.managers.other.config.ConfigManager;
import org.obsidian.client.utils.keyboard.Keyboard;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ConfigCommand implements Command, CommandWithAdvice, MultiNamedCommand {

    final ConfigManager configManager;
    final Prefix prefix;
    final Logger logger;

    private static final String CONFIG_NOT_FOUND = TextFormatting.RED + "Конфигурация " + TextFormatting.GRAY + "%s" + TextFormatting.RED + " не найдена!";

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");
        configManager.update();

        if (commandType.isEmpty() || commandType.equals("help")) {
            for (String line : adviceMessage()) {
                logger.log(line);
            }
            return;
        }

        if (commandType.equals("confirmdelete")) {
            String configName = parameters.asString(1)
                    .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название конфига для удаления!"));
            deleteConfigConfirmed(configName);
            return;
        }

        if (commandType.equals("confirmclear")) {
            clearAllConfigsConfirmed();
            return;
        }

        if (commandType.equals("canceldelete") || commandType.equals("cancelclear")) {
            cancelDelete();
            return;
        }

        switch (commandType) {
            case "load" -> loadConfig(parameters);
            case "save" -> saveConfig(parameters);
            case "list" -> configList();
            case "dir" -> getDirectory();
            case "delete" -> deleteConfig(parameters);
            case "clear" -> clearAllConfigs();
            default ->
                    throw new CommandException(TextFormatting.RED + "Неверная команда. Используйте " + prefix.get() + "config help для списка команд.");
        }
    }

    @Override
    public String name() {
        return "config";
    }

    @Override
    public String description() {
        return "Позволяет взаимодействовать с конфигами в чите";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(
                TextFormatting.YELLOW + "Управление конфигурациями:",
                TextFormatting.GRAY + "Использование: " + commandPrefix + "config <команда> [аргументы]",
                TextFormatting.GRAY + "Доступные команды:",
                TextFormatting.GREEN + "  load <config> " + TextFormatting.GRAY + "- Загрузить указанную конфигурацию.",
                TextFormatting.GREEN + "  save <config> " + TextFormatting.GRAY + "- Сохранить текущие настройки в указанную конфигурацию.",
                TextFormatting.GREEN + "  list " + TextFormatting.GRAY + "- Показать список всех доступных конфигураций.",
                TextFormatting.GREEN + "  dir " + TextFormatting.GRAY + "- Открыть папку, где хранятся конфигурации.",
                TextFormatting.GREEN + "  delete <config> " + TextFormatting.GRAY + "- Удалить указанную конфигурацию (требуется подтверждение).",
                TextFormatting.GREEN + "  clear " + TextFormatting.GRAY + "- Удалить все конфигурации (требуется подтверждение).",
                TextFormatting.GRAY + "Примеры:",
                TextFormatting.RED + "  " + commandPrefix + "config save myConfig",
                TextFormatting.RED + "  " + commandPrefix + "config load myConfig",
                TextFormatting.RED + "  " + commandPrefix + "config delete myConfig",
                TextFormatting.GRAY + "Команда также доступна под алиасом 'cfg', например, " + commandPrefix + "cfg load myConfig"
        );
    }

    @Override
    public List<String> aliases() {
        return List.of("cfg");
    }

    private void loadConfig(Parameters parameters) {
        String configName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название конфига для загрузки!"));

        File configFile = new File(ConfigManager.CONFIG_DIRECTORY, configName + Constants.FILE_FORMAT);
        if (!configFile.exists()) {
            logger.log(String.format(CONFIG_NOT_FOUND, configName));
            return;
        }

        ConfigFile config = configManager.get(configName);
        if (config == null) {
            logger.log(String.format(CONFIG_NOT_FOUND, configName));
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                if (config.read()) {
                    configManager.set();
                    logger.log(TextFormatting.GREEN + "Конфигурация " + TextFormatting.RED + configName + TextFormatting.YELLOW + " загружена!");
                    if (ClickGui.getInstance().getKey() == Keyboard.KEY_NONE.getKey()) {
                        ClickGui.getInstance().setKey(Keyboard.KEY_RIGHT_SHIFT.getKey());
                    }
                } else {
                    logger.log(TextFormatting.RED + "Не удалось загрузить конфигурацию " + TextFormatting.GRAY + configName + TextFormatting.RED + ": неверный формат");
                }
            } catch (Exception e) {
                logger.log(TextFormatting.RED + "Ошибка при загрузке конфигурации " + TextFormatting.GRAY + configName + ": " + e.getMessage());
            }
        });
    }

    private void saveConfig(Parameters parameters) {
        String configName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название конфига для сохранения!"));

        CompletableFuture.runAsync(() -> {
            try {
                configManager.set(configName);
                configManager.set();
                logger.log(TextFormatting.GREEN + "Конфигурация " + TextFormatting.RED + configName + TextFormatting.GREEN + " сохранена!");
            } catch (Exception e) {
                logger.log(TextFormatting.RED + "Ошибка при сохранении конфигурации " + TextFormatting.GRAY + configName + ": " + e.getMessage());
            }
        });
    }

    private void configList() {
        if (configManager.isEmpty()) {
            logger.log(TextFormatting.RED + "Список конфигураций пустой");
            return;
        }
        logger.log(TextFormatting.GRAY + "Список конфигов:");

        configManager.update();
        Set<String> uniqueConfigs = new HashSet<>();

        configManager.forEach(configFile -> {
            String configName = configFile.getFile().getName().replace(Constants.FILE_FORMAT, "");
            if (!uniqueConfigs.add(configName)) {
                return;
            }

            String configCommand = prefix.get() + "config load " + configName;
            StringTextComponent chatText = new StringTextComponent(TextFormatting.GREEN + "> " + configName);
            StringTextComponent hoverText = new StringTextComponent(TextFormatting.RED + "Конфиг: " + configName);

            chatText.setStyle(Style.EMPTY
                    .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, configCommand))
                    .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)));

            Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(chatText);
        });
    }

    private void getDirectory() {
        Util.getOSType().openFile(ConfigManager.CONFIG_DIRECTORY);
        logger.log(TextFormatting.RED + "Папка с конфигами открыта");
    }

    private void deleteConfig(Parameters parameters) {
        String configName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите название конфига для удаления!"));

        File configFile = new File(ConfigManager.CONFIG_DIRECTORY, configName + Constants.FILE_FORMAT);
        if (!configFile.exists()) {
            logger.log(String.format(CONFIG_NOT_FOUND, configName));
            return;
        }

        StringTextComponent confirmMessage = new StringTextComponent(
                TextFormatting.YELLOW + "Вы уверены, что хотите удалить конфиг " +
                        TextFormatting.RED + configName + TextFormatting.YELLOW + "? ");

        String confirmCommand = prefix.get() + "config confirmdelete " + configName;
        String cancelCommand = prefix.get() + "config canceldelete";

        StringTextComponent confirmButton = new StringTextComponent(TextFormatting.GREEN + "[Подтвердить]");
        confirmButton.setStyle(Style.EMPTY
                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, confirmCommand))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new StringTextComponent(TextFormatting.RED + "Удалить конфиг " + configName))));

        StringTextComponent cancelButton = new StringTextComponent(TextFormatting.RED + " [Отмена]");
        cancelButton.setStyle(Style.EMPTY
                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cancelCommand))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new StringTextComponent(TextFormatting.GREEN + "Отменить удаление"))));

        confirmMessage.append(confirmButton).append(cancelButton);
        Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(confirmMessage);
    }

    private void deleteConfigConfirmed(String configName) {
        File configFile = new File(ConfigManager.CONFIG_DIRECTORY, configName + Constants.FILE_FORMAT);
        if (!configFile.exists()) {
            logger.log(String.format(CONFIG_NOT_FOUND, configName));
            return;
        }

        try {
            if (configFile.delete()) {
                configManager.update();
                logger.log(TextFormatting.GREEN + "Конфигурация " + TextFormatting.RED + configName +
                        TextFormatting.GREEN + " успешно удалена!");
            } else {
                logger.log(TextFormatting.RED + "Ошибка при удалении конфигурации " +
                        TextFormatting.GRAY + configName);
            }
        } catch (SecurityException e) {
            logger.log(TextFormatting.RED + "Нет доступа для удаления конфигурации " +
                    TextFormatting.GRAY + configName + ": " + e.getMessage());
        }
    }

    private void clearAllConfigs() {
        if (configManager.isEmpty()) {
            logger.log(TextFormatting.RED + "Список конфигураций пустой");
            return;
        }

        StringTextComponent confirmMessage = new StringTextComponent(
                TextFormatting.YELLOW + "Вы уверены, что хотите удалить ВСЕ конфиги? " +
                        TextFormatting.RED + "Это действие нельзя отменить!");

        String confirmCommand = prefix.get() + "config confirmclear";
        String cancelCommand = prefix.get() + "config cancelclear";

        StringTextComponent confirmButton = new StringTextComponent(TextFormatting.GREEN + " [Подтвердить]");
        confirmButton.setStyle(Style.EMPTY
                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, confirmCommand))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new StringTextComponent(TextFormatting.RED + "Удалить ВСЕ конфиги"))));

        StringTextComponent cancelButton = new StringTextComponent(TextFormatting.RED + " [Отмена]");
        cancelButton.setStyle(Style.EMPTY
                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, cancelCommand))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new StringTextComponent(TextFormatting.GREEN + "Отменить удаление"))));

        confirmMessage.append(confirmButton).append(cancelButton);
        Minecraft.getInstance().ingameGUI.getChatGUI().printChatMessage(confirmMessage);
    }

    private void clearAllConfigsConfirmed() {
        File[] configFiles = ConfigManager.CONFIG_DIRECTORY.listFiles((dir, name) -> name.endsWith(Constants.FILE_FORMAT));
        if (configFiles == null || configFiles.length == 0) {
            logger.log(TextFormatting.RED + "Нет конфигураций для удаления");
            return;
        }

        int deletedCount = 0;
        for (File file : configFiles) {
            try {
                if (file.delete()) {
                    deletedCount++;
                }
            } catch (SecurityException e) {
                logger.log(TextFormatting.RED + "Нет доступа для удаления " +
                        TextFormatting.GRAY + file.getName() + ": " + e.getMessage());
            }
        }

        if (deletedCount > 0) {
            configManager.update();
            logger.log(TextFormatting.GREEN + "Успешно удалено " +
                    TextFormatting.RED + deletedCount + TextFormatting.GREEN + " конфигов!");
        } else {
            logger.log(TextFormatting.RED + "Не удалось удалить конфигурации");
        }
    }

    private void cancelDelete() {
        logger.log(TextFormatting.GREEN + "Операция удаления отменена");
    }
}