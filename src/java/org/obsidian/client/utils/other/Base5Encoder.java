package org.obsidian.client.utils.other;

import java.util.HashMap;
import java.util.Map;

public class Base5Encoder {

    public static String textToAsciiBase5(String text) {
        StringBuilder base5Strings = new StringBuilder();

        for (char c : text.toCharArray()) {
            int asciiValue = c;
            StringBuilder base5Value = new StringBuilder();

            while (asciiValue > 0) {
                base5Value.insert(0, asciiValue % 5);
                asciiValue /= 5;
            }

            while (base5Value.length() < 5) {
                base5Value.insert(0, '0');
            }

            base5Strings.append(base5Value);
        }

        return base5Strings.toString();
    }

    public static String encode(String base5String) {
        Map<Character, Character> encodingMap = new HashMap<>();
        encodingMap.put('0', 'Z');
        encodingMap.put('1', 'O');
        encodingMap.put('2', 'V');
        encodingMap.put('3', 'z');
        encodingMap.put('4', 'o');

        StringBuilder encodedString = new StringBuilder();
        for (char c : base5String.toCharArray()) {
            encodedString.append(encodingMap.get(c));
        }

        return encodedString.toString();
    }

    public static String decode(String encodedText) {
        Map<Character, Character> decodingMap = new HashMap<>();
        decodingMap.put('Z', '0');
        decodingMap.put('O', '1');
        decodingMap.put('V', '2');
        decodingMap.put('z', '3');
        decodingMap.put('o', '4');

        StringBuilder decodedString = new StringBuilder();
        for (char c : encodedText.toCharArray()) {
            decodedString.append(decodingMap.get(c));
        }

        return decodedString.toString();
    }

    public static String base5ToText(String base5String) {
        StringBuilder text = new StringBuilder();

        for (int i = 0; i < base5String.length(); i += 5) {
            String base5Chunk = base5String.substring(i, Math.min(i + 5, base5String.length()));
            int asciiValue = Integer.parseInt(base5Chunk, 5);
            text.append((char) asciiValue);
        }

        return text.toString();
    }

    public static String encrypt(String text) {
        String base5String = textToAsciiBase5(text);
        return encode(base5String);
    }

    public static String decrypt(String encodedText) {
        String base5String = decode(encodedText);
        return base5ToText(base5String);
    }
}