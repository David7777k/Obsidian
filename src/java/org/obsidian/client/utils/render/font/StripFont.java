package org.obsidian.client.utils.render.font;

import net.minecraft.util.text.ITextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.draw.StencilUtil;
import org.obsidian.lib.util.time.StopWatch;

public class StripFont implements IMinecraft {
    private final Animation animation = new Animation();
    private final StopWatch time = new StopWatch();

    public void draw(Font font, MatrixStack matrix, String text, float x, float y, float width, int color, float size, double duration) {
        // Кирюша сори за костыль
        update(duration);
        boolean enabledFromThisClass = false;
        if (!StencilUtil.enabled) {
            StencilUtil.enable();
            RectUtil.drawRect(matrix, x, y - size / 4.0F, width, size * 1.5F, -1);
            StencilUtil.read(StencilUtil.Action.OUTSIDE.getAction());
            enabledFromThisClass = true;
        }
        float fontWidth = font.getWidth(text, size);
        float offset = Math.max(0.0F, fontWidth - width) * animation.get();
        font.draw(matrix, text, x - offset, y, color, size);
        if (enabledFromThisClass) StencilUtil.disable();
    }

    public void draw(Font font, MatrixStack matrix, ITextComponent text, float x, float y, float width, int color, float size, double duration) {
        update(duration);
        StencilUtil.enable();
        RectUtil.drawRect(matrix, x, y - size / 4.0F, width, size * 1.5F, -1);
        StencilUtil.read(StencilUtil.Action.OUTSIDE.getAction());
        float fontWidth = font.getWidth(text, size);
        float offset = Math.max(0.0F, fontWidth - width) * animation.get();
        font.drawTextComponent(matrix, text, x - offset, y, color, false, size);
        StencilUtil.disable();
    }

    private void update(double duration) {
        animation.update();
        double timeDuration = duration * 2000.0F;
        if (animation.isFinished() && time.finished(timeDuration)) {
            float targetValue = animation.get() == 1.0F ? 0.0F : 1.0F;
            animation.run(targetValue, duration, Easings.SINE_IN_OUT);
            time.reset();
        }
    }
}