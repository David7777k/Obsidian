package org.obsidian.client.screen.account;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Session;
import net.minecraft.util.text.StringTextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.api.interfaces.IMouse;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.keyboard.Keyboard;
import org.obsidian.client.utils.other.NameGenerator;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.client.utils.render.color.ColorFormatting;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;
import org.obsidian.client.utils.render.draw.StencilUtil;
import org.obsidian.client.utils.render.font.Fonts;
import org.obsidian.client.utils.render.scroll.ScrollUtil;
import org.obsidian.client.utils.render.text.TextAlign;
import org.obsidian.client.utils.render.text.TextBox;
import org.obsidian.common.user.LoginManager;
import org.joml.Vector2f;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class AccountGuiScreen extends Screen implements IMinecraft, IMouse {
    public AccountGuiScreen() {
        super(StringTextComponent.EMPTY);
    }

    private TextBox textBox;
    private boolean exit = false;
    private final Animation alpha = new Animation();
    private final Animation scale = new Animation();
    private final ScrollUtil scroll = new ScrollUtil();
    @Setter
    private Account selected = null;

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        Obsidian.inst().accountManager().forEach(account -> account.resize(minecraft, width, height));
    }

    @Override
    protected void init() {
        super.init();
        alpha.set(0.0);
        scale.set(0.0);
        alpha.run(1.0, 0.5);
        scale.run(1.0, 0.5, Easings.EXPO_OUT);
        exit = false;

        {
            textBox = new TextBox(new Vector2f(), Fonts.SF_SEMIBOLD, 8, Theme.getInstance().clientColor(), TextAlign.LEFT, "Введите желаемое имя", 0, false, false);
            textBox.setText(minecraft.session.getProfile().getName());
            textBox.setCursor(minecraft.session.getProfile().getName().length());
        }

        if (!Obsidian.inst().accountManager().isAccount(mc.session.getProfile().getName())) {
            Obsidian.inst().accountManager().addAccount(new Account(LocalDateTime.now(), mc.session.getProfile().getName()));
        }

        setSelected(Obsidian.inst().accountManager().stream().findFirst().orElse(null));

        Obsidian.inst().accountManager().forEach(Account::init);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        alpha.update();
        scale.update();
        this.closeCheck();

        RenderUtil.drawShaderBackground(matrixStack, 1F);

        int color = ColorUtil.multAlpha(ColorUtil.multDark(Theme.getInstance().clientColor(), 0.04F), alpha.get());

        float width = 280;
        float height = 240;
        float x = this.width / 2F - width / 2F;
        float y = this.height / 2F - height / 2F;
        float margin = 10;

        Fonts.SF_BOLD.drawOutline(matrixStack, "Менеджер аккаунтов", x + margin, y - margin - 8, ColorUtil.getColor(255, alpha.get()), 8);
        Fonts.SF_BOLD.drawRightOutline(matrixStack, "Ваш никнейм: " + ColorFormatting.getColor(ColorUtil.multAlpha(Theme.getInstance().clientColor(), alpha.get())) + mc.session.getProfile().getName(), x + width - margin, y - margin - 8 + 8 - 6, ColorUtil.getColor(255, alpha.get()), 6);

        List<String> strings = new ArrayList<>();
        strings.add(ColorFormatting.getColor(ColorUtil.getColor(128, alpha.get())) + "Левая кнопка мыши - " + ColorFormatting.getColor(ColorUtil.multAlpha(Theme.getInstance().darkColor(), alpha.get())) + "Использовать аккаунт.");
        strings.add(ColorFormatting.getColor(ColorUtil.getColor(128, alpha.get())) + "Правая кнопка мыши - " + ColorFormatting.getColor(ColorUtil.multDark(ColorUtil.getColor(255, 72, 72, alpha.get()), 0.5F)) + "Удалить аккаунт.");
        float index = 0;
        for (String string : strings) {
            Fonts.SF_BOLD.drawCenter(matrixStack, string, this.width / 2F, this.height - margin - 6F - index, -1, 6F);
            index += 6F + 2F;
        }


        RenderUtil.Rounded.smooth(matrixStack, x, y, width, height, color, Round.of(8));

        scroll.update();

        float offset = 0;
        boolean isFavoriteEmpty = Obsidian.inst().accountManager().getFavoriteAccountsSorted().isEmpty();


        StencilUtil.enable();
        RectUtil.drawRect(matrixStack, x, y + margin / 2F, width, height - margin, ColorUtil.getColor(255, 0.5F));
        StencilUtil.read(1);

        if (!isFavoriteEmpty) {
            Fonts.SF_BOLD.draw(matrixStack, "Избранное", x + margin, y + scroll.getWheel() + margin, ColorUtil.getColor(255, 215, 0, alpha.get()), 6);
            offset += 6F + margin;
        }

        for (Account account : Obsidian.inst().accountManager().getFavoriteAccountsSorted()) {
            account.position().set(x + margin, y + scroll.getWheel() + margin + offset);
            account.size().set(width - margin * 2, 20);
            account.render(matrixStack, mouseX, mouseY, partialTicks);
            offset += 25;
        }

        if (!Obsidian.inst().accountManager().stream().filter(account -> !account.favorite()).toList().isEmpty()) {
            offset += margin / 2F;
            Fonts.SF_BOLD.draw(matrixStack, "Список никнеймов", x + margin, y + scroll.getWheel() + margin + offset, ColorUtil.getColor(215, 215, 255, alpha.get()), 6);

            offset += 6F + margin;
        }

        for (Account account : Obsidian.inst().accountManager()) {
            if (account.favorite()) continue;
            account.position().set(x + margin, y + scroll.getWheel() + margin + offset);
            account.size().set(width - margin * 2, 20);
            account.render(matrixStack, mouseX, mouseY, partialTicks);
            offset += 25;
        }
        StencilUtil.disable();

        scroll.setMax(offset - (margin / 2F), height - (margin * 2F));

        scroll.renderV(matrixStack, new Vector2f(x + width - 5, y + margin), height - (margin * 2F), alpha.get() / 2F);

        float append = 5;
        float rectSize = append + 8 + append;
        textBox.position.set(x + append, y + margin + height + append);
        textBox.setWidth(textBox.isEmpty() ? textBox.font.getWidth(textBox.getEmptyText(), textBox.fontSize) : Math.max(textBox.font.getWidth(textBox.getEmptyText(), textBox.fontSize), textBox.font.getWidth(textBox.getText(), textBox.fontSize)));
        textBox.setColor(ColorUtil.multAlpha(Theme.getInstance().clientColor(), alpha.get()));
        RenderUtil.Rounded.smooth(matrixStack, x, y + height + margin, textBox.getWidth() + append * 2, rectSize, color, Round.of(4));
        textBox.draw(matrixStack);

        RenderUtil.Rounded.smooth(matrixStack, x + width - rectSize, y + height + margin, rectSize, rectSize, color, Round.of(4));
        Fonts.ACCOUNT.drawCenter(matrixStack, "d", x + width - rectSize / 2F, y + height + margin + rectSize / 2F - 12F / 2F + 1F, ColorUtil.getColor(255, 215, 0, alpha.get()), 12F);

        RenderUtil.Rounded.smooth(matrixStack, x + width - rectSize - append - rectSize, y + height + margin, rectSize, rectSize, color, Round.of(4));
        Fonts.ACCOUNT.drawCenter(matrixStack, "e", x + width - rectSize - append - rectSize / 2F, y + height + margin + rectSize / 2F - 12F / 2F + 1F, ColorUtil.getColor(244, 119, 255, alpha.get()), 12F);

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (exit) return false;
        float width = 280;
        float height = 240;
        float x = this.width / 2F - width / 2F;
        float y = this.height / 2F - height / 2F;
        float margin = 10;
        textBox.mouse(mouseX, mouseY, button);

        float append = 5;
        float rectSize = append + 8 + append;

        if (isLClick(button)) {
            // favorite
            if (isHover(mouseX, mouseY, x + width - rectSize, y + height + margin, rectSize, rectSize) && selected != null) {
                selected.toggleFavorite();
                return false;
            }

            // random gen
            if (isHover(mouseX, mouseY, x + width - rectSize - append - rectSize, y + height + margin, rectSize, rectSize) && selected != null) {
                minecraft.session = new Session(NameGenerator.generate());
                LoginManager.saveUsername(minecraft.session.getProfile().getName());
                textBox.setText(minecraft.session.getProfile().getName());
                textBox.setCursor(minecraft.session.getProfile().getName().length());
                Account account = new Account(LocalDateTime.now(), minecraft.session.getProfile().getName());
                Obsidian.inst().accountManager().addAccount(account);
                selected = account;
                scroll.setTarget(0);
                scroll.update();
                return false;
            }
        }


        if (!isHover(mouseX, mouseY, x, y + margin, width, height - margin * 2)) {
            return false;
        }
        Obsidian.inst().accountManager().forEach(account -> account.mouseClicked(mouseX, mouseY, button));
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Obsidian.inst().accountManager().forEach(account -> account.mouseReleased(mouseX, mouseY, button));
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (exit) return false;

        Obsidian.inst().accountManager().forEach(account -> account.keyPressed(keyCode, scanCode, modifiers));

        String prevText = textBox.getText();
        textBox.keyPressed(keyCode);
        if (textBox.getText().length() >= 3 && PlayerUtil.isInvalidName(textBox.getText()))
            textBox.setText(prevText);
        if (textBox.isSelected() && keyCode == Keyboard.KEY_ENTER.getKey()) {
            textBox.setSelected(false);
            if (!textBox.getText().isEmpty() && textBox.getText().length() >= 3) {
                minecraft.session = new Session(textBox.getText());
                LoginManager.saveUsername(minecraft.session.getProfile().getName());
                textBox.setText(minecraft.session.getProfile().getName());
                textBox.setCursor(minecraft.session.getProfile().getName().length());
                Account account = new Account(LocalDateTime.now(), minecraft.session.getProfile().getName());
                Obsidian.inst().accountManager().addAccount(account);
                selected = account;
                scroll.setTarget(0);
                scroll.update();
            }
        }


        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        Obsidian.inst().accountManager().forEach(account -> account.keyReleased(keyCode, scanCode, modifiers));
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (exit) return false;

        Obsidian.inst().accountManager().forEach(account -> account.charTyped(codePoint, modifiers));

        if (textBox.getText().length() < 16) {
            String prevText = textBox.getText();
            textBox.charTyped(codePoint);
            if (textBox.getText().length() >= 3 && PlayerUtil.isInvalidName(textBox.getText()))
                textBox.setText(prevText);
        }

        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        boolean noneMatch = true; // TODO
        if (!exit && scale.getValue() > 0.0F && alpha.getValue() > 0.0F) {
            alpha.run(0.0, 0.25, Easings.SINE_IN);
            scale.run(0.0, 0.5, Easings.SINE_IN);
            exit = true;
        }
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        Obsidian.inst().accountManager().forEach(Account::onClose);

        textBox.selected = false;
        LoginManager.saveUsername(minecraft.session.getProfile().getName());
        textBox.setText(minecraft.session.getProfile().getName());
    }

    private void closeCheck() {
        boolean noneMatch = true; // TODO
        if (exit && scale.isFinished() && alpha.isFinished()) {
            closeScreen();
            exit = false;
        }
    }
}
