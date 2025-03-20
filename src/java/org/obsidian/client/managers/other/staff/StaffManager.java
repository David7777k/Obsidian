package org.obsidian.client.managers.other.staff;


import lombok.extern.log4j.Log4j2;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.client.Constants;
import org.obsidian.client.utils.file.FileManager;
import org.obsidian.client.utils.file.FileType;

import java.io.File;
import java.util.ArrayList;

@Log4j2
public class StaffManager extends ArrayList<String> {
    public static File STAFF_DIRECTORY;

    public StaffManager() {
        init();
    }


    public void init() {
        STAFF_DIRECTORY = new File(FileManager.DIRECTORY, FileType.STAFF.getName());
        if (!STAFF_DIRECTORY.exists()) {
            if (!STAFF_DIRECTORY.mkdir()) {
                log.error("Не удалось создать папку {}", FileType.STAFF.getName());
                System.exit(0);
            }
        }

        Obsidian.eventHandler().subscribe(this);
    }

    public StaffFile get() {
        final File file = new File(STAFF_DIRECTORY, FileType.STAFF.getName() + Constants.FILE_FORMAT);
        return new StaffFile(file);
    }

    public void set() {
        final File file = new File(STAFF_DIRECTORY, FileType.STAFF.getName() + Constants.FILE_FORMAT);
        StaffFile staffFile = get();
        if (staffFile == null) {
            staffFile = new StaffFile(file);
        }
        staffFile.write();
    }


    public void addStaff(String name) {
        if (isStaff(name)) return;
        this.add(name);
        set();
    }

    public String getStaff(String name) {
        return this.stream().filter(staff -> staff.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public boolean isStaff(String name) {
        return this.stream().anyMatch(staff -> staff.equalsIgnoreCase(name));
    }

    public void removeStaff(String name) {
        this.removeIf(staff -> staff.equalsIgnoreCase(name));
        set();
    }

    public void clearStaffs() {
        this.clear();
        set();
    }

}
