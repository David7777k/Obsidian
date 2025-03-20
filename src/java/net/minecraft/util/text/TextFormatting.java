package net.minecraft.util.text;

import lombok.Getter;
import org.obsidian.client.utils.render.color.ColorUtil;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum TextFormatting {
    BLACK("BLACK", '0', 0, ColorUtil.replAlpha(0, 1F)),
    DARK_BLUE("DARK_BLUE", '1', 1, ColorUtil.replAlpha(170, 1F)),
    DARK_GREEN("DARK_GREEN", '2', 2, ColorUtil.replAlpha(43520, 1F)),
    DARK_AQUA("DARK_AQUA", '3', 3, ColorUtil.replAlpha(43690, 1F)),
    DARK_RED("DARK_RED", '4', 4, ColorUtil.replAlpha(11141120, 1F)),
    DARK_PURPLE("DARK_PURPLE", '5', 5, ColorUtil.replAlpha(11141290, 1F)),
    GOLD("GOLD", '6', 6, ColorUtil.replAlpha(16755200, 1F)),
    GRAY("GRAY", '7', 7, ColorUtil.replAlpha(11184810, 1F)),
    DARK_GRAY("DARK_GRAY", '8', 8, ColorUtil.replAlpha(5592405, 1F)),
    BLUE("BLUE", '9', 9, ColorUtil.replAlpha(5592575, 1F)),
    GREEN("GREEN", 'a', 10, ColorUtil.replAlpha(5635925, 1F)),
    AQUA("AQUA", 'b', 11, ColorUtil.replAlpha(5636095, 1F)),
    RED("RED", 'c', 12, ColorUtil.replAlpha(16733525, 1F)),
    LIGHT_PURPLE("LIGHT_PURPLE", 'd', 13, ColorUtil.replAlpha(16733695, 1F)),
    YELLOW("YELLOW", 'e', 14, ColorUtil.replAlpha(16777045, 1F)),
    WHITE("WHITE", 'f', 15, ColorUtil.replAlpha(16777215, 1F)),
    OBFUSCATED("OBFUSCATED", 'k', true),
    BOLD("BOLD", 'l', true),
    STRIKETHROUGH("STRIKETHROUGH", 'm', true),
    UNDERLINE("UNDERLINE", 'n', true),
    ITALIC("ITALIC", 'o', true),
    RESET("RESET", 'r', -1, null);

    @SuppressWarnings("UnnecessaryUnicodeEscape")
    public static final char COLOR_CODE = '\u00A7';
    private static final EnumMap<TextFormatting, String> NAME_MAPPING = new EnumMap<>(TextFormatting.class);
    public static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    private final String name;
    private final char formattingCode;

    @Getter
    private final boolean fancyStyling;
    private final String controlString;

    @Getter
    private final int colorIndex;
    @Nullable
    private final Integer color;

    static {
        for (TextFormatting format : values()) {
            NAME_MAPPING.put(format, lowercaseAlpha(format.name));
        }
    }

    private static String lowercaseAlpha(String string) {
        return string.replaceAll("[^a-z]", "").toLowerCase(Locale.ROOT);
    }

    TextFormatting(String formattingName, char formattingCodeIn, int index, @Nullable Integer colorCode) {
        this(formattingName, formattingCodeIn, false, index, colorCode);
    }

    TextFormatting(String formattingName, char formattingCodeIn, boolean fancyStylingIn) {
        this(formattingName, formattingCodeIn, fancyStylingIn, -1, null);
    }

    TextFormatting(String formattingName, char formattingCodeIn, boolean fancyStylingIn, int index, @Nullable Integer colorCode) {
        this.name = formattingName;
        this.formattingCode = formattingCodeIn;
        this.fancyStyling = fancyStylingIn;
        this.colorIndex = index;
        this.color = colorCode;
        this.controlString = "§" + formattingCodeIn;
    }

    public static Object getTextWithoutFormattingCodes(String string) {
        return null;
    }

    public boolean isColor() {
        return !this.fancyStyling && this != RESET;
    }

    @Nullable
    public Integer getColor() {
        return this.color;
    }

    public String getFriendlyName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public String toString() {
        return this.controlString;
    }

    @Nullable
    public static String removeFormatting(@Nullable String text) {
        return text == null ? null : FORMATTING_CODE_PATTERN.matcher(text).replaceAll("");
    }

    @Nullable
    public static TextFormatting getValueByName(@Nullable String friendlyName) {
        return friendlyName == null ? null : NAME_MAPPING.entrySet().stream()
                .filter(entry -> entry.getValue().equals(lowercaseAlpha(friendlyName)))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public static TextFormatting fromColorIndex(int index) {
        if (index < 0) {
            return RESET;
        }
        return Arrays.stream(values())
                .filter(format -> format.colorIndex == index)
                .findFirst()
                .orElse(null);
    }

    @Nullable
    public static TextFormatting fromFormattingCode(char formattingCodeIn) {
        char c0 = Character.toString(formattingCodeIn).toLowerCase(Locale.ROOT).charAt(0);
        return Arrays.stream(values())
                .filter(format -> format.formattingCode == c0)
                .findFirst()
                .orElse(null);
    }

    public static Collection<String> getValidValues(boolean getColor, boolean getFancyStyling) {
        return Arrays.stream(values())
                .filter(format -> (!format.isColor() || getColor) && (!format.isFancyStyling() || getFancyStyling))
                .map(TextFormatting::getFriendlyName)
                .collect(Collectors.toList());
    }
}
