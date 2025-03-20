package org.obsidian.client.screen.flatgui.impl.buttons;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.screen.flatgui.AbstractPanel;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;
import org.obsidian.client.utils.render.font.Font;
import org.obsidian.client.utils.render.font.Fonts;

@Getter
@Setter
@Accessors(fluent = true)
public class CategoryButton extends AbstractPanel {
    private final Category category;

    public CategoryButton(Category category) {
        this.category = category;
        size().set(24F);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {

    }

    @Override
    public void init() {
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        hover().update();
        hover().run(hovered(mouseX, mouseY) ? 1F : 0F, 0.25F, Easings.SINE_OUT, true);
        RenderUtil.Rounded.smooth(matrix, position().x, position().y, size().x, size().y, ColorUtil.getColor(15, 15, 20, alpha()), Round.of(4));
        Font font = Fonts.CLICKGUI;
        float fontSize = 10F + (hover().get() * 3F);
        font.drawCenter(matrix, category.getIcon(), position().x + size().x / 2F, position().y + 1F + size().y / 2F - fontSize / 2F, themeColor(), fontSize);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered(mouseX, mouseY)) {
            flatgui().selectedCategory(category);
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
