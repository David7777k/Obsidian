//вроде заебись
package org.obsidian.client.managers.module.impl.player;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.input.KeyboardPressEvent;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BindSetting;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.managers.module.settings.impl.StringSetting;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "CordDroper", category = Category.PLAYER)
public class CordDroper extends Module {

    // Привязка клавиши (по умолчанию: 66 = B)
    final BindSetting cordButton = new BindSetting(this, "Кнопка отправки координат", 66);
    // Режим отправки: "Глобальный чат" или "Другу"
    final ModeSetting mode = new ModeSetting(this, "Кому отправлять", "Глобальный чат", "Другу");
    // Настройка для ника друга, видна только при режиме "Другу"
    final StringSetting friendNameSetting = new StringSetting(this, "Ник друга", "Player")
            .setVisible(() -> mode.getValue().equals("Другу"));

    // Статическое поле для хранения ника друга (обновляется в onUpdate)
    public static String friendName = "";

    public CordDroper() {
        addSetting(cordButton);
        addSetting(mode);
        addSetting(friendNameSetting);
    }

    // Метод регистрации настроек (здесь можно добавить свою логику)
    private void addSetting(Object setting) {
        // Например, settings.add((Setting<?>) setting);
    }

    @Override
    protected void onEnable() {
        // Модуль включён – никаких дополнительных сообщений не выводим
    }

    // Обработчик события KeyboardPressEvent
    @EventHandler
    public void onKeyboardPress(KeyboardPressEvent event) {
        if (mc == null || mc.player == null || mc.world == null) return;
        if (event.getKey() == cordButton.getValue()) {
            int x = (int) mc.player.getPosX();
            int y = (int) mc.player.getPosY();
            int z = (int) mc.player.getPosZ();
            if (mode.getValue().equals("Глобальный чат")) {
                mc.player.sendChatMessage("Координаты: " + x + ", " + y + ", " + z);
            } else if (mode.getValue().equals("Другу") && friendName != null && !friendName.isEmpty()) {
                mc.player.sendChatMessage("/msg " + friendName + " Координаты: " + x + ", " + y + ", " + z);
            }
        }
    }

    // Обработчик события обновления (например, игровой тик)
    @EventHandler
    public void onUpdate(UpdateEvent event) {
        friendName = friendNameSetting.getValue();

    }

    // Переопределение метода isStarred для добавления звёздочки
    @Override
    public boolean isStarred() {
        return true; // Звёздочка всегда отображается
    }
}
