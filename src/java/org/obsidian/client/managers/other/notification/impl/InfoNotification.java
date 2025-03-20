package org.obsidian.client.managers.other.notification.impl;

import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.managers.other.notification.Notification;
import org.obsidian.client.utils.animation.util.Easing;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.render.color.ColorUtil;

public class InfoNotification extends Notification {
    public InfoNotification(String content, long delay, int index) {
        super(content, delay, index);
        animationY.set(index);
    }

    @Override
    public void render(MatrixStack matrix, int multiplier) {
        Theme theme = Theme.getInstance();
        boolean finished = finished();

        animation.update();
        animationY.update();

        Easing easing = finished ? Easings.EXPO_IN : Easings.EXPO_OUT;

        float margin = 5;
        float width = margin + font.getWidth(content, fontSize) + margin;
        float height = margin + fontSize + margin;
        animation.run(finished ? 0 : 1, (wait / 1000F) / 2F, easing, true);
        animationY.run(multiplier, (wait / 1000F) / 2F, Easings.CUBIC_OUT, true);

        float x = width() - margin - (width * animation.get());
        float y = height() - height - 32 - ((animationY.get() * (height + (margin / 2F))));

        float alpha = (float) Math.pow(animation.get(), 4);

        matrix.push();
        matrix.translate((x + width), (y + height / 2F), 0);
        matrix.scale(1, animation.get(), 1);
        matrix.translate(-(x + width), -(y + height / 2F), 0);
        theme.drawClientRect(matrix, x, y, width, height, alpha, 4);
        font.drawOutline(matrix, content, x + margin, y + margin, ColorUtil.multAlpha(theme.textColor(), alpha), fontSize);
        matrix.pop();
    }

}