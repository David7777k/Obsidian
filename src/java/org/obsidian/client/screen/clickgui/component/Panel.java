package org.obsidian.client.screen.clickgui.component;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Namespaced;
import net.minecraft.util.ResourceLocation;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.interfaces.IScreen;
import org.obsidian.client.api.interfaces.IWindow;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.screen.clickgui.ClickGuiScreen;
import org.obsidian.client.screen.clickgui.component.category.CategoryComponent;
import org.obsidian.client.screen.clickgui.component.module.ModuleComponent;
import org.obsidian.client.utils.keyboard.Keyboard;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.scroll.ScrollUtil;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Panel implements IScreen, IWindow {
    private final ClickGuiScreen clickGui;
    private final ResourceLocation avatar = new Namespaced("texture/avatar.png");
    private final List<CategoryComponent> categoryComponents = new ArrayList<>();
    private final ScrollUtil scrollUtilV = new ScrollUtil();
    private final ScrollUtil scrollUtilH = new ScrollUtil();
    @Setter
    public ModuleComponent expandedModule = null;
    private boolean firstInit = true;

    public Panel(ClickGuiScreen clickGui) {
        this.clickGui = clickGui;

        float posX = 0;
        for (Category category : Category.values()) {
            CategoryComponent component = new CategoryComponent(category, clickGui);
            component.position().set(posX, 0);
            categoryComponents.add(component);
            posX += clickGui.categoryWidth() + clickGui.categoryOffset();
        }

    }

    private void setPosition() {
        double categoryHeight = categoryComponents.stream()
                .mapToDouble(CategoryComponent::getModuleHeight)
                .max()
                .orElse(0) + clickGui.categoryHeight();


        scrollUtilH.setMax(scaled().x + 2 * (categoryComponents.size() * (clickGui.categoryWidth() + clickGui.categoryOffset()) - clickGui.categoryOffset()), (categoryComponents.size() * (clickGui.categoryWidth() + clickGui.categoryOffset()) - clickGui.categoryOffset()));
        scrollUtilV.setMax(scaled().y, (float) -categoryHeight);

        scrollUtilH.setEnabled(false);
        scrollUtilH.setAutoReset(false);
        scrollUtilH.setWheel(scrollUtilH.getMax() / 2F);
        scrollUtilH.setTarget(scrollUtilH.getMax() / 2F);
        scrollUtilH.getAnimation().set(scrollUtilH.getMax() / 2F);
        scrollUtilV.setEnabled(false);
        scrollUtilV.setAutoReset(false);
        scrollUtilV.setWheel(scrollUtilV.getMax() * 0.5F);
        scrollUtilV.setTarget(scrollUtilV.getMax() * 0.5F);
        scrollUtilV.getAnimation().set(scrollUtilV.getMax() * 0.25F);

    }


    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        setPosition();
        for (CategoryComponent component : categoryComponents) {
            component.resize(minecraft, width, height);
        }
    }

    @Override
    public void init() {
        if (firstInit) {
            setPosition();
            firstInit = false;
        }

        for (CategoryComponent component : categoryComponents) {
            component.init();
        }
    }

    private void componentMove(MatrixStack matrixStack) {
        double categoryHeight = categoryComponents.stream()
                .mapToDouble(CategoryComponent::getModuleHeight)
                .max()
                .orElse(0) + clickGui.categoryHeight();

        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT_SHIFT.getKey())) {
            scrollUtilV.setEnabled(false);
            scrollUtilV.setAutoReset(false);
            scrollUtilH.setEnabled(true);
            scrollUtilH.setMax(scaled().x + 2 * (categoryComponents.size() * (clickGui.categoryWidth() + clickGui.categoryOffset()) - clickGui.categoryOffset()), (categoryComponents.size() * (clickGui.categoryWidth() + clickGui.categoryOffset()) - clickGui.categoryOffset()));
        } else {
            scrollUtilH.setEnabled(false);
            scrollUtilH.setAutoReset(false);
            scrollUtilV.setEnabled(true);
            scrollUtilV.setMax(scaled().y, (float) -categoryHeight);
        }
        scrollUtilH.update();
        scrollUtilV.update();
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        RectUtil.drawRect(matrix, 0, 0, width(), height(), ColorUtil.getColor(0, clickGui.alpha().get() / 2F));

        componentMove(matrix);

        double categoryHeight = categoryComponents.stream()
                .mapToDouble(CategoryComponent::getModuleHeight)
                .max()
                .orElse(0) + clickGui.categoryHeight();

        double categoryWidth = (categoryComponents.size() * (clickGui.categoryWidth() + clickGui.categoryOffset()) - clickGui.categoryOffset());

        float x = (scaled().x + scrollUtilH.getWheel() + (categoryComponents.size() * (clickGui.categoryWidth() + clickGui.categoryOffset()))) - (float) (categoryWidth / 2F);
        float y = (scaled().y + scrollUtilV.getWheel()) - (float) (categoryHeight / 2F);

        float scale = clickGui.scale().get();

        matrix.push();
        matrix.translate(x, y + (categoryHeight), 0);
        matrix.scale(scale, scale, 0);
        matrix.translate(-x, -(y + (categoryHeight)), 0);
        renderComponents(matrix, mouseX, mouseY, partialTicks);
        matrix.pop();
    }

    private void renderComponents(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        float offset = 0, y = scaled().y;
        for (CategoryComponent component : categoryComponents) {
            float x = ((scaled().x + offset) + scrollUtilH.getWheel() + ((categoryComponents.size() * (clickGui.categoryWidth() + clickGui.categoryOffset()) - clickGui.categoryOffset()))) - (categoryComponents.size() * (clickGui.categoryWidth() + clickGui.categoryOffset()) - clickGui.categoryOffset());

            component.position().set(x, y + scrollUtilV.getWheel());
            component.render(matrix, mouseX, mouseY, partialTicks);
            offset += clickGui.categoryWidth() + clickGui.categoryOffset();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (CategoryComponent component : categoryComponents) {
            component.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (CategoryComponent component : categoryComponents) {
            component.mouseReleased(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (CategoryComponent component : categoryComponents) {
            component.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (CategoryComponent component : categoryComponents) {
            component.keyReleased(keyCode, scanCode, modifiers);
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        for (CategoryComponent component : categoryComponents) {
            component.charTyped(codePoint, modifiers);
        }
        return false;
    }

    @Override
    public void onClose() {
        for (CategoryComponent component : categoryComponents) {
            component.onClose();
        }
    }
}