package org.obsidian.client.screen.clickgui.component;

import lombok.Data;
import lombok.experimental.Accessors;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.interfaces.IScreen;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.screen.clickgui.ClickGuiScreen;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.font.Font;
import org.obsidian.client.utils.render.font.Fonts;
import org.joml.Vector2f;

@Data
@Accessors(fluent = true)
public abstract class WindowComponent implements IScreen {
    public Vector2f position = new Vector2f();
    public Vector2f size = new Vector2f();
    public final Animation hoverAnimation = new Animation();
    public final Animation expandAnimation = new Animation();
    public final Font font = Fonts.SF_MEDIUM;
    public final float moduleFontSize = 7;
    public final float categoryFontSize = 8;

    public float outline = 2F;
    public float round = 4F;

    public boolean isHover(double mouseX, double mouseY) {
        return isHover(mouseX, mouseY, position.x, position.y, size.x, size.y);
    }

    public int alpha() {
        return Math.round(alphaPC() * 255F);
    }

    public float alphaPC() {
        return Mathf.clamp01(clickgui().alpha().get());
    }

    public int backgroundColor() {
        return ColorUtil.getColor(20, 20, 28, alphaPC() / 1.5F);
    }

    public int getWhite() {
        return ColorUtil.getColor(200, alpha());
    }

    public int backColor() {
        return ColorUtil.getColor(12, 12, 18, alphaPC() / 2);
    }

    public int frontColor() {
        return ColorUtil.overCol(backColor(), accentColor(), 0.075F);
    }

    public int accentColor() {
        Theme theme = Theme.getInstance();
        return ColorUtil.multAlpha(theme.textColor(), alphaPC());
    }

    public boolean isHover(int mouseX, int mouseY) {
        return isHover(mouseX, mouseY, position.x, position.y, size.x, size.y);
    }

    public ClickGuiScreen clickgui() {
        return Obsidian.inst().clickGui();
    }

    public Panel panel() {
        return clickgui().panel();
    }
}
