package org.obsidian.client.screen.clickgui.component.setting;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.interfaces.IMouse;
import org.obsidian.client.managers.module.settings.Setting;
import org.obsidian.client.managers.module.settings.impl.DelimiterSetting;
import org.obsidian.client.screen.clickgui.component.WindowComponent;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.font.Font;
import org.obsidian.client.utils.render.font.Fonts;
import org.obsidian.client.utils.render.text.TextUtils;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
public abstract class SettingComponent extends WindowComponent implements IMouse {
    public Setting<?> value;
    public final Font font = Fonts.SF_SEMIBOLD;
    public final float fontSize = 6;
    public float margin = 4;
    private final String splitter = "-";

    public SettingComponent(final Setting<?> value) {
        this.value = value;
        size.set(100, 20);
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        hoverAnimation.update();
    }

    public float drawName(MatrixStack matrix, int mouseX, int mouseY, float width) {
        boolean isHover = isHover(mouseX, mouseY, position.x, position.y + margin, width, valueHeight());

        hoverAnimation.run(isHover ? 1 : 0, 0.5, Easings.QUAD_OUT);

        int hoverColor = accentColor();
        int defaultColor = ColorUtil.getColor(230, alpha());

        boolean isDelimiter = value instanceof DelimiterSetting;

        int finalColor = isDelimiter ? accentColor() : hoverAnimation().isFinished() && hoverAnimation.getValue() == 0.0 ? defaultColor : ColorUtil.overCol(defaultColor, hoverColor, hoverAnimation.get());

        return font.drawSplitted(matrix, value.getName(), splitter, position.x, position.y + margin, width, finalColor, fontSize);
    }

    public float valueHeight() {
        return TextUtils.splitLineHeight(value.getName(), font, fontSize, size.x, splitter) * fontSize;
    }


}