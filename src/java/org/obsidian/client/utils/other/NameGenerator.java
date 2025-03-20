package org.obsidian.client.utils.other;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.util.List;

@UtilityClass
public class NameGenerator {

    private final SecureRandom random = new SecureRandom();

    private final List<String> SYLLABLES = List.of(
            "a", "e", "i", "o", "u", "y",
            "ka", "ke", "ki", "ko", "ku", "ky",
            "sa", "se", "shi", "so", "su", "sy",
            "ta", "te", "chi", "tsu", "to", "ty",
            "na", "ne", "ni", "no", "nu", "ny",
            "ha", "he", "hi", "fu", "hu", "hy",
            "ma", "me", "mi", "mo", "mu", "my",
            "ya", "yu", "yo",
            "ra", "re", "ri", "ro", "ru", "ry",
            "wa", "wo", "n", "zi", "zo", "zu",
            "je", "ji",
            "fa", "fe", "fi", "fo",
            "ga", "ge", "gi", "go", "gu",
            "la", "le", "li", "lo", "lu",
            "ba", "be", "bi", "bo", "bu",
            "da", "de", "di", "do", "du",
            "xa", "xe", "xi", "xo", "xu"
            // При необходимости можно добавить или убрать слоги
    );

    public String generate() {
        // Генерируем имя, состоящее из 3-5 случайных слогов
        int syllableCount = random.nextInt(3) + 3; // 3, 4 или 5 слогов
        StringBuilder name = new StringBuilder();

        for (int i = 0; i < syllableCount; i++) {
            String syllable = SYLLABLES.get(random.nextInt(SYLLABLES.size()));
            name.append(syllable);
        }

        // Первая буква всегда заглавная
        if (name.length() > 0) {
            name.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        }

        // С вероятностью 25% применяем дополнительную случайную капитализацию внутри имени
        if (random.nextInt(4) == 0) {
            applyRandomCapitalization(name);
        }

        // С вероятностью 10% добавляем числовой суффикс (от 0 до 99)
        if (random.nextInt(10) == 0) {
            name.append(random.nextInt(100));
        }

        return name.toString();
    }

    private void applyRandomCapitalization(StringBuilder name) {
        // Применяем случайное изменение регистра для нескольких символов, исключая первую букву
        int changes = random.nextInt(Math.max(1, name.length() / 3)); // примерно меняем до 1/3 символов
        for (int i = 1; i < name.length() && changes > 0; i++) {
            if (random.nextBoolean()) {
                name.setCharAt(i, Character.toUpperCase(name.charAt(i)));
                changes--;
            }
        }
    }
}
