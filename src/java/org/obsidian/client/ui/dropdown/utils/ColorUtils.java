package org.obsidian.client.ui.dropdown.utils;

import org.obsidian.client.utils.render.color.ColorUtil;

public class ColorUtils {
    public static int rgb(int r, int g, int b) {
        return ColorUtil.getColor(r, g, b);
    }

    public static int rgba(int r, int g, int b, int a) {
        return ColorUtil.getColor(r, g, b, a);
    }

    public static int interpolate(int c1, int c2, float pct) {
        return ColorUtil.overCol(c1, c2, pct);
    }

    public static int setAlpha(int color, int alpha) {
        return ColorUtil.replAlpha(color, alpha);
    }
}
