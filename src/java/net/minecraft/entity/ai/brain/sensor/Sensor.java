package net.minecraft.entity.ai.brain.sensor;

import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.world.server.ServerWorld;
import org.obsidian.common.impl.fastrandom.FastRandom;

import java.util.Random;
import java.util.Set;

public abstract class Sensor<E extends LivingEntity> {
    private static final Random RANDOM = new FastRandom();
    private static final EntityPredicate FRIENDLY_FIRE_WITH_VISIBILITY_CHECK = (new EntityPredicate()).setDistance(16.0D).allowFriendlyFire().setSkipAttackChecks();
    private static final EntityPredicate FRIENDLY_FIRE_WITHOUT_VISIBILITY_CHECK = (new EntityPredicate()).setDistance(16.0D).allowFriendlyFire().setSkipAttackChecks().setUseInvisibilityCheck();
    private final int interval;
    private long counter;

    public Sensor(int interval) {
        this.interval = interval;
        this.counter = RANDOM.nextInt(interval);
    }

    public Sensor() {
        this(20);
    }

    public final void tick(ServerWorld worldIn, E entityIn) {
        if (--this.counter <= 0L) {
            this.counter = this.interval;
            this.update(worldIn, entityIn);
        }
    }

    protected abstract void update(ServerWorld worldIn, E entityIn);

    public abstract Set<MemoryModuleType<?>> getUsedMemories();

    protected static boolean canAttackTarget(LivingEntity livingEntity, LivingEntity target) {
        return livingEntity.getBrain().hasMemory(MemoryModuleType.ATTACK_TARGET, target) ? FRIENDLY_FIRE_WITHOUT_VISIBILITY_CHECK.canTarget(livingEntity, target) : FRIENDLY_FIRE_WITH_VISIBILITY_CHECK.canTarget(livingEntity, target);
    }
}
