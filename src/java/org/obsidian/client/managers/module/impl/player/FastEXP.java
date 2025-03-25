package org.obsidian.client.managers.module.impl.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.TickEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "FastEXP", category = Category.PLAYER)
public class FastEXP extends Module {

    // Добавляем слайдер для выбора задержки.
    // К примеру, от 0 до 5, по умолчанию 0, шаг 1.
    private final SliderSetting delaySetting = new SliderSetting(
            this,
            "Delay",
            0.0F, // defaultValue
            0.0F, // minValue
            5.0F, // maxValue
            1.0F  // step
    );

    // Переопределение метода isStarred для добавления звёздочки
    @Override
    public boolean isStarred() {
        return true; // Звёздочка всегда отображается
    }

    // Событие тика, на котором обнуляем (или задаём) задержку правого клика,
    // если в руке у игрока бутылка опыта.
    @EventHandler
    public void onTick(TickEvent e) {
        if (mc.player == null || mc.world == null) return;

        ItemStack itemStack = mc.player.getHeldItemMainhand();
        if (itemStack.getItem() == Items.EXPERIENCE_BOTTLE) {
            // Получаем целочисленное значение задержки из слайдера
            int delay = delaySetting.getValue().intValue();
            // Устанавливаем задержку (в некоторых версиях может быть mc.rightClickDelayTimer = delay)
            mc.setRightClickDelayTimer(delay);
        }
    }

    
}
