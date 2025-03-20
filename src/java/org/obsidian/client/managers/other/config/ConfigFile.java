package org.obsidian.client.managers.other.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.client.Constants;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.settings.Setting;
import org.obsidian.client.managers.module.settings.impl.*;
import org.obsidian.client.utils.file.AbstractFile;
import org.obsidian.client.utils.file.FileType;
import org.obsidian.client.utils.render.color.ColorUtil;

import java.io.*;
import java.util.function.Consumer;


public class ConfigFile extends AbstractFile {

    public ConfigFile(final File file) {
        super(file, FileType.CONFIG);
    }

    @Override
    public boolean read() {
        if (!this.getFile().exists()) {
            return false;
        }

        try (FileReader fileReader = new FileReader(getFile()); // Try-with-resources
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {

            final JsonObject jsonObject = GSON.fromJson(bufferedReader, JsonObject.class);

            if (jsonObject == null) {
                return false;
            }

            if (jsonObject.has("module")) {
                loadModuleSettings(jsonObject.getAsJsonObject("module"));
            }

        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public boolean write() {
        try (FileWriter fileWriter = new FileWriter(getFile()); // Try-with-resources
             BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            if (!this.getFile().exists()) {
                if (!this.getFile().createNewFile()) {
                    System.err.println("Ошибка при создании файла конфига.");
                    System.exit(0);
                }
            }

            final JsonObject jsonObject = new JsonObject();
            final JsonObject infoJsonObject = new JsonObject();
            infoJsonObject.addProperty("build", Constants.VERSION);
            jsonObject.add(Constants.NAME, infoJsonObject);

            JsonObject modulesObject = new JsonObject();
            saveModuleSettings(modulesObject);
            jsonObject.add("module", modulesObject);

            GSON.toJson(jsonObject, bufferedWriter);
            bufferedWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void loadModuleSettings(JsonObject jsonObject) {
        for (Module module : Obsidian.inst().moduleManager().values()) {
            JsonObject moduleObject = jsonObject.getAsJsonObject(module.getName().replaceAll(" ", ""));
            if (moduleObject == null) {
                continue;
            }

            module.setEnabled(false, false);
            loadSettingFromJson(moduleObject, "key", value -> module.setKey(value.getAsInt()));
            loadSettingFromJson(moduleObject, "state", value -> module.setEnabled(value.getAsBoolean(), false));
            module.getSettings().forEach(setting -> loadIndividualSetting(moduleObject, setting));
        }
    }

    private void loadIndividualSetting(JsonObject jsonObject, Setting<?> setting) {
        JsonElement settingElement = jsonObject.get(setting.getName());

        if (settingElement == null || settingElement.isJsonNull()) {
            return;
        }

        if (setting instanceof BindSetting value) {
            value.set(settingElement.getAsInt());
        }
        if (setting instanceof BooleanSetting value) {
            value.set(settingElement.getAsBoolean());
        }
        if (setting instanceof ColorSetting value) {
            value.set(ColorUtil.replAlpha(settingElement.getAsInt(), 1F));
        }
        if (setting instanceof DragSetting value) {
            JsonObject elements = jsonObject.getAsJsonObject(value.getName());
            if (elements.has("x") && elements.has("y") && elements.has("width") && elements.has("height")) {
                value.position.set(elements.get("x").getAsFloat(), elements.get("y").getAsFloat());
                value.targetPosition.set(elements.get("x").getAsFloat(), elements.get("y").getAsFloat());
                value.size.set(elements.get("width").getAsFloat(), elements.get("height").getAsFloat());
            }
        }
        if (setting instanceof ListSetting<?> value) {
            for (Object mode : value.values) {
                if (mode.toString().equals(settingElement.getAsString())) {
                    value.setAsObject(mode);
                    break;
                }
            }
        }
        if (setting instanceof ModeSetting value) {
            for (String mode : value.values) {
                if (mode.equals(settingElement.getAsString())) {
                    value.set(settingElement.getAsString());
                    break;
                }
            }
        }
        if (setting instanceof MultiBooleanSetting value) {
            JsonObject elements = jsonObject.getAsJsonObject(value.getName());
            value.getValues().forEach(x -> {
                JsonElement element = elements.get(x.getName());
                if (element != null && !element.isJsonNull()) {
                    x.set(element.getAsBoolean());
                }
            });
        }
        if (setting instanceof SliderSetting value) {
            value.set(settingElement.getAsFloat());
        }
        if (setting instanceof StringSetting value) {
            value.set(settingElement.getAsString());
        }
    }

    private void loadSettingFromJson(JsonObject jsonObject, String key, Consumer<JsonElement> consumer) {
        JsonElement element = jsonObject.get(key);
        if (element != null && !element.isJsonNull()) {
            consumer.accept(element);
        }
    }

    private void saveModuleSettings(JsonObject jsonObject) {
        Obsidian.inst().moduleManager().values().forEach(module -> {
            JsonObject moduleObject = new JsonObject();
            moduleObject.addProperty("key", module.getKey());
            moduleObject.addProperty("state", module.isEnabled());
            module.getSettings().forEach(setting -> saveIndividualSetting(moduleObject, setting));
            jsonObject.add(module.getName().replaceAll(" ", ""), moduleObject);
        });
    }

    private void saveIndividualSetting(JsonObject jsonObject, Setting<?> setting) {
        String settingName = setting.getName();
        if (setting instanceof BindSetting value) {
            jsonObject.addProperty(settingName, value.getValue());
        }
        if (setting instanceof BooleanSetting value) {
            jsonObject.addProperty(settingName, value.getValue());
        }
        if (setting instanceof ColorSetting value) {
            jsonObject.addProperty(settingName, ColorUtil.replAlpha(value.getValue(), 1F));
        }
        if (setting instanceof DragSetting value) {
            JsonObject elements = new JsonObject();

            elements.addProperty("x", value.position.x);
            elements.addProperty("y", value.position.y);
            elements.addProperty("width", value.size.x);
            elements.addProperty("height", value.size.y);

            jsonObject.add(settingName, elements);
        }
        if (setting instanceof ListSetting<?> value) {
            jsonObject.addProperty(settingName, value.getValue().toString());
        }
        if (setting instanceof ModeSetting value) {
            jsonObject.addProperty(settingName, value.getValue());
        }
        if (setting instanceof MultiBooleanSetting value) {
            JsonObject elements = new JsonObject();
            value.getValues().forEach(x -> elements.addProperty(x.getName(), x.getValue()));
            jsonObject.add(settingName, elements);
        }
        if (setting instanceof SliderSetting value) {
            jsonObject.addProperty(settingName, value.getValue());
        }
        if (setting instanceof StringSetting value) {
            jsonObject.addProperty(settingName, value.getValue());
        }
    }
}