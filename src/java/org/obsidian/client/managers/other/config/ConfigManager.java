package org.obsidian.client.managers.other.config;

import lombok.extern.log4j.Log4j2;
import org.obsidian.client.api.client.Constants;
import org.obsidian.client.api.interfaces.IExecutor;
import org.obsidian.client.utils.file.FileManager;
import org.obsidian.client.utils.file.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
public class ConfigManager extends ArrayList<ConfigFile> implements IExecutor {

    public static final File CONFIG_DIRECTORY = new File(FileManager.DIRECTORY, FileType.CONFIG.getName());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public ConfigManager() {
        init();
    }

    private void init() {
        if (!CONFIG_DIRECTORY.exists() && !CONFIG_DIRECTORY.mkdir()) {
            log.error("Не удалось создать папку {}", FileType.CONFIG.getName());
            System.exit(0);
        }
        update();
    }

    public ConfigFile get(final String config) {
        return new ConfigFile(new File(CONFIG_DIRECTORY, config + Constants.FILE_FORMAT));
    }

    public ConfigFile get() {
        return get("default");
    }

    public void set(final String config) {
        executorService.execute(() -> {
            ConfigFile configFile = get(config);
            if (!this.contains(configFile)) {
                add(configFile);
            }
            configFile.write();
        });
    }

    public void set() {
        set("default");
    }

    public boolean update() {
        clear();
        File[] files = CONFIG_DIRECTORY.listFiles((dir, name) -> name.endsWith(Constants.FILE_FORMAT));
        if (files == null) {
            return false;
        }
        for (File file : files) {
            add(new ConfigFile(file));
        }
        return true;
    }

    public boolean delete(final String config) {
        ConfigFile configFile = get(config);
        if (configFile == null) {
            return false;
        }
        remove(configFile);
        return configFile.getFile().delete();
    }
}
