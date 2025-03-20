package org.obsidian.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.MoveInputEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.MoveUtil;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AntiAFK", category = Category.MISC)
public class AntiAFK extends Module {
    public static AntiAFK getInstance() {
        return Instance.get(AntiAFK.class);
    }

    private final SliderSetting ticks = new SliderSetting(this, "Задержка в тиках", 1000, 10, 5000, 10);

    @EventHandler
    public void onEvent(MoveInputEvent event) {
        if (MoveUtil.isMoving()) return;
        int ticks = mc.player.ticksExisted;

        boolean tick1 = ticks % this.ticks.getValue().intValue() == 0;
        boolean tick2 = ticks % this.ticks.getValue().intValue() == 1;

        event.setSneaking(tick1);
        event.setJump(tick1);
        if (tick2) {
            event.setSneaking(false);
            event.setJump(false);
            mc.clickMouse();
            mc.player.rotationYaw += 90;
        }
    }
}