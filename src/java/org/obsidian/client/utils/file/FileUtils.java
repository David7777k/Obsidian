package org.obsidian.client.utils.file;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.io.*;
import java.util.stream.Collectors;

@UtilityClass
public final class FileUtils {
    @SneakyThrows
    public String readFile(File file) {
        @Cleanup BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        return bufferedReader.lines().collect(Collectors.joining("\n"));
    }

    @SneakyThrows
    public void writeFile(File file, String content) {
        @Cleanup FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(content);
    }

    @SneakyThrows
    public String readInputStream(InputStream inputStream) {
        @Cleanup BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        return reader.lines().collect(Collectors.joining("\n"));
    }
}
