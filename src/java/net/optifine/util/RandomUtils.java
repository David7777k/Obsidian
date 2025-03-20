package net.optifine.util;

import lombok.Getter;
import org.obsidian.common.impl.fastrandom.FastRandom;

import java.util.Random;

public class RandomUtils {
    @Getter
    private static final Random random = new FastRandom();

    public static int getRandomInt(int bound) {
        return random.nextInt(bound);
    }
}
