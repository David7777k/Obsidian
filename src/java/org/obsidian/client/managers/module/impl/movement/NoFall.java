package org.obsidian.client.managers.module.impl.movement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.utils.chat.ChatUtil;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.other.ViaUtil;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "NoFall", category = Category.MOVEMENT)
public class NoFall extends Module {
    public static NoFall getInstance() {
        return Instance.get(NoFall.class);
    }

    private final ModeSetting mode = new ModeSetting(this, "Mode", "Grim");

    @Override
    public void toggle() {
        super.toggle();
        if (!ViaUtil.allowedBypass()) {
            ChatUtil.addText("Нужно зайти на сервер с версии 1.17 и выше!");
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        if (ViaUtil.allowedBypass() && mc.player.fallDistance > 2.5) {
            if (mc.player.getServerBrand().toLowerCase().contains("captcha")) {
                return;
            }

            ViaUtil.sendPositionPacket(mc.player.getPosX(), mc.player.getPosY() + 1e-6, mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch, false);
            mc.player.fallDistance = 0;
        }
    }
}
