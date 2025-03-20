package org.obsidian.client.managers.other.autobuy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NonNull;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.registry.Registry;
import org.obsidian.client.Obsidian;
import org.obsidian.client.managers.other.autobuy.item.AutoBuyEnchItem;
import org.obsidian.client.managers.other.autobuy.item.AutoBuyItem;
import org.obsidian.client.managers.other.autobuy.item.AutoBuyToolTipItem;
import org.obsidian.client.utils.file.AbstractFile;
import org.obsidian.client.utils.file.FileType;

import java.io.*;
import java.util.*;

public class AutoBuyFile extends AbstractFile {

    public AutoBuyFile(File file) {
        super(file, FileType.AUTOBUY);
    }

    @Override
    public boolean read() {
        if (!this.getFile().exists()) {
            return false;
        }

        try {

            final FileReader fileReader = new FileReader(this.getFile());
            final BufferedReader bufferedReader = new BufferedReader(fileReader);
            final JsonObject jsonObject = GSON.fromJson(bufferedReader, JsonObject.class);
            bufferedReader.close();
            fileReader.close();

            if (jsonObject == null) {
                return false;
            }
            JsonArray autoBuyArray = jsonObject.getAsJsonArray("items");
            if (autoBuyArray != null) {
                AutoBuyManager manager = Obsidian.inst().autoBuyManager();
                for (JsonElement autoBuyElement : autoBuyArray) {
                    JsonObject autoBuyJSONElement = autoBuyElement.getAsJsonObject();
                    String item = autoBuyJSONElement.get("item").getAsString();
                    int price = autoBuyJSONElement.get("price").getAsInt();
                    boolean enabled = autoBuyJSONElement.get("enabled").getAsBoolean();

                    Optional<AutoBuyItem> itemOptional = manager.stream()
                            .filter(x -> x.getItem().getTranslationKey().equals(item))
                            .findFirst();

                    if (itemOptional.isPresent()) {
                        AutoBuyItem autoBuyItem = itemOptional.get();
                        autoBuyItem.setPrice(price);
                        autoBuyItem.setEnabled(enabled);

                        if (autoBuyJSONElement.has("enchants") && autoBuyItem instanceof AutoBuyEnchItem enchItem) {
                            JsonObject enchantments = autoBuyJSONElement.getAsJsonObject("enchants");
                            enchantments.entrySet().forEach(entry -> {
                                String enchantmentID = entry.getKey();
                                JsonArray levelsArray = entry.getValue().getAsJsonArray();

                                Enchantment enchantment = Enchantment.getEnchantmentByID(Integer.parseInt(enchantmentID));
                                if (enchantment != null) {
                                    List<Integer> levels = new ArrayList<>();
                                    for (JsonElement levelElement : levelsArray) {
                                        levels.add(levelElement.getAsInt());
                                    }
                                    enchItem.addEnchant(enchantment, levels);
                                }
                            });
                        }

                        if (autoBuyJSONElement.has("tooltips") && autoBuyItem instanceof AutoBuyToolTipItem tooltipItem) {
                            JsonArray tooltipsArray = autoBuyJSONElement.getAsJsonArray("tooltips");
                            Set<String> tooltips = new HashSet<>();
                            for (JsonElement tooltipElement : tooltipsArray) {
                                tooltips.add(tooltipElement.getAsString());
                            }
                            tooltipItem.setToolTips(tooltips);
                        }
                    }
                }
            }

        } catch (final IOException ignored) {
            return false;
        }

        return true;
    }

    @Override
    public boolean write() {
        try {
            if (!this.getFile().exists()) {
                if (this.getFile().createNewFile()) {
                    System.out.println("Файл autobuy успешно создан.");
                } else {
                    System.out.println("Произошла ошибка при создании файла autobuy.");
                }
            }

            final JsonObject jsonObject = getJsonObject();

            final FileWriter fileWriter = new FileWriter(getFile());
            final BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

            GSON.toJson(jsonObject, bufferedWriter);

            bufferedWriter.flush();
            bufferedWriter.close();
            fileWriter.flush();
            fileWriter.close();
        } catch (final IOException ignored) {
            return false;
        }

        return true;
    }

    @NonNull
    private static JsonObject getJsonObject() {
        final JsonObject jsonObject = new JsonObject();
        JsonArray autoBuyArray = new JsonArray();
        AutoBuyManager manager = Obsidian.inst().autoBuyManager();
        for (AutoBuyItem autoBuyItem : manager) {
            final JsonObject autoBuyJsonObject = new JsonObject();
            autoBuyJsonObject.addProperty("item", autoBuyItem.getItem().getTranslationKey());
            autoBuyJsonObject.addProperty("price", autoBuyItem.getPrice());
            autoBuyJsonObject.addProperty("enabled", autoBuyItem.isEnabled());

            if (autoBuyItem instanceof AutoBuyEnchItem enchItem) {
                JsonObject enchantments = new JsonObject();

                for (AutoBuyEnchItem.EnchantmentLevel enchant : enchItem.getEnchants()) {
                    JsonArray levelsArray = new JsonArray();
                    for (Integer level : enchant.getLevels()) {
                        levelsArray.add(level);
                    }
                    enchantments.add(String.valueOf(Registry.ENCHANTMENT.getId(enchant.getEnchantment())), levelsArray);
                }

                autoBuyJsonObject.add("enchants", enchantments);
            }

            if (autoBuyItem instanceof AutoBuyToolTipItem nameItem) {
                JsonArray tooltipsArray = new JsonArray();
                for (String tooltip : nameItem.getToolTips()) {
                    tooltipsArray.add(tooltip);
                }
                autoBuyJsonObject.add("tooltips", tooltipsArray);
            }

            autoBuyArray.add(autoBuyJsonObject);
        }

        jsonObject.add("items", autoBuyArray);
        return jsonObject;
    }
}