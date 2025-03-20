package org.obsidian.client.managers.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.AttackEvent;
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
@ModuleInfo(name = "Criticals", category = Category.COMBAT)
public class Criticals extends Module {
    public static Criticals getInstance() {
        return Instance.get(Criticals.class);
    }

    private final ModeSetting mode = new ModeSetting(this, "Режим", "Grim");

    @Override
    public void onEnable() {
        if (!ViaUtil.allowedBypass()) {
            ChatUtil.addTextWithError("Нужно зайти на сервер с версии 1.17 и выше!");
            toggle();
        }
    }

    @EventHandler
    public void onAttack(AttackEvent event) {
        if (ViaUtil.allowedBypass() && !mc.player.isElytraFlying() && !mc.player.isInWater()) {
            KillAura aura = KillAura.getInstance();

            if (mode.is("Grim")) {
                if (!mc.player.isOnGround() && mc.player.fallDistance == 0) {
                    mc.player.fallDistance = 0.001f;
                    if (aura.target() != null && event.getTarget() == aura.target() && aura.componentMode().is("Грим")) {
                        return;
                    }
                    ViaUtil.sendPositionPacket(mc.player.getPosX(), mc.player.getPosY() - 1e-6, mc.player.getPosZ(), mc.player.rotationYaw, mc.player.rotationPitch, false);
                }
            }
        }
    }
}