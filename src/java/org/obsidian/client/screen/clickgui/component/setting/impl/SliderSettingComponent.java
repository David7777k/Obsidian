package org.obsidian.client.screen.clickgui.component.setting.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.screen.clickgui.component.setting.SettingComponent;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;

public class SliderSettingComponent extends SettingComponent {
    private final SliderSetting value;
    private final SliderSetting localValue;

    public SliderSettingComponent(SliderSetting value) {
        super(value);
        this.value = this.localValue = value;
    }

    private boolean drag;

    @Override
    public void resize(Minecraft minecraft, int width, int height) {

    }

    @Override
    public void init() {
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
        value.getAnimation().update();

        float sliderHeight = 2;

        String currentValue = String.valueOf(localValue.getValue());

        float valueHeight = drawName(matrix, mouseX, mouseY, size.x);

        value.getAnimation().run(Mathf.step(size.x * (localValue.getValue() - value.min) / (value.max - value.min), value.increment / size.x), 0.25, Easings.CUBIC_OUT, true);

        int backColorDark = ColorUtil.multDark(accentColor(), 0.25F);
        int backColor = ColorUtil.overCol(backColorDark, accentColor(), value.getAnimation().get());

        RenderUtil.Rounded.smooth(matrix, position.x, position.y + margin + valueHeight + margin, size.x, sliderHeight, backColor(), Round.of(sliderHeight / 2F));
        RenderUtil.Rounded.smooth(matrix, position.x, position.y + margin + valueHeight + margin, (float) value.getAnimation().getValue(), sliderHeight, backColor, backColor, backColorDark, backColorDark, Round.of(sliderHeight / 2F));
        float circleSize = drag ? 5 : 4;
        RenderUtil.Rounded.smooth(matrix, position.x + (float) (value.getAnimation().getValue()) - (circleSize / 2F), position.y + margin + valueHeight + margin - (circleSize / 2F) + (sliderHeight / 2F), circleSize, circleSize, getWhite(), Round.of((circleSize / 2F)));
        RenderUtil.Rounded.smooth(matrix, position.x + (float) (value.getAnimation().getValue()) - (circleSize / 4F), position.y + margin + valueHeight + margin - (circleSize / 4F) + (sliderHeight / 2F), circleSize / 2F, circleSize / 2F, accentColor(), Round.of((circleSize / 4F)));

        if (drag) {
            localValue.set((float) MathHelper.clamp(Mathf.step((mouseX - position.x) / size.x * (value.max - value.min) + value.min, value.increment), value.min, value.max));
        }

        font.draw(matrix, String.valueOf(value.min), position.x, position.y + margin + valueHeight + margin + sliderHeight + margin, ColorUtil.multDark(getWhite(), 0.75F), fontSize);
        font.drawCenter(matrix, currentValue, position.x + size.x / 2F, position.y + margin + valueHeight + margin + sliderHeight + margin, getWhite(), fontSize);
        font.drawRight(matrix, String.valueOf(value.max), position.x + size.x, position.y + margin + valueHeight + margin + sliderHeight + margin, ColorUtil.multDark(getWhite(), 0.75F), fontSize);


        size.y = margin + valueHeight + margin + sliderHeight + margin + fontSize + margin;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHover(mouseX, mouseY, position.x, position.y + margin, size.x, size.y - (margin * 2F))) {
            drag = true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        drag = false;
        value.set(localValue.getValue());
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    @Override
    public void onClose() {
        drag = false;
        value.set(localValue.getValue());
    }

}