package org.obsidian.client.managers.other.friend;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NonNull;
import org.obsidian.client.Obsidian;
import org.obsidian.client.utils.file.AbstractFile;
import org.obsidian.client.utils.file.FileType;

import java.io.*;

public class FriendFile extends AbstractFile {
    public FriendFile(File file) {
        super(file, FileType.FRIEND);
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

            JsonArray friendsArray = jsonObject.getAsJsonArray("friends");
            if (friendsArray != null) {
                for (JsonElement friendElement : friendsArray) {
                    JsonObject friendJSONElement = friendElement.getAsJsonObject();
                    String name = friendJSONElement.get("name").getAsString();
                    Obsidian.inst().friendManager().add(name);
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
        JsonArray friendsArray = new JsonArray();

        for (String friend : Obsidian.inst().friendManager()) {
            final JsonObject friendJsonObject = new JsonObject();
            friendJsonObject.addProperty("name", friend);
            friendsArray.add(friendJsonObject);
        }

        jsonObject.add("friends", friendsArray);

        return jsonObject;
    }
}
