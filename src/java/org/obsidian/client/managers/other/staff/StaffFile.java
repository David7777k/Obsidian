package org.obsidian.client.managers.other.staff;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NonNull;
import org.obsidian.client.Obsidian;
import org.obsidian.client.utils.file.AbstractFile;
import org.obsidian.client.utils.file.FileType;

import java.io.*;

public class StaffFile extends AbstractFile {
    public StaffFile(File file) {
        super(file, FileType.STAFF);
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

            JsonArray staffsArray = jsonObject.getAsJsonArray("staffs");
            if (staffsArray != null) {
                for (JsonElement staffElement : staffsArray) {
                    JsonObject staffJSONElement = staffElement.getAsJsonObject();
                    String name = staffJSONElement.get("name").getAsString();
                    Obsidian.inst().staffManager().add(name);
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
                    System.out.println("Файл с списком друзей успешно создан.");
                } else {
                    System.out.println("Произошла ошибка при создании файла с списком друзей.");
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
        JsonArray staffsArray = new JsonArray();

        for (String staff : Obsidian.inst().staffManager()) {
            final JsonObject staffJsonObject = new JsonObject();
            staffJsonObject.addProperty("name", staff);
            staffsArray.add(staffJsonObject);
        }

        jsonObject.add("staffs", staffsArray);

        return jsonObject;
    }
}
