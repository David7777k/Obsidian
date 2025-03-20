package org.obsidian.client.utils.other;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author sheluvparis
 */

@UtilityClass
public class SearchUtil {

    /**
     * Поиск строк в списке, содержащих все ключевые слова (даже частичные).
     *
     * @param list     Список строк для поиска.
     * @param keywords Ключевые слова для поиска.
     * @return Список строк, содержащих ключевые слова.
     */
    public List<String> search(List<String> list, List<String> keywords) {
        if (list == null || keywords == null || list.isEmpty() || keywords.isEmpty()) {
            return new ArrayList<>();
        }

        return list.stream()
                .filter(item -> keywords.stream().allMatch(keyword -> item.toLowerCase().contains(keyword.toLowerCase())))
                .collect(Collectors.toList());
    }

    /**
     * Поиск строк в списке, содержащих хотя бы одно ключевое слово (даже частичные).
     *
     * @param list     Список строк для поиска.
     * @param keywords Ключевые слова для поиска.
     * @return Список строк, содержащих хотя бы одно ключевое слово.
     */
    public List<String> searchAny(List<String> list, List<String> keywords) {
        if (list == null || keywords == null || list.isEmpty() || keywords.isEmpty()) {
            return new ArrayList<>();
        }

        return list.stream()
                .filter(item -> keywords.stream().anyMatch(keyword -> item.toLowerCase().contains(keyword.toLowerCase())))
                .collect(Collectors.toList());
    }

    /**
     * Поиск строк в списке, начинающихся с ключевого слова.
     *
     * @param list    Список строк для поиска.
     * @param keyword Ключевое слово для поиска.
     * @return Список строк, начинающихся с ключевого слова.
     */
    public List<String> searchStartsWith(List<String> list, String keyword) {
        if (list == null || keyword == null || list.isEmpty() || keyword.isEmpty()) {
            return new ArrayList<>();
        }

        return list.stream()
                .filter(item -> item.toLowerCase().startsWith(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Поиск строк в списке, заканчивающихся на ключевое слово.
     *
     * @param list    Список строк для поиска.
     * @param keyword Ключевое слово для поиска.
     * @return Список строк, заканчивающихся на ключевое слово.
     */
    public List<String> searchEndsWith(List<String> list, String keyword) {
        if (list == null || keyword == null || list.isEmpty() || keyword.isEmpty()) {
            return new ArrayList<>();
        }

        return list.stream()
                .filter(item -> item.toLowerCase().endsWith(keyword.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * Подсчет количества строк в списке, содержащих ключевые слова (даже частичные).
     *
     * @param list     Список строк для поиска.
     * @param keywords Ключевые слова для поиска.
     * @return Количество строк, содержащих ключевые слова.
     */
    public long count(List<String> list, List<String> keywords) {
        if (list == null || keywords == null || list.isEmpty() || keywords.isEmpty()) {
            return 0;
        }

        return list.stream()
                .filter(item -> keywords.stream().allMatch(keyword -> item.toLowerCase().contains(keyword.toLowerCase())))
                .count();
    }
}