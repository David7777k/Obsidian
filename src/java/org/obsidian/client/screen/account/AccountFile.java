package org.obsidian.client.screen.account;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.NonNull;
import org.obsidian.client.Obsidian;
import org.obsidian.client.utils.file.AbstractFile;
import org.obsidian.client.utils.file.FileType;

import java.io.*;
import java.time.LocalDateTime;

public class AccountFile extends AbstractFile {
    public AccountFile(File file) {
        super(file, FileType.ACCOUNT);
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

            JsonArray accountsArray = jsonObject.getAsJsonArray("accounts");
            if (accountsArray != null) {
                for (JsonElement accountElement : accountsArray) {
                    JsonObject accountJSONElement = accountElement.getAsJsonObject();
                    String name = accountJSONElement.get("name").getAsString();
                    LocalDateTime creationDate = LocalDateTime.parse(accountJSONElement.get("creationDate").getAsString());
                    boolean favorite = accountJSONElement.get("favorite").getAsBoolean();

                    Account account = new Account(creationDate, name);
                    account.favorite(favorite);

                    Obsidian.inst().accountManager().addAccount(account);
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
        JsonArray accountsArray = new JsonArray();

        for (Account account : Obsidian.inst().accountManager()) {
            final JsonObject accountJsonObject = new JsonObject();
            accountJsonObject.addProperty("name", account.name());
            accountJsonObject.addProperty("creationDate", account.creationDate().toString());
            accountJsonObject.addProperty("favorite", account.favorite());
            accountsArray.add(accountJsonObject);
        }

        jsonObject.add("accounts", accountsArray);

        return jsonObject;
    }
}
