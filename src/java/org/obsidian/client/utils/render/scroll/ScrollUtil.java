package org.obsidian.client.utils.render.scroll;

import lombok.Getter;
import lombok.Setter;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.joml.Vector2f;

@Getter
@Setter
public class ScrollUtil implements IMinecraft {
    private float target, max, wheel;
    private boolean enabled;
    private boolean autoReset;
    private float speed = 5F;
    private final Animation animation = new Animation();

    public ScrollUtil() {
        enabled = true;
        autoReset = true;
    }

    public void update() {
        if (enabled) {
            double wheel = mc.mouseHelper.getWheel() * (speed * 10F);
            double stretch = 0;
            target = (float) Math.min(Math.max(target + wheel / 2, max - (wheel == 0 ? 0 : stretch)), (wheel == 0 ? 0 : stretch));
            resetWheel();
        }
        if (autoReset) resetWheel();
        animation.update();
        animation.run(target, 0.25F, Easings.QUAD_OUT, true);
        wheel = (float) animation.getValue();
    }

    public void renderV(MatrixStack matrixStack, Vector2f position, float maxHeight, float alpha) {
        float percentage = (getWheel() / getMax());
        float barHeight = maxHeight - ((getMax() / (getMax() - maxHeight)) * maxHeight);
        boolean allowed = (barHeight < maxHeight);
        if (!allowed) return;
        float scrollX = position.x;
        float scrollY = position.y + (maxHeight * percentage) - (barHeight * percentage);
        RectUtil.drawRect(matrixStack, scrollX, scrollY, 0.5F, barHeight, ColorUtil.getColor(255, alpha));

    }

    public void renderH(MatrixStack matrixStack, Vector2f position, float maxWidth, float alpha) {
        float percentage = (getWheel() / getMax());
        float barWidth = maxWidth - ((getMax() / (getMax() - maxWidth)) * maxWidth);
        boolean allowed = (barWidth < maxWidth);
        if (!allowed) return;
        float scrollX = position.x + (maxWidth * percentage) - (barWidth * percentage);
        float scrollY = position.y;
        RectUtil.drawRect(matrixStack, scrollX, scrollY, barWidth, 0.5F, ColorUtil.getColor(255, alpha));
    }

    public void reset() {
        this.animation.set(this.wheel = this.target = 0F);
    }

    private void resetWheel() {
        mc.mouseHelper.setWheel(0F);
    }

    public void setMax(float max, float value) {
        this.max = -max + value;
    }
}