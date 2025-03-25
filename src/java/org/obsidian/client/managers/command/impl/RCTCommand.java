package org.obsidian.client.managers.command.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.network.play.client.CPlayerPacket;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.command.api.Command;
import org.obsidian.client.managers.command.api.Logger;
import org.obsidian.client.managers.command.api.MultiNamedCommand;
import org.obsidian.client.managers.command.api.Parameters;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.utils.chat.ChatUtil;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.common.impl.taskript.Script;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RCTCommand extends org.obsidian.client.api.events.Handler implements Command, MultiNamedCommand, IMinecraft {

    // Скрипт для пошагового выполнения команд (TaskRipt)
    final Script script = new Script();

    // Логгер для вывода сообщений в чат/консоль
    final Logger logger;

    // Флаг, чтобы понимать, находимся ли в процессе "перезахода"
    @NonFinal
    boolean reconnecting = false;

    /**
     * Отслеживаем событие Render2D, чтобы "обновлять" скрипт,
     * когда находимся в лобби (Хаб) и хотим снова подключиться на анархию.
     */
    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (reconnecting
                && mc.ingameGUI.overlayPlayerList.header != null
                && mc.ingameGUI.overlayPlayerList.header.getString().contains("Режим: Хаб #")) {
            script.update();
        }
    }

    /**
     * Основной метод команды:
     * - Если передан аргумент (например, .rct 305), заходит на указанную анархию.
     * - Если аргумента нет, берётся номер анархии через PlayerUtil.getAnarchy().
     */
    @Override
    public void execute(Parameters parameters) {
        // Если игрок находится в PvP, не даём перезаходить
        if (PlayerUtil.isPvp()) {
            logger.log("Вы не можете перезаходить на анархию в режиме PVP");
            return;
        }

        // Определяем номер сервера-анархии
        int server;
        Optional<String> argOpt = parameters.asString(0);
        if (argOpt.isPresent()) {
            try {
                server = Integer.parseInt(argOpt.get());
            } catch (NumberFormatException e) {
                logger.log("Неверный номер анархии: " + argOpt.get());
                return;
            }
        } else {
            server = PlayerUtil.getAnarchy();
            if (server == -1) {
                logger.log("Не удалось получить номер анархии.");
                return;
            }
        }

        reconnecting = true;
        // Сначала идём в хаб
        ChatUtil.sendText("/hub");

        // Очищаем скрипт и добавляем шаги
        script.cleanup()
                // Через 1 секунду отправляем команду /an<номер>
                .addStep(1000, () -> ChatUtil.sendText("/an" + server))
                // Через ещё 2 секунды посылаем позиционные пакеты для синхронизации
                .addStep(2000, () -> {
                    for (int i = 0; i < 10; i++) {
                        mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(
                                mc.player.getPosX(),
                                mc.player.getPosY(),
                                mc.player.getPosZ(),
                                mc.player.isOnGround()
                        ));
                    }
                    mc.player.respawnPlayer();
                    for (int i = 0; i < 10; i++) {
                        mc.player.connection.sendPacket(new CPlayerPacket.PositionPacket(
                                mc.player.getPosX(),
                                mc.player.getPosY(),
                                mc.player.getPosZ(),
                                mc.player.isOnGround()
                        ));
                    }
                    ChatUtil.addText("✅ Зашли на анархию: " + server);
                    reconnecting = false;
                });
    }

    @Override
    public String name() {
        return "rct";
    }

    @Override
    public String description() {
        return "Перезаходит на анархию";
    }

    @Override
    public List<String> aliases() {
        return Collections.singletonList("reconnect");
    }
}
