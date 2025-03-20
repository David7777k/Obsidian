package org.obsidian.client.managers.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.other.ViaUtil;
import org.obsidian.client.utils.player.InvUtil;
import org.obsidian.client.utils.player.MoveUtil;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.client.utils.rotation.RotationUtil;
import org.obsidian.lib.util.time.StopWatch;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoPotion", category = Category.COMBAT)
public class AutoPotion extends Module {
    public static AutoPotion getInstance() {
        return Instance.get(AutoPotion.class);
    }

    private final MultiBooleanSetting baffs = new MultiBooleanSetting(this, "Зелья",
            BooleanSetting.of("Исцеление", true),
            BooleanSetting.of("Сила", false),
            BooleanSetting.of("Скорость", false),
            BooleanSetting.of("Огнестойкость", false)
    );
    private final SliderSetting minHealth = new SliderSetting(this, "Уровень здоровья", 14, 4, 20, 1).setVisible(() -> baffs.getValue("Исцеление"));
    private final BooleanSetting near = new BooleanSetting(this, "Только если рядом Игрок", true);
    private final StopWatch timer = new StopWatch();

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        boolean findPlayer = near.getValue() && mc.world.getPlayers().stream().filter(p -> mc.player.getDistance(p) < 10).findFirst().isEmpty();
        Vector3d vec = getRotationTargetPosition();
        if (ViaUtil.allowedBypass() || findPlayer || vec == null) return;
        Vector2f rotations = calculateRotations(vec);

        if (baffs.getValue("Сила") || baffs.getValue("Скорость") || baffs.getValue("Огнестойкость")) {
            if (!getPotionsToThrow().isEmpty() && !mc.player.isHandActive() && timer.finished(400)) {
                for (PotionTypes potionType : getPotionsToThrow()) {
                    Slot potionSlot = getPotionSlot(potionType);
                    if (potionSlot != null) {
                        InvUtil.findItemAndThrow(potionSlot, rotations.x, rotations.y);
                        timer.reset();
                    }
                }
            }
        }
        if (baffs.getValue("Исцеление")) {
            boolean hp = minHealth.getValue() > (mc.player.getHealth() + mc.player.getAbsorptionAmount());
            Slot healthSlot = getPotionSlot(PotionTypes.INSTANT_HEALTH);
            if (hp && timer.finished(400) && healthSlot != null) {
                InvUtil.findItemAndThrow(healthSlot, rotations.x, rotations.y);
                timer.reset();
            }
        }
    }

    private Vector3d getRotationTargetPosition() {
        List<Vector3d> potentialTargets = new CopyOnWriteArrayList<>();
        float searchRadiusXZ = 2;
        float searchRadiusYPositive = 2;
        float searchRadiusYNegative = 2;
        double playerPosX = mc.player.getPosX();
        double playerPosY = mc.player.getPosY();
        double playerPosZ = mc.player.getPosZ();

        for (double x = playerPosX - searchRadiusXZ; x < playerPosX + searchRadiusXZ; x += 1f) {
            for (double z = playerPosZ - searchRadiusXZ; z < playerPosZ + searchRadiusXZ; z += 1f) {
                for (double y = playerPosY + searchRadiusYPositive; y > playerPosY - searchRadiusYNegative; y -= 1f) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (mc.world.getBlockState(pos).isSolid())
                        potentialTargets.add(new Vector3d(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5));
                }
            }
        }

        return potentialTargets.stream().min(Comparator.comparingDouble(p -> mc.player.getDistanceSq(p))).orElse(null);
    }

    private float calculateThrowPitch() {
        double speed = MoveUtil.speedSqrt();
        float maxPitch = 90;
        double delta = Mathf.clamp(0, 0.5, Math.sqrt(speed * speed) / 0.5F) - Mathf.clamp(0, 0.5, Math.abs(Math.sqrt(mc.player.getMotion().y * mc.player.getMotion().y)) * 2F);
        return (float) (maxPitch - maxPitch * Mathf.clamp(0, 1, delta * 2.0D));
    }

    private Vector2f calculateRotations(Vector3d targetPosition) {
        if (targetPosition == null) {
            float yaw = (float) MoveUtil.getInputYaw(mc.player.rotationYaw);
            boolean blockSolid = PlayerUtil.isBlockSolid(new BlockPos(mc.player.getPositionVec().add(0, mc.player.getEyeHeight() + 0.7D, 0)));
            float pitch = blockSolid ? -90 : calculateThrowPitch();
            return new Vector2f(yaw, pitch);
        } else {
            return RotationUtil.calculate(targetPosition);
        }
    }

    private boolean isPotion(ItemStack stack, PotionTypes potionType) {
        if (stack == null || stack.getItem() != Items.SPLASH_POTION) return false;

        int potionId = getPotionId(potionType);
        for (EffectInstance effect : PotionUtils.getEffectsFromStack(stack)) {
            if (effect.getPotion() == Effect.get(potionId)) return true;
        }
        return false;
    }

    private boolean isPotionActive(PotionTypes potionType) {
        Effect effect = Effect.get(getPotionId(potionType));
        if (effect == null || mc.player == null) return false;

        return mc.player.isPotionActive(effect) && mc.player.getActivePotionEffect(effect).getDuration() > 5;
    }

    private List<PotionTypes> getPotionsToThrow() {
        return Arrays.stream(PotionTypes.values())
                .filter(potionType -> getPotionSlot(potionType) != null && !isPotionActive(potionType))
                .collect(Collectors.toList());
    }

    private Slot getPotionSlot(PotionTypes potionType) {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> isPotion(s.getStack(), potionType)).findFirst().orElse(null);
    }

    private int getPotionId(PotionTypes potionType) {
        return switch (potionType) {
            case STRENGTH -> 5;
            case SPEED -> 1;
            case INSTANT_HEALTH -> 6;
            case FIRE_RESISTANCE -> 12;
            case INSTANT_DAMAGE -> 7;
        };
    }

    private enum PotionTypes {
        STRENGTH, SPEED, FIRE_RESISTANCE, INSTANT_HEALTH, INSTANT_DAMAGE
    }
}
