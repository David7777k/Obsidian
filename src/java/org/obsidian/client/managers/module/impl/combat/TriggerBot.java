package org.obsidian.client.managers.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.events.player.MoveEvent;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.events.world.WorldChangeEvent;
import org.obsidian.client.managers.events.world.WorldLoadEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.utils.math.PerfectDelay;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.client.utils.rotation.AuraUtil;
import org.obsidian.client.utils.rotation.RayTraceUtil;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "TriggerBot", category = Category.COMBAT)
public class TriggerBot extends Module {

    public static TriggerBot getInstance() {
        return Instance.get(TriggerBot.class);
    }

    private final BooleanSetting onlyCrits = new BooleanSetting(this, "Только криты", true);
    private final BooleanSetting smartCrits = new BooleanSetting(this, "Умные криты", false).setVisible(onlyCrits::getValue);

    private final PerfectDelay perfectDelay = new PerfectDelay();

    private boolean canCrit;

    @Override
    public void toggle() {
        super.toggle();
        reset();
    }

    @EventHandler
    public void onEvent(PacketEvent event) {
        IPacket<?> packet = event.getPacket();
        if (packet instanceof CHeldItemChangePacket) {
            perfectDelay.reset(650L);
        } else if (packet instanceof CAnimateHandPacket) {
            perfectDelay.reset(500L);
        }
    }

    @EventHandler
    public void onEvent(WorldLoadEvent event) {
        reset();
    }

    @EventHandler
    public void onEvent(WorldChangeEvent event) {
        reset();
    }

    @EventHandler
    public void onEvent(MoveEvent event) {
        if (mc.player == null || mc.world == null) {
            canCrit = false;
            return;
        }
        final boolean fallCheck = mc.player.nextFallDistance != 0F;
        canCrit = !event.isToGround() && event.getFrom().y > event.getTo().y && fallCheck;
    }

    @EventHandler
    public void onEvent(UpdateEvent event) {
        if (mc.player == null || mc.world == null) {
            reset();
            return;
        }
        if (mc.pointedEntity instanceof LivingEntity target && !(mc.pointedEntity instanceof ArmorStandEntity)) {
            if (target instanceof PlayerEntity player && Obsidian.inst().friendManager().isFriend(player.getGameProfile().getName())) {
                return;
            }
            updateAttack(target);
        }
    }

    private void updateAttack(LivingEntity target) {
        if (shouldAttack() && (RayTraceUtil.rayTraceEntity(mc.player.rotationYaw, mc.player.rotationPitch, attackDistance(), target) || mc.player.isElytraFlying()) && AuraUtil.getStrictDistance(target) < attackDistance()) {
            boolean isInLiquid = mc.player.isActualySwimming() || mc.player.isSwimming() && mc.player.areEyesInFluid(FluidTags.WATER) || mc.player.areEyesInFluid(FluidTags.LAVA);
            boolean sprinting = mc.player.isSprinting();
            if (!isInLiquid && sprinting) {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.STOP_SPRINTING));
                mc.player.setServerSprintState(false);
                mc.player.setSprinting(false);
            }
            attackEntity(target);
            canCrit = false;
        }
    }

    private void attackEntity(Entity entity) {
        mc.playerController.attackEntity(mc.player, entity);
        mc.player.swingArm(Hand.MAIN_HAND);
    }

    private boolean shouldAttack() {
        if (!perfectDelay.cooldownComplete() || !cooldownComplete()) return false;

        boolean isBlockAboveHead = PlayerUtil.isBlockAboveHead();

        boolean isInLiquid = mc.player.isActualySwimming() || mc.player.isSwimming() && mc.player.areEyesInFluid(FluidTags.WATER) || mc.player.areEyesInFluid(FluidTags.LAVA);

        if (isInLiquid) return true;

        if (onlyCrits.getValue() && !smartCrits.getValue()) {
            return shouldCritical()
                    && mc.player.fallDistance > 0
                    && canCrit
                    && isBlockAboveHead;
        }

        if (smartCrits.getValue()) {
            boolean canSmartCrit = isBlockAboveHead && !mc.player.movementInput.jump && !canCrit
                    && mc.player.isOnGround()
                    && mc.player.collidedVertically;

            return (shouldCritical() && mc.player.fallDistance > 0 && canCrit && isBlockAboveHead) || canSmartCrit;
        }

        return true;
    }

    private boolean shouldCritical() {
        boolean isDeBuffed = mc.player.isPotionActive(Effects.LEVITATION) || mc.player.isPotionActive(Effects.BLINDNESS) || mc.player.isPotionActive(Effects.SLOW_FALLING);
        boolean isInLiquid = mc.player.isActualySwimming() || mc.player.isSwimming() && mc.player.areEyesInFluid(FluidTags.WATER) || mc.player.areEyesInFluid(FluidTags.LAVA);
        boolean isFlying = mc.player.abilities.isFlying || mc.player.isElytraFlying();
        boolean isClimbing = mc.player.isOnLadder();
        boolean isCantJump = mc.player.isPassenger();
        boolean isOnWeb = PlayerUtil.isPlayerInWeb();

        return !(isDeBuffed || isInLiquid || isFlying || isClimbing || isCantJump || isOnWeb);
    }

    public boolean cooldownComplete() {
        return mc.player.getCooledAttackStrength(1.5F) >= 0.93F;
    }

    public double attackDistance() {
        return mc.playerController.extendedReach() ? 6.0D : 3.0D;
    }

    private void reset() {
        canCrit = false;
    }
}