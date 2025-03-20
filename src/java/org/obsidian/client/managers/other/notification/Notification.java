package org.obsidian.client.managers.other.notification;

import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.interfaces.IWindow;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.render.font.Font;
import org.obsidian.client.utils.render.font.Fonts;
import org.obsidian.lib.util.time.StopWatch;

public abstract class Notification implements IWindow {
    protected final String content;
    protected final long delay, wait = 500;
    protected final int index;
    protected final Font font = Fonts.SF_MEDIUM;
    protected final float fontSize = 6;
    protected final Animation animationY = new Animation();
    protected final Animation animation = new Animation();
    protected final StopWatch time = new StopWatch();

    public Notification(String content, long delay, int index) {
        this.content = content;
        this.delay = delay;
        this.index = index;
    }

    public abstract void render(MatrixStack matrix, final int multiplier);

    public boolean finished() {
        return time.finished(wait + delay);
    }

    public boolean hasExpired() {
        return time.finished(wait + delay + wait);
    }
}
