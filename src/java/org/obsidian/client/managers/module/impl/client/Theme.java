package org.obsidian.client.managers.module.impl.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.ColorSetting;
import org.obsidian.client.managers.module.settings.impl.DelimiterSetting;
import org.obsidian.client.managers.module.settings.impl.ListSetting;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Theme", category = Category.CLIENT, autoEnabled = true, allowDisable = false)
public class Theme extends Module {
    public static Theme getInstance() {
        return Instance.get(Theme.class);
    }

    private final DelimiterSetting delimiter = new DelimiterSetting(this, "Цвета клиента");
    private final ColorSetting client = new ColorSetting(this, "Клиент", ColorUtil.getColor(90, 90, 255));
    private final ColorSetting background = new ColorSetting(this, "Задний фон", ColorUtil.getColor(20, 20, 30));
    private final ColorSetting shadow = new ColorSetting(this, "Тень", ColorUtil.getColor(33, 33, 43));
    private final ColorSetting text = new ColorSetting(this, "Текст", ColorUtil.getColor(150, 100, 255));
    private final ColorSetting icon = new ColorSetting(this, "Иконки", ColorUtil.getColor(120, 60, 255));
    private final ColorSetting secondary = new ColorSetting(this, "Второстепенный", ColorUtil.getColor(70, 70, 200));
    private final ColorSetting tertiary = new ColorSetting(this, "Третичный", ColorUtil.getColor(50, 50, 150));
    private final ColorSetting starColor = new ColorSetting(this, "Цвет звёздочек", ColorUtil.getColor(255, 215, 0)); // Значение по умолчанию - золотистый
    private final ListSetting<ThemeType> preset = new ListSetting<>(this, "Preset", ThemeType.values())
            .onAction(() -> applyTheme(preset.getValue()))
            .set(ThemeType.DARK);

    public Theme() {
        applyTheme(preset.getValue());
    }

    public void drawClientRect(MatrixStack matrix, float x, float y, float width, float height, float alpha, float radius) {
        x = (float) Mathf.step(x, 0.5);
        y = (float) Mathf.step(y, 0.5);
        width = (float) Mathf.step(width, 0.5);
        height = (float) Mathf.step(height, 0.5);

        RenderUtil.Shadow.drawShadow(matrix, x - radius / 2, y - radius / 2, width + radius, height + radius, 10, ColorUtil.replAlpha(shadowColor(), (float) Math.pow(alpha, 3)));
        RenderUtil.Rounded.smooth(matrix, x, y, width, height, ColorUtil.replAlpha(backgroundColor(), alpha), Round.of(radius));
    }

    public void drawClientRect(MatrixStack matrix, float x, float y, float width, float height, float alpha) {
        drawClientRect(matrix, x, y, width, height, alpha, 4);
    }

    public void drawClientRect(MatrixStack matrix, float x, float y, float width, float height) {
        drawClientRect(matrix, x, y, width, height, 1F, 4);
    }

    public int clientColor() {
        return client.getValue();
    }

    public int backgroundColor() {
        return background.getValue();
    }

    public int shadowColor() {
        return shadow.getValue();
    }

    public int textColor() {
        return text.getValue();
    }

    public int textAccentColor() {
        return ColorUtil.multDark(text.getValue(), 0.75F);
    }

    public int iconColor() {
        return icon.getValue();
    }

    public int darkColor() {
        return ColorUtil.multDark(client.getValue(), 0.25F);
    }

    public int getSpeed() {
        return 10;
    }

    public int secondaryColor() {
        return secondary.getValue();
    }

    public int tertiaryColor() {
        return tertiary.getValue();
    }

    // Новый метод для получения цвета звёздочек
    public int starColor() {
        return starColor.getValue();
    }

    public void applyTheme(ThemeType type) {
        client.set(type.getClientColor());
        background.set(type.getBackgroundColor());
        shadow.set(type.getShadowColor());
        text.set(type.getTextColor());
        icon.set(type.getIconColor());
        secondary.set(type.getSecondaryColor());
        tertiary.set(type.getTertiaryColor());
    }
}