package org.obsidian.client.utils.math;

import lombok.experimental.UtilityClass;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public class Mathf {
    private static void validateRange(double min, double max) {
        if (max < min) {
            throw new IllegalArgumentException("max не может быть меньше min.");
        }
    }

    public double randomValue(double min, double max) {
        validateRange(min, max);
        return min + ThreadLocalRandom.current().nextDouble() * (max - min);
    }

    public float randomValue(float min, float max) {
        validateRange(min, max);
        return min + ThreadLocalRandom.current().nextFloat() * (max - min);
    }

    public double calcDiff(double a, double b) {
        return a - b;
    }

    public float deltaTime() {
        float debugFPS = Minecraft.getDebugFPS();
        if (debugFPS > 0) {
            return 1.0F / debugFPS;
        } else {
            return 1.0F;
        }
    }


    public String formatTime(long millis) {
        long hours = millis / 3600000;
        long minutes = (millis % 3600000) / 60000;
        long seconds = ((millis % 360000) % 60000) / 1000;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public double round(double value, int increment) {
        double num = Math.pow(10, increment);
        return Math.round(value * num) / num;
    }

    public double round(final double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public double step(double value, double steps) {
        double roundedValue = Math.round(value / steps) * steps;
        return Math.round(roundedValue * 100.0) / 100.0;
    }

    public double clamp(double min, double max, double value) {
        return Math.max(min, Math.min(max, value));
    }


    public float clamp(float min, float max, float value) {
        return Math.max(min, Math.min(max, value));
    }

    public int clamp(int min, int max, int value) {
        return Math.max(min, Math.min(max, value));
    }

    public double clamp01(double value) {
        return clamp(0.0D, 1.0D, value);
    }

    public float clamp01(float value) {
        return clamp(0.0F, 1.0F, value);
    }

    public double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double deltaX = calcDiff(x2, x1);
        double deltaY = calcDiff(y2, y1);
        double deltaZ = calcDiff(z2, z1);
        return MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public double getDistance(BlockPos pos1, BlockPos pos2) {
        double deltaX = calcDiff(pos1.getX(), pos2.getX());
        double deltaY = calcDiff(pos1.getY(), pos2.getY());
        double deltaZ = calcDiff(pos1.getZ(), pos2.getZ());
        return MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public static float limit(float current, float inputMin, float inputMax, float outputMin, float outputMax) {
        current = Mathf.clamp(inputMin, inputMax, current);
        float distancePercentage = (current - inputMin) / (inputMax - inputMin);
        return Interpolator.lerp(outputMin, outputMax, distancePercentage);
    }
}