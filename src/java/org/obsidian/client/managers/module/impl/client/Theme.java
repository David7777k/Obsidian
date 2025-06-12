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
    private final ColorSetting accent = new ColorSetting(this, "Accent", ColorUtil.getColor(224, 224, 224));
    private final ColorSetting background = new ColorSetting(this, "Background", ColorUtil.getColor(32, 32, 32));
    private final ColorSetting text = new ColorSetting(this, "Text", ColorUtil.getColor(224, 224, 224));
    private final ColorSetting starColor = new ColorSetting(this, "Star", ColorUtil.getColor(224, 224, 224));
    private final ListSetting<ThemeType> preset = new ListSetting<>(this, "Preset", ThemeType.values())
            .onAction(() -> applyTheme(preset.getValue()))
            .set(ThemeType.FLAT);

    public Theme() {
        applyTheme(preset.getValue());
    }

    public void drawClientRect(MatrixStack matrix, float x, float y, float width, float height, float alpha, float radius) {
        x = (float) Mathf.step(x, 0.5);
        y = (float) Mathf.step(y, 0.5);
        width = (float) Mathf.step(width, 0.5);
        height = (float) Mathf.step(height, 0.5);

        RenderUtil.Rounded.roundedRect(matrix, x, y, width, height, ColorUtil.replAlpha(backgroundColor(), alpha), Round.of(radius));
        RenderUtil.Rounded.roundedOutline(matrix, x, y, width, height, 0.5F, ColorUtil.replAlpha(textColor(), alpha), Round.of(radius));
    }

    public void drawClientRect(MatrixStack matrix, float x, float y, float width, float height, float alpha) {
        drawClientRect(matrix, x, y, width, height, alpha, 2);
    }

    public void drawClientRect(MatrixStack matrix, float x, float y, float width, float height) {
        drawClientRect(matrix, x, y, width, height, 1F, 2);
    }

    public int clientColor() {
        return accent.getValue();
    }

    public int backgroundColor() {
        return background.getValue();
    }

    public int shadowColor() {
        return ColorUtil.multDark(background.getValue(), 0.8F);
    }

    public int textColor() {
        return text.getValue();
    }

    public int textAccentColor() {
        return ColorUtil.multDark(text.getValue(), 0.75F);
    }

    public int iconColor() {
        return accent.getValue();
    }

    public int darkColor() {
        return ColorUtil.multDark(accent.getValue(), 0.25F);
    }

    public int getSpeed() {
        return 10;
    }

    public int secondaryColor() {
        return ColorUtil.multDark(accent.getValue(), 0.75F);
    }

    public int tertiaryColor() {
        return ColorUtil.multDark(accent.getValue(), 0.5F);
    }

    // Новый метод для получения цвета звёздочек
    public int starColor() {
        return starColor.getValue();
    }

    public void applyTheme(ThemeType type) {
        accent.set(type.getAccentColor());
        background.set(type.getBackgroundColor());
        text.set(type.getTextColor());
    }
}
