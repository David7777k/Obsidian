package org.obsidian.client.managers.module.impl.movement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.network.play.client.CEntityActionPacket;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.lib.util.time.StopWatch;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "WaterSpeed", category = Category.MOVEMENT)
public class WaterSpeed extends Module {
    public static WaterSpeed getInstance() {
        return Instance.get(WaterSpeed.class);
    }

    private final StopWatch time = new StopWatch();

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        if ((mc.world.getBlockState(mc.player.getPosition().up()).getBlock() != Blocks.WATER && mc.gameSettings.keyBindJump.isKeyDown()) || !mc.player.isInWater()) {
            time.reset();
        }

        if (mc.player.isInWater() && time.finished(160)) {
            float ySpeed = mc.gameSettings.keyBindJump.isKeyDown() ? 0.05f : mc.gameSettings.keyBindSneak.isKeyDown() ? -0.05f : !mc.player.isSprinting() ? 0.005f : 0;
            mc.player.movementInput.sneaking = false;

            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.PRESS_SHIFT_KEY));
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
            float acceletion = mc.player.isSprinting() ? 1.02f : 1.15f;
            mc.player.setVelocity(mc.player.getMotion().x * acceletion, mc.player.getMotion().y + ySpeed, mc.player.getMotion().z * acceletion);
        }
    }
}