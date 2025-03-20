import org.fusesource.jansi.Ansi;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class Builder {
    private static final List<String> EXCLUDED_CLASSES = List.of("Builder.class", "FontGenerator.class", "module-info.class");
    private static final List<String> EXCLUDED_FOLDERS = List.of("lombok/", "generated/");
    private static final String GREEN = Ansi.ansi().fgRgb(0, 255, 0).toString();
    private static final String RED = Ansi.ansi().fgRgb(255, 0, 0).toString();
    private static final String WHITE = Ansi.ansi().fgRgb(255, 255, 255).toString();

    private final String inJar;
    private final String outJar;

    public Builder(String inJar, String outJar) {
        this.inJar = inJar;
        this.outJar = outJar;
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Usage: java -jar builder.jar in.jar");
            return;
        }

        String inJar = args[0];
        String outJar = "temp.jar";

        Builder builder = new Builder(inJar, outJar);
        System.out.println(GREEN + "Процесс сборки начался!");
        builder.cleanup();
        builder.extractClassesToJar();
        System.out.println(GREEN + "Сборка завершена!");
    }

    private void cleanup() {
        deleteIfExists(outJar);
    }

    private void deleteIfExists(String fileName) {
        Path path = Paths.get(fileName);
        try {
            if (Files.exists(path)) {
                Files.delete(path);
                System.out.println(RED + "Удален файл: " + fileName);
            }
        } catch (IOException e) {
            System.err.println(RED + "Ошибка при удалении файла: " + fileName);
        }
    }

    public void extractClassesToJar() {
        try (JarFile jarFile = new JarFile(inJar);
             JarOutputStream jos = new JarOutputStream(new FileOutputStream(outJar))) {

            Enumeration<JarEntry> entries = jarFile.entries();
            int totalEntries = jarFile.size();
            int processedEntries = 0;

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && shouldCopyEntry(entry)) {
                    copyEntry(jarFile, entry, jos);
                }
                processedEntries++;
                drawProgress(processedEntries, totalEntries);
            }
            System.out.println();
        } catch (IOException e) {
            System.err.println(RED + "Ошибка при сборке " + inJar);
        }
        try {
            Path jarPath = Paths.get(inJar);
            Files.deleteIfExists(jarPath);
            Files.move(Paths.get(outJar), jarPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println(RED + "Ошибка при удалении или перезаписи " + inJar);
        }
    }

    private boolean shouldCopyEntry(JarEntry entry) {
        return !isExcludedFolders(entry) && !isExcludedClasses(entry);
    }

    private void drawProgress(double processedEntries, int totalEntries) {
        double progress = processedEntries / totalEntries * 100;
        int barLength = 20;
        int filledChars = (int) (barLength * progress / 100.0);

        StringBuilder progressBar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            progressBar.append(i < filledChars ? GREEN + "|" : RED + ".");
        }
        progressBar.append(WHITE).append("] ").append((int) progress).append("%");

        System.out.printf("\r%s", progressBar);
    }

    private void copyEntry(JarFile jarFile, JarEntry entry, JarOutputStream jos) throws IOException {
        try (InputStream inputStream = jarFile.getInputStream(entry)) {
            byte[] buffer = new byte[16384];
            int bytesRead;

            JarEntry newEntry = new JarEntry(entry.getName());
            jos.putNextEntry(newEntry);
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                jos.write(buffer, 0, bytesRead);
            }
        }
    }

    private boolean isExcludedFolders(JarEntry entry) {
        String name = entry.getName().toLowerCase();
        return EXCLUDED_FOLDERS.stream().anyMatch(name::startsWith);
    }

    private boolean isExcludedClasses(JarEntry entry) {
        return EXCLUDED_CLASSES.contains(entry.getName());
    }
}