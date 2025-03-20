package org.obsidian.client.managers.component.impl.target;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.component.Component;
import org.obsidian.client.managers.events.world.WorldChangeEvent;
import org.obsidian.client.managers.module.impl.client.Targets;
import org.obsidian.client.managers.module.impl.combat.AntiBot;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.utils.player.FakePlayer;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.client.utils.rotation.AuraUtil;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Accessors(fluent = true)
public class TargetComponent extends Component {
    private static final List<LivingEntity> entityList = Collections.synchronizedList(new ArrayList<>());
    private static int countLoadedEntities;
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    @Getter
    private static LivingEntity currentTarget;
    @Getter
    private static LivingEntity lastTarget;
    private static double lastRange = 0;

    @EventHandler
    public void onEvent(WorldChangeEvent event) {
        entityList.clear();
    }

    public static void updateTargetList() {
        if (mc.world == null) {
            synchronized (entityList) {
                entityList.clear();
            }
            return;
        }
        List<LivingEntity> entities = mc.world.loadedLivingEntityList()
                .stream()
                .filter(entity -> entity != mc.player)
                .toList();

        synchronized (entityList) {
            entityList.clear();
            entityList.addAll(entities);
        }
    }

    public static List<LivingEntity> getTargets(final double range, boolean saveRange) {
        if (countLoadedEntities != mc.world.getCountLoadedEntities()) {
            executorService.execute(TargetComponent::updateTargetList);
            countLoadedEntities = mc.world.getCountLoadedEntities();
        }

        if (currentTarget != null && !isValid(currentTarget)) {
            currentTarget = null;
        }
        if (saveRange) lastRange = range;
        synchronized (entityList) {
            return entityList.stream()
                    .filter(ENTITY_FILTER)
                    .filter(entity -> mc.player.getDistance(entity) <= range && mc.world.loadedLivingEntityList().contains(entity))
                    .collect(Collectors.toList());
        }
    }

    public static List<Entity> getTargets(final double range, Predicate<Entity> predicate, boolean saveRange) {
        if (countLoadedEntities != mc.world.getCountLoadedEntities()) {
            executorService.execute(TargetComponent::updateTargetList);
            countLoadedEntities = mc.world.getCountLoadedEntities();
        }

        if (currentTarget != null && !isValid(currentTarget)) {
            currentTarget = null;
        }
        if (saveRange) lastRange = range;
        synchronized (entityList) {
            return entityList.stream()
                    .filter(predicate)
                    .filter(entity -> mc.player.getDistance(entity) <= range && mc.world.loadedLivingEntityList().contains(entity))
                    .collect(Collectors.toList());
        }
    }

    public static LivingEntity getTarget(final double range) {
        return getTarget(range, true);
    }

    public static LivingEntity getTarget(final double range, boolean saveRange) {
        if (currentTarget == null || !isValid(currentTarget) || mc.player.getDistance(currentTarget) > range) {
            lastTarget = currentTarget = find(range, saveRange).orElse(null);
        }
        return currentTarget;
    }

    public static boolean targetExist() {
        return getTarget(lastRange, false) != null;
    }

    public static void clearTarget() {
        currentTarget = null;
    }

    private static final Predicate<LivingEntity> ENTITY_FILTER = TargetComponent::isValid;

    private static boolean isValid(final LivingEntity entity) {
        Targets module = Targets.getInstance();
        if (entity == null) return false;
        if (entity instanceof FakePlayer) return false;
        if (!module.throughWalls().getValue() && !mc.player.canEntityBeSeen(entity)) return false;
        if (entity.getHealth() <= 0 || !entity.isAlive() || entity.equals(mc.player)) return false;
        if (entity instanceof ArmorStandEntity) return false;
        final MultiBooleanSetting setting = module.targets();
        if (!setting.getValue("Невидимые") && entity.isInvisible()) return false;
        if (entity instanceof PlayerEntity player) {
            if (AntiBot.getInstance().isBot(player)) return false;
            if (Obsidian.inst().friendManager().isFriend(player.getGameProfile().getName())) return false;
            if (!setting.getValue("Игроки")) return false;
            if (!setting.getValue("Голые") && player.getTotalArmorValue() <= 0) return false;
            if (!setting.getValue("Тиммейты") && mc.player.isOnSameTeam(entity)) return false;
        }
        if (entity instanceof AnimalEntity) return setting.getValue("Животные");
        if (entity instanceof MobEntity) return setting.getValue("Мобы");
        return true;
    }

    private static Optional<LivingEntity> find(final double range, boolean saveRange) {
        List<LivingEntity> validTargets = getTargets(range, saveRange);
        final ModeSetting sortMode = Targets.getInstance().sortMode();
        if (validTargets.isEmpty()) return Optional.empty();
        if (sortMode.is("Адаптивный")) {
            validTargets.sort(Comparator.comparingDouble(TargetComponent::compareArmor)
                    .thenComparingDouble(AuraUtil::calculateFOVFromCamera)
                    .thenComparingDouble(AuraUtil::getStrictDistance)
                    .thenComparingDouble(PlayerUtil::getEntityHealth));
        } else if (sortMode.is("Дистанция")) {
            validTargets.sort(Comparator.comparingDouble(mc.player::getDistance)
                    .thenComparingDouble(PlayerUtil::getEntityHealth));
        } else if (sortMode.is("Здоровье")) {
            validTargets.sort(Comparator.comparingDouble(PlayerUtil::getEntityHealth)
                    .thenComparingDouble(AuraUtil::getStrictDistance));
        } else if (sortMode.is("Наводка")) {
            validTargets.sort(Comparator.comparingDouble(AuraUtil::calculateFOVFromCamera)
                    .thenComparingDouble(AuraUtil::getStrictDistance)
                    .thenComparingDouble(PlayerUtil::getEntityHealth)
                    .thenComparingDouble(TargetComponent::compareArmor));
        }
        return Optional.of(validTargets.get(0));
    }

    private static double compareArmor(LivingEntity entity) {
        return (entity instanceof PlayerEntity player) ? -PlayerUtil.getEntityArmor(player) : -entity.getTotalArmorValue();
    }
}
