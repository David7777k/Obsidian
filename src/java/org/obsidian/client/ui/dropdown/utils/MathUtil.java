package org.obsidian.client.ui.dropdown.utils;

public class MathUtil {
    public static boolean isHovered(float mx, float my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    public static float fast(float current, float target, float speed) {
        return current + (target - current) / speed;
    }

    public static double round(double value, double inc) {
        return Math.round(value / inc) * inc;
    }
}
