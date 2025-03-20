package org.obsidian.client.screen.account;

import lombok.Data;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.interfaces.IScreen;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.other.SoundUtil;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;
import org.obsidian.client.utils.render.font.Fonts;
import org.obsidian.common.user.LoginManager;
import org.joml.Vector2f;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@Accessors(fluent = true)
public class Account implements IScreen {
    private final LocalDateTime creationDate;
    private final String name;
    private boolean favorite;
    private final Vector2f position = new Vector2f();
    private final Vector2f size = new Vector2f();
    private final Animation hover = new Animation();

    public Account(LocalDateTime creationDate, String name) {
        if (creationDate == null || creationDate.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Дата создания не может быть в будущем.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым.");
        }
        this.creationDate = creationDate;
        this.name = name;
    }

    public Account toggleFavorite() {
        this.favorite = !this.favorite;
        return this;
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {

    }

    @Override
    public void init() {

    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        boolean selected = mc.session.getProfile().getName().equals(name);
        float alpha = Obsidian.inst().accountGui().getAlpha().get();
        if (Obsidian.inst().accountGui().getSelected() != null && Obsidian.inst().accountGui().getSelected().equals(this) && Obsidian.inst().accountGui().getAlpha().isFinished()) {

            int first = ColorUtil.multAlpha(ColorUtil.multDark(Theme.getInstance().clientColor(), 0.5F), alpha / 2F);

            float glowSize = 10f;

            RenderUtil.Shadow.drawShadow(matrix, position.x, position.y, size.x, size.y, glowSize,
                    first,
                    Round.of(glowSize / 2F)
            );
        }
        RenderUtil.Rounded.smooth(matrix, position.x, position.y, size.x, size.y, ColorUtil.multAlpha(ColorUtil.multDark(Theme.getInstance().clientColor(), 0.06F), alpha), Round.of(4));
        Fonts.SF_SEMIBOLD.draw(matrix, name, position.x + 5F, position.y + size.y / 2F - 8F / 2, selected ? ColorUtil.multAlpha(Theme.getInstance().clientColor(), alpha) : ColorUtil.getColor(180, alpha), 8F);

        if (favorite) {
            Fonts.ACCOUNT.draw(matrix, "b", position.x + 5F + Fonts.SF_SEMIBOLD.getWidth(name, 8F) + 2.5F, position.y + size.y / 2F - 8F / 2, ColorUtil.getColor(255, 215, 0, alpha), 8F);
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm");
        String formattedDate = creationDate.format(formatter);
        Fonts.SF_SEMIBOLD.drawRight(matrix, formattedDate, position.x + size.x - 5F, position.y + size.y / 2F - 8F / 2F + 8F - 6F, ColorUtil.getColor(130, alpha), 6F);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (hovered(mouseX, mouseY)) {
            if (Obsidian.inst().accountGui().getSelected() == null || !Obsidian.inst().accountGui().getSelected().equals(this)) {
                Obsidian.inst().accountGui().setSelected(this);
                return false;
            }
            if (Obsidian.inst().accountGui().getSelected().equals(this) && !mc.session.getProfile().getName().equals(name)) {
                if (isLClick(button)) {
                    mc.session = new Session(name);
                    LoginManager.saveUsername(name);
                    SoundUtil.playSound("select.wav", 0.5F);
                }
                if (isRClick(button)) {
                    Obsidian.inst().accountManager().removeAccount(name);
                    Obsidian.inst().accountGui().setSelected(Obsidian.inst().accountManager().stream().findFirst().orElse(null));
                }
            }
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

    public boolean hovered(double mouseX, double mouseY) {
        return isHover(mouseX, mouseY, position.x, position.y, size.x, size.y);
    }
}
