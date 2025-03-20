package org.obsidian.client.managers.command.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.command.CommandException;
import org.obsidian.client.managers.command.api.*;
import org.obsidian.client.managers.other.friend.FriendManager;
import org.obsidian.client.utils.player.PlayerUtil;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FriendCommand implements Command, CommandWithAdvice, IMinecraft {

    final FriendManager friendManager;
    final Prefix prefix;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");

        // Show help if no arguments or "help" is provided
        if (commandType.isEmpty() || commandType.equals("help")) {
            for (String line : adviceMessage()) {
                logger.log(line);
            }
            return;
        }

        switch (commandType) {
            case "add" -> addFriend(parameters, logger);
            case "remove" -> removeFriend(parameters, logger);
            case "clear" -> clearFriendList(logger);
            case "list" -> getFriendList(logger);
            default ->
                    throw new CommandException(TextFormatting.RED + "Неверная команда. Используйте " + prefix.get() + "friend help для списка команд.");
        }
    }

    @Override
    public String name() {
        return "friend";
    }

    @Override
    public String description() {
        return "Позволяет управлять списком друзей";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(
                TextFormatting.YELLOW + "Управление списком друзей:",
                TextFormatting.GRAY + "Использование: " + commandPrefix + "friend <команда> [аргументы]",
                TextFormatting.GRAY + "Доступные команды:",
                TextFormatting.GREEN + "  add <name> " + TextFormatting.GRAY + "- Добавить игрока в друзья.",
                TextFormatting.GREEN + "  remove <name> " + TextFormatting.GRAY + "- Удалить игрока из друзей.",
                TextFormatting.GREEN + "  list " + TextFormatting.GRAY + "- Показать список всех друзей.",
                TextFormatting.GREEN + "  clear " + TextFormatting.GRAY + "- Очистить список друзей.",
                TextFormatting.GRAY + "Примеры:",
                TextFormatting.RED + "  " + commandPrefix + "friend add dedinside",
                TextFormatting.RED + "  " + commandPrefix + "friend remove dedinside",
                TextFormatting.RED + "  " + commandPrefix + "friend list"
        );
    }

    private void addFriend(final Parameters parameters, Logger logger) {
        String friendName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите имя друга для добавления!"));

        if (PlayerUtil.isInvalidName(friendName)) {
            logger.log(TextFormatting.RED + "Недопустимое имя.");
            return;
        }

        if (friendName.equalsIgnoreCase(mc.player.getName().getString())) {
            logger.log(TextFormatting.RED + "Вы не можете добавить себя в друзья.");
            return;
        }

        if (friendManager.isFriend(friendName)) {
            logger.log(TextFormatting.RED + "Этот игрок уже находится в вашем списке друзей.");
            return;
        }
        friendManager.addFriend(friendName);
        logger.log(TextFormatting.GRAY + "Вы успешно добавили " + TextFormatting.GRAY + friendName + TextFormatting.GRAY + " в друзья!");
    }

    private void removeFriend(final Parameters parameters, Logger logger) {
        String friendName = parameters.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите имя друга для удаления!"));

        if (friendManager.isFriend(friendName)) {
            friendManager.removeFriend(friendName);
            logger.log(TextFormatting.GRAY + "Вы успешно удалили " + TextFormatting.GRAY + friendName + TextFormatting.GRAY + " из друзей!");
            return;
        }
        logger.log(TextFormatting.RED + friendName + " не найден в списке друзей.");
    }

    private void getFriendList(Logger logger) {
        if (friendManager.isEmpty()) {
            logger.log(TextFormatting.RED + "Список друзей пуст.");
            return;
        }

        logger.log(TextFormatting.GRAY + "Список друзей:");
        for (String friend : friendManager) {
            logger.log(TextFormatting.GRAY + friend);
        }
    }

    private void clearFriendList(Logger logger) {
        if (friendManager.isEmpty()) {
            logger.log(TextFormatting.RED + "Список друзей пуст.");
            return;
        }
        friendManager.clearFriends();
        logger.log(TextFormatting.GRAY + "Список друзей очищен.");
    }
}