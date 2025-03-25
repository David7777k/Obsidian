package org.obsidian.client.screen.mainmenu;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MultiplayerScreen;
import net.minecraft.client.gui.screen.OptionsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.WorldSelectionScreen;
import net.minecraft.util.text.StringTextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.client.Constants;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.api.interfaces.IScreen;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.render.color.ColorFormatting;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;
import org.obsidian.client.utils.render.font.Fonts;

import java.util.List;

public class MainMenu extends Screen implements IMinecraft {
    private final List<Button> buttons = Lists.newArrayList();
    private final Animation alpha = new Animation();

    public MainMenu() {
        super(StringTextComponent.EMPTY);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        buttons.forEach(btn -> btn.resize(minecraft, width, height));
    }

    @Override
    protected void init() {
        super.init();
        alpha.set(0F);
        alpha.run(1.0, 0.5);

        float margin = 5;
        float buttonWidth = 200F;
        float buttonHeight = 25F;

        buttons.clear();

        buttons.add(new Button(new Vector2f(width / 2F - buttonWidth / 2F, height / 2F - buttonHeight / 2F - margin - buttonHeight), new Vector2f(buttonWidth, buttonHeight), "multiplayer",
                new Vector2i(ColorUtil.randomColor(), ColorUtil.randomColor()),
                (button) -> mc.displayScreen(new MultiplayerScreen(this))));

        buttons.add(new Button(new Vector2f(width / 2F - buttonWidth / 2F, height / 2F - buttonHeight / 2F), new Vector2f(buttonWidth, buttonHeight), "singleplayer",
                new Vector2i(ColorUtil.randomColor(), ColorUtil.randomColor()),
                (button) -> mc.displayScreen(new WorldSelectionScreen(this))));

        buttons.add(new Button(new Vector2f(width / 2F - buttonWidth / 2F, height / 2F + buttonHeight / 2F + margin), new Vector2f((buttonWidth / 2F) - (margin / 2F), buttonHeight), "settings",
                new Vector2i(ColorUtil.randomColor(), ColorUtil.randomColor()),
                (button) -> mc.displayScreen(new OptionsScreen(this, this.minecraft.gameSettings))));

        buttons.add(new Button(new Vector2f(width / 2F + (margin / 2F), height / 2F + buttonHeight / 2F + margin), new Vector2f((buttonWidth / 2F) - (margin / 2F), buttonHeight), "exit",
                new Vector2i(ColorUtil.randomColor(), ColorUtil.randomColor()),
                (button) -> this.minecraft.shutdown()));

        buttons.add(new Button(new Vector2f(width / 2F - buttonWidth / 4F, height / 2F + buttonHeight / 2F + margin + buttonHeight + margin), new Vector2f(buttonWidth / 2F, 20), "account",
                new Vector2i(ColorUtil.randomColor(), ColorUtil.randomColor()),
                (button) -> mc.displayScreen(Obsidian.inst().accountGui())));

        buttons.forEach(Button::init);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        alpha.update();
        RenderUtil.drawShaderBackground(matrixStack, 1F);

        int textColor = ColorUtil.getColor(200, 200, 230, alpha.get());
        int accentColor = ColorUtil.multAlpha(Theme.getInstance().clientColor(), alpha.get());

        float darker = 0.5F;

        StringBuilder fadeTitle = new StringBuilder();
        String namespace = Constants.NAMESPACE;
        for (int i = 0; i < namespace.length(); i++) {
            fadeTitle.append(ColorFormatting.getColor(ColorUtil.fade(5, i * 20, accentColor, ColorUtil.multDark(accentColor, darker))));
            fadeTitle.append(namespace.charAt(i));
        }

        String title = "Добро пожаловать в " + fadeTitle;
        Fonts.SF_REGULAR.drawCenter(matrixStack, title, width / 2F, height / 2F - 75, textColor, 10);

        StringBuilder fadeExcellence = new StringBuilder();
        String excellence = "совершенству";
        for (int i = 0; i < excellence.length(); i++) {
            fadeExcellence.append(ColorFormatting.getColor(ColorUtil.fade(5, i * 20, accentColor, ColorUtil.multDark(accentColor, darker))));
            fadeExcellence.append(excellence.charAt(i));
        }

        String subtitle = fadeTitle + ColorFormatting.reset() + " это ваш путь к " + fadeExcellence + "!";
        Fonts.SF_BOLD.drawCenter(matrixStack, subtitle, width / 2F, height - 6 - 5, ColorUtil.multDark(textColor, darker), 6);

        StringBuilder fadeUsername = new StringBuilder();
        String username = mc.session.getProfile().getName();
        for (int i = 0; i < username.length(); i++) {
            fadeUsername.append(ColorFormatting.getColor(ColorUtil.fade(5, i * 20, accentColor, ColorUtil.multDark(accentColor, darker))));
            fadeUsername.append(username.charAt(i));
        }

        String account = "вы авторизованы как " + fadeUsername;
        Fonts.SF_BOLD.drawCenter(matrixStack, account, width / 2F, height - 6 - 5 - 6, ColorUtil.multDark(textColor, darker), 6);

        buttons.forEach(btn -> {
            btn.alpha(alpha.get());
            btn.render(matrixStack, mouseX, mouseY, partialTicks);
        });
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        buttons.forEach(btn -> btn.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        buttons.forEach(btn -> btn.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        buttons.forEach(btn -> btn.keyPressed(keyCode, scanCode, modifiers));
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        buttons.forEach(btn -> btn.keyReleased(keyCode, scanCode, modifiers));
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        buttons.forEach(btn -> btn.charTyped(codePoint, modifiers));
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        buttons.forEach(Button::onClose);
    }

    @Data
    @Accessors(fluent = true)
    @RequiredArgsConstructor
    public static final class Button implements IScreen {
        private final Vector2f position;
        private final Vector2f size;
        private final String text;
        private final Vector2i glowColor;
        private final IPressable action;
        private Animation hover = new Animation();
        private float alpha = 0;

        @Override
        public void resize(Minecraft minecraft, int width, int height) {

        }

        @Override
        public void init() {

        }

        @Override
        public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
            float fontSize = 8;

            hover.update();

            boolean hovered = isHover(mouseX, mouseY, position.x, position.y, size.x, size.y);

            hover.run(hovered ? 1.0 : 0.0, 0.25, Easings.SINE_OUT, true);

            int first = ColorUtil.multAlpha(Theme.getInstance().clientColor(), alpha / 4F);

            float glowSize = 10f;

            RenderUtil.Shadow.drawShadow(matrix, position.x + (glowSize * 2F), position.y + (size.y / 2F), size.x - (glowSize * 4F), size.y / 2F, glowSize,
                    first,
                    Round.of(glowSize / 2F)
            );

            RenderUtil.Rounded.smooth(matrix, position.x, position.y, size.x, size.y, ColorUtil.multAlpha(ColorUtil.multDark(Theme.getInstance().clientColor(), 0.04F), alpha), Round.of(5));

            float hoverAlpha = hover.get() / 5F;
            int hoverFirst = ColorUtil.multAlpha(Theme.getInstance().clientColor(), hoverAlpha);
            int hoverSecond = ColorUtil.multAlpha(Theme.getInstance().clientColor(), hoverAlpha);
            RenderUtil.Rounded.roundedOutline(matrix, position.x, position.y, size.x, size.y, 0.5f,
                    hoverFirst,
                    hoverFirst,
                    hoverSecond,
                    hoverSecond,
                    Round.of(5)
            );
            int textColor = ColorUtil.getColor(200, 200, 230, alpha);
            int accentColor = ColorUtil.multAlpha(Theme.getInstance().clientColor(), alpha);

            Fonts.SF_MEDIUM.drawCenter(matrix, text, position.x + (size.x / 2F), position.y + (size.y / 2F) - (fontSize / 2F), ColorUtil.overCol(textColor, accentColor, hover.get()), fontSize - (hover.get() / 2F));
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (isHover(mouseX, mouseY, position.x, position.y, size.x, size.y)) {
                action.onPress(this);
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

        public interface IPressable {
            void onPress(Button action);
        }
    }
}