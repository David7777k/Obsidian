package org.obsidian.client.utils.render.text;

import lombok.experimental.UtilityClass;
import org.obsidian.client.utils.render.font.Font;

import java.util.*;

@UtilityClass
public class TextUtils {
    public static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
            + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя"
            + "ʙғɢʜɪʟɴǫʀsxʏ"
            + "0123456789"
            + "!?@#$%^&*()-_=+[]{}|\\;:'\"<>,./`~"
            + "©™® ";
    public final String ALLOWED_TO_SESSION = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
            + "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯабвгдеёжзийклмнопрстуфхцчшщъыьэюя"
            + "0123456789_";
    public final String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    public final String NUMBERS = "1234567890";
    public final String EMPTY = "";

    public String removeForbiddenCharacters(String input, String allowedCharacters) {
        StringBuilder result = new StringBuilder();

        for (char ch : input.toCharArray()) {
            if (allowedCharacters.indexOf(ch) != -1) {
                result.append(ch);
            }
        }

        return result.toString();
    }

    public <T> boolean containsAll(List<T> mainList, List<T> findList) {
        return new HashSet<>(mainList).containsAll(findList);
    }

    public List<String> splitLine(String text, Font font, float fontSize, float maxWidth, String splitter) {
        List<String> splitLines = new ArrayList<>();
        StringBuilder currentLine = new StringBuilder();
        float currentLineWidth = 0;
        float hyphenWidth = font.getWidth(splitter, fontSize);
        Map<Character, Float> charWidths = new HashMap<>();

        for (String line : text.trim().split("\n")) {
            for (char character : line.toCharArray()) {
                float charWidth = charWidths.computeIfAbsent(character, ch -> font.getWidth(String.valueOf(ch), fontSize));

                if (currentLineWidth + charWidth + hyphenWidth > maxWidth && !currentLine.isEmpty()) {
                    splitLines.add(currentLine.toString());
                    currentLine.setLength(0);
                    currentLineWidth = 0;
                }

                if (character != ' ' || !currentLine.isEmpty()) {
                    currentLine.append(character);
                    currentLineWidth += charWidth;
                }
            }
            if (!currentLine.isEmpty()) {
                splitLines.add(currentLine.toString());
                currentLine.setLength(0);
                currentLineWidth = 0;
            }
        }

        return splitLines;
    }

    public float splitLineHeight(String text, Font font, float fontSize, float maxWidth, String splitter) {
        int count = 0;
        StringBuilder currentLine = new StringBuilder();
        float currentLineWidth = 0;
        float hyphenWidth = font.getWidth(splitter, fontSize);
        Map<Character, Float> charWidths = new HashMap<>();

        for (String line : text.trim().split("\n")) {
            for (char character : line.toCharArray()) {
                float charWidth = charWidths.computeIfAbsent(character, ch -> font.getWidth(String.valueOf(ch), fontSize));

                if (currentLineWidth + charWidth + hyphenWidth > maxWidth && !currentLine.isEmpty()) {
                    count++;
                    currentLine.setLength(0);
                    currentLineWidth = 0;
                }

                if (character != ' ' || !currentLine.isEmpty()) {
                    currentLine.append(character);
                    currentLineWidth += charWidth;
                }
            }
            if (!currentLine.isEmpty()) {
                count++;
                currentLine.setLength(0);
                currentLineWidth = 0;
            }
        }

        return count;
    }
}