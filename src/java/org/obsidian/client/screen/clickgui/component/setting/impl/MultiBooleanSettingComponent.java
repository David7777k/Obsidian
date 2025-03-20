package org.obsidian.client.screen.clickgui.component.setting.impl;

import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.screen.clickgui.component.setting.SettingComponent;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;

public class MultiBooleanSettingComponent extends SettingComponent {
    private final MultiBooleanSetting value;

    public MultiBooleanSettingComponent(MultiBooleanSetting value) {
        super(value);
        this.value = value;
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {

    }

    @Override
    public void init() {

    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
        float valueHeight = drawName(matrix, mouseX, mouseY, size.x);

        float wOffset = 0;
        float hOffset = 0;
        float append = 3;
        float out = 1;

        RenderUtil.Rounded.smooth(matrix, position.x, position.y + margin + valueHeight + margin, size.x, size.y - (margin + valueHeight + margin) - margin - (margin / 2F), backColor(), Round.of(2));

        for (BooleanSetting setting : value.getValues()) {
            if (!setting.getVisible().get()) continue;
            setting.getAnimation().update();
            setting.getAnimation().run(value.getValue(setting.getName()) ? 1 : 0, 0.25, Easings.LINEAR, true);
            float tOffset = font.getWidth(setting.getName(), fontSize) + append;
            if (wOffset + tOffset >= size.x - (margin * 2)) {
                wOffset = 0;
                hOffset += fontSize + append;
            }
            RenderUtil.Rounded.smooth(matrix, position.x + margin + wOffset - out, position.y + margin + valueHeight + margin + hOffset + margin / 2F, font.getWidth(setting.getName(), fontSize) + (out * 2), fontSize + (out * 2), frontColor(), Round.of(2));
            font.draw(matrix, setting.getName(), position.x + margin + wOffset, position.y + margin + valueHeight + margin + hOffset + margin / 2F + out, (ColorUtil.overCol(getWhite(), accentColor(), setting.getAnimation().get())), fontSize);
            wOffset += tOffset;
        }

        size.y = margin + valueHeight + margin + hOffset + fontSize + (margin * 2F) + margin;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {

        float wOffset = 0;
        float hOffset = 0;
        float append = 3;
        float out = 1;

        for (BooleanSetting setting : value.getValues()) {
            if (!setting.getVisible().get()) continue;
            float tOffset = font.getWidth(setting.getName(), fontSize) + append;
            if (wOffset + tOffset >= size.x - (margin * 2)) {
                wOffset = 0;
                hOffset += fontSize + append;
            }
            if (isLClick(button) && isHover(mouseX, mouseY, position.x + margin + wOffset - out, position.y + margin + valueHeight() + margin + hOffset + margin / 2F, font.getWidth(setting.getName(), fontSize) + (out * 2), fontSize + (out * 2))) {
                value.get(setting.getName()).set(!setting.getValue());
            }
            wOffset += tOffset;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
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

    }
}