package org.obsidian.client.utils.render.color;

import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class ColorFormatting {
    public Pattern PATTERN = Pattern.compile("\\$\\{(rgba|rgb)\\((\\d{1,3}),(\\d{1,3}),(\\d{1,3})(?:,(\\d{1,3}))?\\)}|\\$\\{reset}", Pattern.CASE_INSENSITIVE);

    public String getColor(int red, int green, int blue) {
        return String.format("${rgb(%s,%s,%s)}", red, green, blue);
    }

    public String getColor(int red, int green, int blue, int alpha) {
        return String.format("${rgba(%s,%s,%s,%s)}", red, green, blue, alpha);
    }

    public String getColor(int color) {
        return String.format("${rgba(%s,%s,%s,%s)}", ColorUtil.red(color), ColorUtil.green(color), ColorUtil.blue(color), ColorUtil.alpha(color));
    }

    public int overColor(int firstColor, int secondColor, float factorPercent) {
        return ColorUtil.overCol(firstColor, secondColor, factorPercent);
    }

    public String reset() {
        return "${reset}";
    }

    public String removeFormatting(String text) {
        return PATTERN.matcher(text).replaceAll("");
    }

    public String typeRGB() {
        return "rgb";
    }

    public String typeRGBA() {
        return "rgba";
    }

    public String replaceColor(String text, int red, int green, int blue) {
        return PATTERN.matcher(text).replaceAll(Matcher.quoteReplacement(getColor(red, green, blue)));
    }

    public String replaceColor(String text, int red, int green, int blue, int alpha) {
        return PATTERN.matcher(text).replaceAll(Matcher.quoteReplacement(getColor(red, green, blue, alpha)));
    }
}