package org.obsidian.client.ui.dropdown.impl;

import lombok.Getter;
import lombok.Setter;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.screen.clickgui.component.Panel;


@Getter
@Setter
public abstract class Component implements IBuilder {

    private float x, y, width, height;
    private Panel panel;

    public boolean isHovered(float mouseX, float mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isHovered(float mouseX, float mouseY, float height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public abstract void render(MatrixStack stack, float mouseX, float mouseY);

    public boolean isVisible() {
        return true;
    }

}
