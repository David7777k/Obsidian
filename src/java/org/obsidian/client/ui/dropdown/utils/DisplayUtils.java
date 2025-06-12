package org.obsidian.client.ui.dropdown.utils;

import net.minecraft.util.math.vector.Vector4f;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;
import org.obsidian.client.ui.dropdown.utils.Vector4i;

public class DisplayUtils {
    public static void drawShadow(float x, float y, float w, float h, float r, int c) {
        RenderUtil.Shadow.drawShadow(new MatrixStack(), x, y, w, h, r, c);
    }

    public static void drawRoundedRect(float x, float y, float w, float h, float r, int c) {
        RenderUtil.Rounded.roundedRect(new MatrixStack(), x, y, w, h, c, Round.of(r));
    }

    public static void drawRoundedRect(float x, float y, float w, float h, Object rounding, int c) {
        Round round = toRound(rounding);
        RenderUtil.Rounded.roundedRect(new MatrixStack(), x, y, w, h, c, round);
    }

    public static void drawRoundedRect(float x, float y, float w, float h, Object rounding, int c1, int c2, int c3, int c4) {
        Round round = toRound(rounding);
        RenderUtil.Rounded.roundedRect(new MatrixStack(), x, y, w, h, c1, c2, c3, c4, round);
    }

    public static void drawCircle(float x, float y, float r, int c) {
        RenderUtil.Rounded.roundedRect(new MatrixStack(), x - r, y - r, r * 2, r * 2, c, Round.of(r));
    }

    public static void drawShadowCircle(float x, float y, float r, int c) {
        RenderUtil.Shadow.drawShadow(new MatrixStack(), x - r, y - r, r * 2, r * 2, r, c);
    }

    public static void drawRectVerticalW(float x, float y, float w, float h, int c1, int c2) {
        RectUtil.drawGradientV(new MatrixStack(), x, y, w, h, c1, c2, false);
    }

    public static void drawRoundedRect(float v, float y, float v1, float v2, Vector4f roundingVector, Vector4i vector4i) {
        Round round = Round.of(roundingVector.x, roundingVector.y, roundingVector.z, roundingVector.w);
        RenderUtil.Rounded.roundedRect(new MatrixStack(), v, y, v1, v2, vector4i.x, vector4i.y, vector4i.z, vector4i.w, round);
    }

    private static Round toRound(Object obj) {
        if (obj instanceof Round) {
            return Round.of((Round) obj);
        } else if (obj instanceof Float) {
            return Round.of((Float) obj);
        } else if (obj instanceof Vector4f vec) {
            return Round.of(vec.x, vec.y, vec.z, vec.w);
        }
        return Round.zero();
    }
}
