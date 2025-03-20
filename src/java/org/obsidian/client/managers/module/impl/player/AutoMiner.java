package org.obsidian.client.managers.module.impl.player;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.utils.other.Instance;

@ModuleInfo(name = "AutoMiner", category = Category.PLAYER)
public class AutoMiner extends Module {

    private BlockPos currentMiningPos = null;
    private int miningTicks = 0;
    private final int maxMiningTicks = 15; // Больше времени на разрушение блока
    private final double SPRINT_SPEED = 0.40; // Стандартная скорость спринта в Minecraft
    private int blockActionCounter = 0;
    private final int MAX_BLOCK_ACTIONS = 2; // Максимум действий с блоками за цикл
    private int actionDelay = 0;

    // Метод для получения экземпляра модуля через Instance
    public static AutoMiner getInstance() {
        return Instance.get(AutoMiner.class);
    }

    @Override
    public void onEnable() {
        currentMiningPos = null;
        miningTicks = 0;
        blockActionCounter = 0;
        actionDelay = 0;
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            mc.playerController.resetBlockRemoving();
        }
        super.onDisable();
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) {
            return;
        }

        // Управление задержкой для предотвращения спама действий
        if (actionDelay > 0) {
            actionDelay--;
            // Продолжаем движение даже во время задержки
            movePlayerForward();
            return;
        }

        // Сбрасываем счетчик действий с блоками
        blockActionCounter = 0;

        // Проверяем, надо ли искать новый блок
        if (currentMiningPos == null || mc.world.getBlockState(currentMiningPos).isAir() || miningTicks >= maxMiningTicks) {
            currentMiningPos = findBlockToMine();
            miningTicks = 0;
        }

        // Если есть блок для добычи и не превышен лимит действий
        if (currentMiningPos != null && blockActionCounter < MAX_BLOCK_ACTIONS) {
            // Проверяем, что блок все еще существует
            if (!mc.world.getBlockState(currentMiningPos).isAir()) {
                // Добываем блок
                mc.playerController.onPlayerDamageBlock(currentMiningPos, mc.player.getHorizontalFacing());
                blockActionCounter++;
                miningTicks++;
            } else {
                // Блок уже разрушен, ищем новый
                currentMiningPos = null;
            }
        }

        // Если превышен лимит действий, устанавливаем задержку
        if (blockActionCounter >= MAX_BLOCK_ACTIONS) {
            actionDelay = 5; // Небольшая задержка для снижения нагрузки на сервер
        }

        // Устанавливаем стандартный спринт
        mc.player.setSprinting(true);

        // Движение игрока
        movePlayerForward();
    }

    private BlockPos findBlockToMine() {
        // Определение позиции игрока и направления его взгляда
        BlockPos playerPos = mc.player.getPosition();
        Vector3d lookVec = mc.player.getLookVec();
        Vector3d normalized = new Vector3d(lookVec.x, 0, lookVec.z).normalize();

        // Проверяем только блоки прямо перед игроком на небольшом расстоянии
        for (int distance = 1; distance <= 2; distance++) {
            int dx = Math.round((float) (normalized.x * distance));
            int dz = Math.round((float) (normalized.z * distance));

            // Проверяем три уровня блоков: уровень ног, глаз и над головой
            for (int y = 0; y <= 2; y++) {
                BlockPos checkPos = playerPos.add(dx, y, dz);

                // Если блок не воздух и можно добыть
                if (!mc.world.getBlockState(checkPos).isAir() && canMineBlock(checkPos)) {
                    return checkPos;
                }
            }
        }

        return null;
    }

    private boolean canMineBlock(BlockPos pos) {
        // Дополнительная проверка возможности добычи блока
        // Можно добавить исключения для определенных типов блоков
        return true;
    }

    private void movePlayerForward() {
        // Получаем вектор направления взгляда, исключая вертикальную составляющую
        Vector3d lookVec = mc.player.getLookVec();
        Vector3d forward = new Vector3d(lookVec.x, 0, lookVec.z).normalize();

        // Используем текущую вертикальную скорость
        double verticalMotion = mc.player.getMotion().y;

        // Вычисляем скорость на основе стандартной скорости спринта
        double speed = SPRINT_SPEED;

        // Если игрок находится в воздухе, снижаем скорость
        if (!mc.player.isOnGround()) {
            speed *= 0.7;
        }

        // Смешиваем текущее движение с новым для плавности
        double currentX = mc.player.getMotion().x;
        double currentZ = mc.player.getMotion().z;

        // Плавное ускорение для избежания резких скачков скорости
        double mixFactor = 0.4; // 40% нового движения, 60% старого
        double newX = currentX * (1 - mixFactor) + forward.x * speed * mixFactor;
        double newZ = currentZ * (1 - mixFactor) + forward.z * speed * mixFactor;

        // Устанавливаем новую скорость
        mc.player.setMotion(newX, verticalMotion, newZ);
    }

    // Переопределение метода isStarred для добавления звёздочки
    @Override
    public boolean isStarred() {
        return true; // Звёздочка всегда отображается
    }
}