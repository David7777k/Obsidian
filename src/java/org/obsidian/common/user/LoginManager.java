package org.obsidian.common.user;

import lombok.experimental.UtilityClass;
import org.obsidian.client.api.client.Constants;
import org.obsidian.client.utils.other.NameGenerator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@UtilityClass
public class LoginManager {

    public String loadUsername() {
        Path filePath = Constants.MAIN_DIR.resolve("username");
        if (Files.exists(filePath)) {
            try {
                return Files.readAllLines(filePath).stream().findFirst().orElse(NameGenerator.generate());
            } catch (IOException e) {
                System.err.println("Error reading username from file: " + e.getMessage());
            }
        }
        return NameGenerator.generate();
    }

    public void saveUsername(String username) {
        Path filePath = Constants.MAIN_DIR.resolve("username");
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(username);
        } catch (IOException e) {
            System.err.println("Error saving username to file: " + e.getMessage());
        }
    }

}
