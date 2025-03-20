package org.obsidian.client.managers.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.potion.Effects;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.joml.Vector4f;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.api.events.orbit.EventPriority;
import org.obsidian.client.managers.component.impl.rotation.Rotation;
import org.obsidian.client.managers.component.impl.rotation.RotationComponent;
import org.obsidian.client.managers.component.impl.rotation.SmoothRotationComponent;
import org.obsidian.client.managers.component.impl.target.TargetComponent;
import org.obsidian.client.managers.events.other.GameUpdateEvent;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.events.player.MoveEvent;
import org.obsidian.client.managers.events.player.MoveInputEvent;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.managers.events.world.WorldChangeEvent;
import org.obsidian.client.managers.events.world.WorldLoadEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.chat.ChatUtil;
import org.obsidian.client.utils.math.Interpolator;
import org.obsidian.client.utils.math.PerfectDelay;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.other.ViaUtil;
import org.obsidian.client.utils.player.InvUtil;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;
import org.obsidian.client.utils.rotation.AuraUtil;
import org.obsidian.client.utils.rotation.RayTraceUtil;
import org.obsidian.client.utils.rotation.RotationUtil;
import org.obsidian.common.impl.taskript.Script;
import org.obsidian.lib.util.time.StopWatch;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "KillAura", category = Category.COMBAT)
public class KillAura extends Module {

    // Константы
    private static final float DEFAULT_FOV = 70F;
    private static final float DEFAULT_ATTACK_RANGE = 3F;
    private static final float DEFAULT_PRE_RANGE = 1F;
    private static final float MIN_COOLDOWN_STRENGTH = 0.93F;

    public static KillAura getInstance() {
        return Instance.get(KillAura.class);
    }

    // Настройки модуля
    private final ModeSetting componentMode = new ModeSetting(this, "Тип наводки", "Обычный", "Плавный", "Грим", "Фантайм");

    private final SliderSetting fov = new SliderSetting(this, "Угол обзора", DEFAULT_FOV, 30F, 180F, 1F)
            .setVisible(() -> !componentMode.is("Грим"));

    private final SliderSetting attackRange = new SliderSetting(this, "Дистанция", DEFAULT_ATTACK_RANGE, 3F, 6F, 0.1F);

    private final SliderSetting preRange = new SliderSetting(this, "Доп дистанция", DEFAULT_PRE_RANGE, 0F, 3F, 0.1F)
            .setVisible(() -> !componentMode.is("Грим"));

    private final BooleanSetting onlyCrits = new BooleanSetting(this, "Только криты", true);

    private final BooleanSetting smartCrits = new BooleanSetting(this, "Умные криты", false)
            .setVisible(onlyCrits::getValue);

    private final MultiBooleanSetting checks = new MultiBooleanSetting(this, "Прочее",
            BooleanSetting.of("Таргет есп", false),
            BooleanSetting.of("Отображать FOV", false).setVisible(() -> !componentMode.is("Грим")),
            BooleanSetting.of("Не бить когда ешь", false),
            BooleanSetting.of("Бить только с оружием", false),
            BooleanSetting.of("Выключить после смерти", true)
    );

