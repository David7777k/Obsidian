package org.obsidian.common.impl.viaversion.fixes;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.common.impl.viaversion.ViaLoadingBase;

public class AttackOrder implements IMinecraft {
    public static void sendConditionalSwing(RayTraceResult rayTrace, Hand hand) {
        if (rayTrace != null && rayTrace.getType() != RayTraceResult.Type.ENTITY) mc.player.swingArm(hand);
    }

    public static void sendFixedAttack(PlayerEntity player, Entity target, Hand hand) {
        if (ViaLoadingBase.getInstance().getTargetVersion().olderThanOrEqualTo(ProtocolVersion.v1_8)) {
            mc.player.swingArm(hand);
            mc.playerController.attackEntity(player, target);
        } else {
            mc.playerController.attackEntity(player, target);
            mc.player.swingArm(hand);
        }
    }
}
