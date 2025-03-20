package org.obsidian.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.chat.ChatUtil;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.PlayerUtil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoLeave", category = Category.PLAYER)
public class AutoLeave extends Module {

    // Настройки - эти поля должны остаться final
    final SliderSetting distance = new SliderSetting(this, "Дистанция", 50.0f, 1.0f, 100.0f, 1.0f);
    final ModeSetting mode = new ModeSetting(this, "Действие", "Кик", "/hub", "/spawn", "/home", "/clan home");
    final BooleanSetting autoDisable = new BooleanSetting(this, "Автоотключение", true);
    final BooleanSetting announcePlayer = new BooleanSetting(this, "Сообщать о игроке", true);
    final BooleanSetting ignoreTeammates = new BooleanSetting(this, "Игнорировать команду", true);
    final BooleanSetting safeMode = new BooleanSetting(this, "Безопасный режим", true);

    // Константы
    private static final long COOLDOWN_TIME = 5000; // 5 секунд кулдаун
    private static final long ERROR_COOLDOWN = 10000; // 10 секунд кулдаун после ошибки
    private static final String UNKNOWN_PLAYER = "неизвестный";

    // Нефинальные поля (состояние) - убрано makeFinal=true из аннотации выше
    private boolean cooldown = false;
    private long lastTriggerTime = 0;
    private final AtomicBoolean processingAction = new AtomicBoolean(false);
    private int errorCount = 0;
    private long lastErrorTime = 0;

    // Singleton паттерн
    public static AutoLeave getInstance() {
        return Instance.get(AutoLeave.class);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        resetState();
        ChatUtil.addText("&aAutoLeave: &fМодуль активирован. Расстояние обнаружения: &e" + distance.getValue() + "&f блоков");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        resetState();
    }

    /**
     * Полный сброс состояния модуля
     */
    private void resetState() {
        cooldown = false;
        lastTriggerTime = 0;
        processingAction.set(false);
        errorCount = 0;
        lastErrorTime = 0;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        // Базовые проверки на null и состояние мира
        if (!isValidGameState()) {
            return;
        }

        // Если предыдущая операция всё ещё выполняется или активен кулдаун
        if (processingAction.get() || isCooldownActive()) {
            return;
        }

        // Пропускаем, если игрок находится в PvP-режиме
        if (PlayerUtil.isPvp()) {
            return;
        }

        // Если было много ошибок, временно замедляем работу модуля
        if (isErrorLimitReached()) {
            return;
        }

        try {
            // Поиск ближайшего опасного игрока
            findNearestThreat().ifPresent(player -> {
                // Устанавливаем флаг обработки действия
                if (processingAction.compareAndSet(false, true)) {
                    try {
                        executeAction(player);
                    } finally {
                        // Гарантированно сбрасываем флаг
                        processingAction.set(false);
                    }
                }
            });
        } catch (Exception e) {
            // Обработка ошибки с инкрементом счетчика
            handleModuleError("Ошибка при поиске игроков: " + e.getMessage());
        }
    }

    /**
     * Проверяет базовое состояние игры
     */
    private boolean isValidGameState() {
        return mc != null && mc.player != null && mc.world != null;
    }

    /**
     * Проверяет, активен ли кулдаун модуля
     */
    private boolean isCooldownActive() {
        if (cooldown && System.currentTimeMillis() - lastTriggerTime > COOLDOWN_TIME) {
            cooldown = false;
        }
        return cooldown;
    }

    /**
     * Проверяет, не превышен ли лимит ошибок
     */
    private boolean isErrorLimitReached() {
        // Сбрасываем счетчик ошибок через некоторое время
        if (errorCount > 0 && System.currentTimeMillis() - lastErrorTime > ERROR_COOLDOWN) {
            errorCount = 0;
        }

        // Если было 3+ ошибки за короткий промежуток времени
        return errorCount >= 3;
    }

    /**
     * Обрабатывает ошибку модуля
     */
    private void handleModuleError(String message) {
        errorCount++;
        lastErrorTime = System.currentTimeMillis();

        if (isValidGameState()) {
            ChatUtil.addText("&cAutoLeave: &f" + message);

            // При слишком большом количестве ошибок отключаем модуль
            if (errorCount >= 5) {
                ChatUtil.addText("&cAutoLeave: &fОбнаружено слишком много ошибок, модуль отключен");
                toggle();
            }
        }

        resetCooldown();
    }

    /**
     * Сбрасывает кулдаун
     */
    private void resetCooldown() {
        cooldown = false;
        lastTriggerTime = 0;
    }

