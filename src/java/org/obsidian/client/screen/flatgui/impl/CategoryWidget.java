package org.obsidian.client.screen.flatgui.impl;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.screen.flatgui.AbstractPanel;
import org.obsidian.client.screen.flatgui.impl.buttons.CategoryButton;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;

import java.util.List;

@Getter
@Setter
@Accessors(fluent = true)
public class CategoryWidget extends AbstractPanel {
    private final List<CategoryButton> categories = Lists.newArrayList();

    public CategoryWidget() {
        for (Category category : Category.values()) {
            categories.add(new CategoryButton(category));
        }
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        categories.forEach(category -> category.resize(minecraft, width, height));
    }

    @Override
    public void init() {
        categories.forEach(CategoryButton::init);
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {

        float margin = 5F;


        float centerSize = size().y / 2F - (categories.size() * (categories().stream().findFirst().get().size().y + margin) - margin) / 2F;
        RenderUtil.Rounded.smooth(matrix, position().x, position().y + centerSize - margin, size().x, centerSize * 2F + margin * categories.size(), ColorUtil.getColor(25, 25, 35, alpha() / 2F), Round.of(0, 0, 8, 8));

        float offset = 0;
        for (CategoryButton category : categories) {
            category.position().set(position().x + margin, position().y + centerSize + offset);
            category.render(matrix, mouseX, mouseY, partialTicks);
            offset += category.size().y + margin;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        categories.forEach(category -> category.mouseClicked(mouseX, mouseY, button));
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        categories.forEach(category -> category.mouseReleased(mouseX, mouseY, button));
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        categories.forEach(category -> category.keyPressed(keyCode, scanCode, modifiers));
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        categories.forEach(category -> category.keyReleased(keyCode, scanCode, modifiers));
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        categories.forEach(category -> category.charTyped(codePoint, modifiers));
        return false;
    }

    @Override
    public void onClose() {
        categories.forEach(CategoryButton::onClose);
    }
}
