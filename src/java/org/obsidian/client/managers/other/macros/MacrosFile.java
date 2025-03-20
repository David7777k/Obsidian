package org.obsidian.client.managers.other.macros;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NonNull;
import org.obsidian.client.Obsidian;
import org.obsidian.client.utils.file.AbstractFile;
import org.obsidian.client.utils.file.FileType;

import java.io.*;

public class MacrosFile extends AbstractFile {
    public MacrosFile(File file) {
        super(file, FileType.MACROS);
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

            JsonArray macrosArray = jsonObject.getAsJsonArray("macros");
            if (macrosArray != null) {
                for (JsonElement macrosElement : macrosArray) {
                    JsonObject macrosJSONElement = macrosElement.getAsJsonObject();
                    String name = macrosJSONElement.get("name").getAsString();
                    int keycode = macrosJSONElement.get("keycode").getAsInt();
                    String message = macrosJSONElement.get("message").getAsString();
                    Macro macros = new Macro(name, keycode, message);
                    Obsidian.inst().macrosManager().add(macros);
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
                    System.out.println("Файл с списком макросов успешно создана.");
                } else {
                    System.out.println("Произошла ошибка при создании файла с списком макросов.");
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
        JsonArray macrosArray = new JsonArray();

        for (Macro macros : Obsidian.inst().macrosManager()) {
            final JsonObject macrosJsonObject = new JsonObject();
            macrosJsonObject.addProperty("name", macros.getName());
            macrosJsonObject.addProperty("keycode", macros.getKey());
            macrosJsonObject.addProperty("message", macros.getMessage());
            macrosArray.add(macrosJsonObject);
        }

        jsonObject.add("macros", macrosArray);

        return jsonObject;
    }
}
