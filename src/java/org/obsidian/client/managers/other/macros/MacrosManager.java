package org.obsidian.client.managers.other.macros;


import lombok.extern.log4j.Log4j2;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.client.Constants;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.input.KeyboardPressEvent;
import org.obsidian.client.managers.events.input.MousePressEvent;
import org.obsidian.client.utils.chat.ChatUtil;
import org.obsidian.client.utils.file.FileManager;
import org.obsidian.client.utils.file.FileType;

import java.io.File;
import java.util.ArrayList;

@Log4j2
public class MacrosManager extends ArrayList<Macro> {
    public static File MACROS_DIRECTORY;

    public MacrosManager() {
        init();
    }


    public void init() {
        MACROS_DIRECTORY = new File(FileManager.DIRECTORY, FileType.MACROS.getName());
        if (!MACROS_DIRECTORY.exists()) {
            if (!MACROS_DIRECTORY.mkdir()) {
                log.error("Не удалось создать папку {}", FileType.MACROS.getName());
                System.exit(0);
            }
        }

        Obsidian.eventHandler().subscribe(this);
    }

    public MacrosFile get() {
        final File file = new File(MACROS_DIRECTORY, FileType.MACROS.getName() + Constants.FILE_FORMAT);
        return new MacrosFile(file);
    }

    public void set() {
        final File file = new File(MACROS_DIRECTORY, FileType.MACROS.getName() + Constants.FILE_FORMAT);
        MacrosFile macrosFile = get();
        if (macrosFile == null) {
            macrosFile = new MacrosFile(file);
        }
        macrosFile.write();
    }

    public void addMacro(String name, int keyCode, String message) {
        Macro macros = new Macro(name, keyCode, message);
        if (!this.contains(macros)) {
            this.add(macros);
            set();
        }
    }

    public Macro getMacro(String name) {
        return this.stream().filter(macros -> macros.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Macro getMacro(int keyCode) {
        return this.stream().filter(macros -> macros.getKey() == keyCode).findFirst().orElse(null);
    }

    public void removeMacro(String name) {
        this.removeIf(macros -> macros.getName().equalsIgnoreCase(name));
        set();
    }

    public void removeMacro(int keyCode) {
        this.removeIf(macros -> macros.getKey() == keyCode);
        set();
    }

    public void clearMacros() {
        this.clear();
        set();
    }

    @EventHandler
    public void onKeyPress(KeyboardPressEvent event) {
        if (event.getScreen() != null) return;
        this.forEach(macros -> {
            if (macros.getKey() == event.getKey()) {
                ChatUtil.sendText(macros.getMessage());
            }
        });
    }

    @EventHandler
    public void onMousePress(MousePressEvent event) {
        if (event.getScreen() != null) return;
        this.forEach(macros -> {
            if (macros.getKey() == event.getKey()) {
                ChatUtil.sendText(macros.getMessage());
            }
        });
    }

    public boolean hasMacro(String macroName) {
        for (Macro macro : this) {
            if (macro.getName().equalsIgnoreCase(macroName)) {
                return true;
            }
        }
        return false;
    }
}
