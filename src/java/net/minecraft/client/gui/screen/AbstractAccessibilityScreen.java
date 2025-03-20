package net.minecraft.client.gui.screen;

import net.minecraft.client.AbstractOption;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.OptionsRowList;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;

import java.util.List;

public abstract class AbstractAccessibilityScreen extends SettingsScreen {
    private final AbstractOption[] abstractOptions;
    private OptionsRowList optionsRowList;

    public AbstractAccessibilityScreen(Screen screen, GameSettings gameSettings, ITextComponent textComponent, AbstractOption[] abstractOptions) {
        super(screen, gameSettings, textComponent);
        this.abstractOptions = abstractOptions;
    }

    protected void init() {
        this.optionsRowList = new OptionsRowList(this.minecraft, this.width, this.height, 32, this.height - 32, 25);
        this.optionsRowList.addOptions(this.abstractOptions);
        this.children.add(this.optionsRowList);
        this.addButton(new Button(this.width / 2 - 100, this.height - 27, 200, 20, DialogTexts.GUI_DONE, (p_243316_1_) ->
        {
            this.minecraft.displayScreen(this.parentScreen);
        }));
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        this.optionsRowList.render(matrixStack, mouseX, mouseY, partialTicks);
        drawCenteredStringWithShadow(matrixStack, this.font, this.title, this.width / 2, 20, 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        List<IReorderingProcessor> list = func_243293_a(this.optionsRowList, mouseX, mouseY);

        if (list != null) {
            this.renderTooltip(matrixStack, list, mouseX, mouseY);
        }
    }
}