    // Сервисы и утилиты
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "KillAura-Scheduler");
        thread.setDaemon(true);
        return thread;
    });

    private final PerfectDelay perfectDelay = new PerfectDelay();
    private final StopWatch stopWatch = new StopWatch();
    private final Script script = new Script();

    // Состояние
    public LivingEntity target;
    private boolean canCrit;
    private Vector2f lerpRotation = Vector2f.ZERO;
    private int count;
    private int pauseMovementTicks;

    // Генератор случайных чисел для большей безопасности
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    @Override
    public void toggle() {
        super.toggle();
        reset();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        scheduler.shutdownNow();
        reset();
    }

    @EventHandler
    public void onEvent(PacketEvent event) {
        IPacket<?> packet = event.getPacket();
        if (packet instanceof CHeldItemChangePacket) {
            perfectDelay.reset(550L);
        } else if (packet instanceof CAnimateHandPacket) {
            perfectDelay.reset(450L);
        }
    }

    @EventHandler
    public void onInput(MoveInputEvent e) {
        if (pauseMovementTicks > 0) {
            e.setForward(0);
            pauseMovementTicks--;
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
    public void onEvent(Render2DEvent event) {
        if (target == null || !checks.getValue("Отображать FOV")) return;

        MatrixStack matrix = event.getMatrix();

        float baseFov = this.fov.getValue();
        float calcFov = (float) AuraUtil.calculateFOVFromCamera(target);

        float centerX = mw.getScaledWidth() / 2F;
        float centerY = mw.getScaledHeight() / 2F;

        boolean inFovRange = calcFov < baseFov;
        float delta = Math.max(5F, calcFov - baseFov);

        if (!inFovRange) {
            float percent = delta / (fov.max - baseFov);
            int calcColor = ColorUtil.multAlpha(ColorUtil.RED, 1F - percent);
            RenderUtil.Rounded.roundedOutline(matrix, centerX - calcFov, centerY - calcFov,
                    calcFov * 2F, calcFov * 2F, delta,
                    calcColor, Round.of(calcFov - delta));
        }

        int baseColor = inFovRange ? ColorUtil.GREEN : ColorUtil.RED;
        float outline = inFovRange ? 5F : delta;
        RenderUtil.Rounded.roundedOutline(matrix, centerX - baseFov, centerY - baseFov,
                baseFov * 2F, baseFov * 2F, outline,
                baseColor, Round.of(baseFov - outline));
    }

    @EventHandler
    public void onEvent(MoveEvent event) {
        if (target == null || mc.player == null || mc.world == null) {
            canCrit = false;
            return;
        }

        final boolean fallCheck = mc.player.nextFallDistance != 0F;
        canCrit = !event.isToGround() && event.getFrom().y > event.getTo().y && fallCheck;
    }

    @EventHandler
    public void onEvent(UpdateEvent event) {
        script.update();

        // Проверка условий запуска модуля
        if (!ViaUtil.allowedBypass() && componentMode.is("Грим")) {
            ChatUtil.addTextWithError("Нужно зайти на сервер с версии 1.17 и выше!");
            toggle();
            return;
        }

        if (checks.getValue("Выключить после смерти") && !mc.player.isAlive()) {
            toggle();
            return;
        }

        updateTarget();

        if (target == null || mc.player == null || mc.world == null) {
            reset();
            return;
        }

        if (checkReturn() || handleShieldBreaker()) return;

        updateAttack();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEvent(GameUpdateEvent event) {
        if (target == null || mc.player == null || mc.world == null) {
            reset();
            return;
        }

        if (checkReturn()) return;

        updateRotation();
    }

    /**
     * Обновляет поворот игрока в направлении цели с учетом естественности движений
     */
    private void updateRotation() {
        if (target == null) return;

        double maxHeight = (AuraUtil.getStrictDistance(target) / attackDistance());
        Vector3d targetPos = target.getPositionVec()
                .add(0, MathHelper.clamp(mc.player.getEyePosition(mc.getRenderPartialTicks()).y - target.getPosY(), 0, maxHeight), 0)
                .subtract(mc.player.getEyePosition(mc.getRenderPartialTicks()))
                .normalize();

        float rawYaw = (float) Math.toDegrees(Math.atan2(-targetPos.x, targetPos.z));
        float rawPitch = (float) MathHelper.clamp(-Math.toDegrees(Math.atan2(targetPos.y, Math.hypot(targetPos.x, targetPos.z))), -90F, 90F);

        // Более естественные движения с оптимизированной случайностью
        float speed = random.nextBoolean() ? randomRange(0.3F, 0.4F) : randomRange(0.5F, 0.6F);

        float time = System.currentTimeMillis() / 100F;
        float cos = (float) Math.cos(time);
        float sin = (float) Math.sin(time);

        float cooldown = 1F - cooldownFromLastSwing();
        float yawOffset = randomRange(6F, 12F) * cos + cooldown * (randomRange(60F, 90F) * (count == 0 ? 1 : -1));
        float pitchOffset = randomRange(6F, 12F) * sin + cooldown * (randomRange(15F, 45F) * (count == 0 ? 1 : -1));

        // Плавное интерполирование поворота
        lerpRotation = new Vector2f(
                wrapLerp(speed, MathHelper.wrapDegrees(lerpRotation.x), MathHelper.wrapDegrees(rawYaw + yawOffset)),
                wrapLerp(speed / 2F, lerpRotation.y, MathHelper.clamp(rawPitch + pitchOffset, -90F, 90F))
        );

        Rotation rotation = new Rotation(
                mc.player.rotationYaw + (float) Math.ceil(MathHelper.wrapDegrees(lerpRotation.x) - MathHelper.wrapDegrees(mc.player.rotationYaw)),
                mc.player.rotationPitch + (float) Math.ceil(MathHelper.wrapDegrees(lerpRotation.y) - MathHelper.wrapDegrees(mc.player.rotationPitch))
        );

        float currentFov = (float) AuraUtil.calculateFOVFromCamera(target);
        float baseFov = this.fov.getValue();

        boolean isFastAttack = cooldownFromLastSwing() > 0.5F;
        if (Math.abs(currentFov) < baseFov) {
            if (componentMode.is("Плавный")) {
                SmoothRotationComponent.update(rotation,
                        isFastAttack || rayTraceTarget() ? random.nextFloat() : 3F,
                        10F, 3F, 3F, 1, 5, false);
            }
            if (componentMode.is("Обычный")) {
                RotationComponent.update(rotation,
                        isFastAttack || rayTraceTarget() ? baseFov : (1F - (currentFov / baseFov)) * baseFov,
                        20, 1, 5);
            }
        }
    }

    /**
     * Интерполирует значение с учетом обертывания угла
     */
    public float wrapLerp(float step, float input, float target) {
        return input + step * MathHelper.wrapDegrees(target - input);
    }

    /**
     * Возвращает случайное значение в заданном диапазоне
     */
    public float randomRange(float min, float max) {
        return Interpolator.lerp(max, min, random.nextFloat());
    }

    /**
     * Рассчитывает кулдаун от последнего удара
     */
    public float cooldownFromLastSwing() {
        return MathHelper.clamp(mc.player.ticksSinceLastSwing / randomRange(8, 12), 0.0F, 1.0F);
    }

    /**
     * Обновляет атаку на цель
     */
    private void updateAttack() {
        if (shouldAttack() && rayTraceTarget() && AuraUtil.getStrictDistance(target) < attackDistance()) {
            boolean isInLiquid = isPlayerInLiquid();
            boolean sprinting = mc.player.isSprinting();

            if (!isInLiquid && sprinting) {
                // Останавливаем спринт перед атакой
                pauseMovementTicks = 1;
                if (mc.player.isServerSprintState()) return;
            }

            if (componentMode.is("Фантайм")) {
                Vector2f vec2f = RotationUtil.calculate(target);
                RotationComponent.update(new Rotation(vec2f.x, vec2f.y), 360, 360, 0, 5);
            }

            attackEntity(target);
            count = (count + 1) % 2;
            canCrit = false;
            stopWatch.reset();
        } else if (!mc.player.canEntityBeSeen(target) && PlayerUtil.isHoly()) {
            RotationComponent.update(new Rotation(Rotation.cameraYaw(), 90), 360, 360, 0, 5);
        }
    }

    /**
     * Проверяет, находится ли игрок в жидкости
     */
    private boolean isPlayerInLiquid() {
        return mc.player.isActualySwimming() ||
                (mc.player.isSwimming() && mc.player.areEyesInFluid(FluidTags.WATER)) ||
                mc.player.areEyesInFluid(FluidTags.LAVA);
    }

    /**
     * Обрабатывает прерывание щита цели
     */
    private boolean handleShieldBreaker() {
        Slot axeSlot = InvUtil.getAxeSlot();
        if (target.getActiveItemStack().getItem() == Items.SHIELD && axeSlot != null && rayTraceTarget()) {
            InvUtil.clickSlot(axeSlot, mc.player.inventory.currentItem, ClickType.SWAP, true);
            attackEntity(target);
            InvUtil.clickSlot(axeSlot, mc.player.inventory.currentItem, ClickType.SWAP, true);
            return true;
        }
        return false;
    }

    /**
     * Проверяет условия для прерывания атаки
     */
    private boolean checkReturn() {
        return (mc.player.isHandActive() && checks.getValue("Не бить когда ешь")) ||
                (!(mc.player.getHeldItemMainhand().getItem() instanceof AxeItem ||
                        mc.player.getHeldItemMainhand().getItem() instanceof SwordItem) &&
                        checks.getValue("Бить только с оружием"));
    }

    /**
     * Атакует сущность с учетом вращения
     */
    private void attackEntity(Entity entity) {
        rotateGrim(true);
        mc.playerController.attackEntity(mc.player, entity);
        mc.player.swingArm(Hand.MAIN_HAND);
        script.cleanup().addTickStep(0, () -> rotateGrim(false));
    }

    /**
     * Обновляет текущую цель
     */
    private void updateTarget() {
        target = TargetComponent.getTarget(attackRange.getValue() + preRange.getValue());
    }

    /**
     * Применяет вращение для режима "Грим"
     */
    public void rotateGrim(boolean start) {
        if (!componentMode.is("Грим")) return;

        if (start) {
            final Vector4f rotation = AuraUtil.calculateRotation(target);
            float yaw = rotation.x + randomRange(-0.02f, 0.02f);
            float pitch = rotation.y + randomRange(-0.02f, 0.02f);
            float yOffset = Criticals.getInstance().isEnabled() && !mc.player.isOnGround() ? 1e-6f : 0f;
            ViaUtil.sendPositionPacket(
                    mc.player.getPosX(),
                    mc.player.getPosY() - yOffset,
                    mc.player.getPosZ(),
                    yaw, pitch,
                    mc.player.isOnGround()
            );
        } else {
            ViaUtil.sendPositionPacket(mc.player.rotationYaw, mc.player.rotationPitch, false);
        }
    }

    /**
     * Проверяет, можно ли атаковать цель
     */
    private boolean shouldAttack() {
        if (!perfectDelay.cooldownComplete() || !cooldownComplete()) return false;

        boolean isBlockAboveHead = PlayerUtil.isBlockAboveHead();
        boolean isInLiquid = isPlayerInLiquid();

        if (isInLiquid) return true;

        boolean canDefaultCrit = (mc.player.fallDistance > 0 && canCrit && isBlockAboveHead) ||
                (Criticals.getInstance().isEnabled() && !mc.player.isOnGround());

        boolean canSmartCrit = isBlockAboveHead &&
                !mc.player.movementInput.jump &&
                !canCrit &&
                mc.player.isOnGround() &&
                mc.player.collidedVertically;

        if (onlyCrits.getValue() && !smartCrits.getValue()) {
            return shouldCritical() && canDefaultCrit;
        }

        if (smartCrits.getValue()) {
            return (shouldCritical() || canDefaultCrit) || canSmartCrit;
        }

        return true;
    }

    /**
     * Проверяет, можно ли сделать критический удар
     */
    private boolean shouldCritical() {
        boolean isDeBuffed = mc.player.isPotionActive(Effects.LEVITATION) ||
                mc.player.isPotionActive(Effects.BLINDNESS) ||
                mc.player.isPotionActive(Effects.SLOW_FALLING);

        boolean isInLiquid = isPlayerInLiquid();
        boolean isFlying = mc.player.abilities.isFlying || mc.player.isElytraFlying();
        boolean isClimbing = mc.player.isOnLadder();
        boolean isCantJump = mc.player.isPassenger();
        boolean isOnWeb = PlayerUtil.isPlayerInWeb();

        return !(isDeBuffed || isInLiquid || isFlying || isClimbing || isCantJump || isOnWeb);
    }

    /**
     * Проверяет готовность к атаке по кулдауну
     */
    public boolean cooldownComplete() {
        return mc.player.getCooledAttackStrength(1.5F) >= MIN_COOLDOWN_STRENGTH;
    }

    /**
     * Проверяет видимость цели
     */
    public boolean rayTraceTarget() {
        return RayTraceUtil.rayTraceEntity(mc.player.rotationYaw, mc.player.rotationPitch, attackDistance(), target) ||
                componentMode.is("Грим") ||
                componentMode.is("Фантайм");
    }

    /**
     * Возвращает дистанцию атаки
     */
    public double attackDistance() {
        return Math.max(mc.playerController.extendedReach() ? 6.0D : 3.0D, attackRange.getValue());
    }

    /**
     * Сбрасывает состояние модуля
     */
    private void reset() {
        TargetComponent.clearTarget();
        TargetComponent.updateTargetList();
        target = null;
        canCrit = false;
    }
}