    /**
     * Находит ближайшего игрока в радиусе действия
     */
    private Optional<PlayerEntity> findNearestThreat() {
        if (!isValidGameState()) {
            return Optional.empty();
        }

        try {
            // Безопасное получение списка игроков
            List<PlayerEntity> players = safeGetPlayerList();

            // Фильтрация и сортировка игроков
            return players.stream()
                    .filter(player -> player != null && player != mc.player) // Исключаем себя и null
                    .filter(this::isPlayerThreat) // Фильтруем по условиям угрозы
                    .filter(this::isPlayerInRange) // По дистанции
                    .min(Comparator.comparingDouble(this::getDistanceToPlayer));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Безопасно получает список игроков
     */
    private List<PlayerEntity> safeGetPlayerList() {
        try {
            // Проверяем наличие списка игроков
            if (mc != null && mc.world != null) {
                return mc.world.getPlayers().stream()
                        .map(player -> (PlayerEntity) player)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            // Игнорируем исключение здесь
        }
        return Collections.emptyList();
    }

    /**
     * Проверяет, находится ли игрок в диапазоне действия
     */
    private boolean isPlayerInRange(PlayerEntity player) {
        try {
            return player != null &&
                    mc != null &&
                    mc.player != null &&
                    mc.player.getDistance(player) <= distance.getValue();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Получает расстояние до игрока
     */
    private double getDistanceToPlayer(PlayerEntity player) {
        try {
            if (player != null && mc != null && mc.player != null) {
                return mc.player.getDistance(player);
            }
        } catch (Exception e) {
            // Игнорируем исключение
        }
        return Double.MAX_VALUE;
    }

    /**
     * Проверяет, является ли игрок угрозой
     */
    private boolean isPlayerThreat(PlayerEntity player) {
        if (player == null) {
            return false;
        }

        try {
            // Проверка команды (если включена соответствующая настройка)
            return !ignoreTeammates.getValue() || !isSameTeam(player);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Проверяет, находится ли игрок в той же команде
     */
    private boolean isSameTeam(PlayerEntity player) {
        if (!isValidGameState() || player == null) {
            return false;
        }

        try {
            return mc.player.getTeam() != null &&
                    player.getTeam() != null &&
                    mc.player.getTeam().isSameTeam(player.getTeam());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Выполняет действие при обнаружении угрозы
     */
    private void executeAction(PlayerEntity player) {
        if (!isValidGameState() || player == null) {
            return;
        }

        try {
            // Установка кулдауна
            cooldown = true;
            lastTriggerTime = System.currentTimeMillis();

            // Безопасное получение имени игрока
            String playerName = extractPlayerName(player);

            // Выполнение действия в зависимости от режима
            if ("Кик".equals(mode.getValue())) {
                kickPlayer(playerName);
            } else {
                executeCommand(playerName);
            }

            // Отключение модуля если нужно
            if (autoDisable.getValue()) {
                toggle();
            }
        } catch (Exception e) {
            // Логирование ошибки и безопасное отключение
            handleModuleError("Ошибка при выполнении действия: " + e.getMessage());
        }
    }

    /**
     * Безопасно извлекает имя игрока
     */
    private String extractPlayerName(PlayerEntity player) {
        if (player == null) {
            return UNKNOWN_PLAYER;
        }

        try {
            if (player.getName() != null) {
                String rawName = player.getName().getString();
                return (String) Optional.of(TextFormatting.getTextWithoutFormattingCodes(rawName))
                        .orElse(rawName);
            }
        } catch (Exception e) {
            // Тихий перехват
        }
        return UNKNOWN_PLAYER;
    }

    /**
     * Выполняет кик игрока
     */
    private void kickPlayer(String playerName) {
        if (!isValidGameState() || mc.player.connection == null) {
            return;
        }

        try {
            // Безопасный режим используется для предотвращения возможных ошибок
            if (safeMode.getValue()) {
                // В безопасном режиме используем команды вместо прямого кика
                mc.player.sendChatMessage("/lobby");
                notifyAction(playerName);
                return;
            }

            // Если безопасный режим выключен, используем стандартный метод
            StringBuilder message = new StringBuilder("AutoLeave: вы были кикнуты! ");
            if (announcePlayer.getValue()) {
                message.append("Ливнул от игрока: ").append(playerName);
            }
            mc.player.connection.onDisconnect(new StringTextComponent(message.toString()));
        } catch (Exception e) {
            // Резервное действие при ошибке
            fallbackAction();
            handleModuleError("Не удалось выполнить кик, используется резервное действие");
        }
    }

    /**
     * Резервное действие при ошибках
     */
    private void fallbackAction() {
        if (isValidGameState()) {
            try {
                mc.player.sendChatMessage("/lobby");
            } catch (Exception ex) {
                // Тихий перехват
            }
        }
    }

    /**
     * Выполняет команду телепорта и выводит сообщение
     */
    private void executeCommand(String playerName) {
        if (!isValidGameState()) {
            return;
        }

        try {
            mc.player.sendChatMessage(mode.getValue());
            notifyAction(playerName);
        } catch (Exception e) {
            handleModuleError("Ошибка при выполнении команды: " + e.getMessage());
        }
    }

    /**
     * Показывает сообщение об успешном действии
     */
    private void notifyAction(String playerName) {
        if (isValidGameState()) {
            if (announcePlayer.getValue()) {
                ChatUtil.addText("&cAutoLeave: &fливнул от игрока - &e" + playerName);
            } else {
                ChatUtil.addText("&cAutoLeave: &fвыполнена команда &e" + mode.getValue());
            }
        }
    }
}