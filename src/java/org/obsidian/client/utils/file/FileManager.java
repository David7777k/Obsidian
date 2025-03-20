package org.obsidian.client.utils.file;


import lombok.extern.log4j.Log4j2;
import org.obsidian.client.api.client.Constants;
import org.obsidian.client.api.interfaces.IMinecraft;

import java.io.File;

@Log4j2
public class FileManager implements IMinecraft {
    public static File DIRECTORY;

    public FileManager() {
        DIRECTORY = new File(mc.gameDir, Constants.NAMESPACE);
        if (!DIRECTORY.exists()) {
            if (!DIRECTORY.mkdir()) {
                log.error("Не удалось создать папку {}", Constants.NAMESPACE);
                System.exit(0);
            }
        }
    }
}