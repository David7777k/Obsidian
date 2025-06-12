package org.obsidian.client.ui.dropdown;


import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;

public class DropDown extends Screen {
    public DropDown() {
        super(StringTextComponent.EMPTY);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
    }
}
