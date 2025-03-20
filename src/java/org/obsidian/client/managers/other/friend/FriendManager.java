package org.obsidian.client.managers.other.friend;


import lombok.extern.log4j.Log4j2;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.client.Constants;
import org.obsidian.client.utils.file.FileManager;
import org.obsidian.client.utils.file.FileType;

import java.io.File;
import java.util.ArrayList;

@Log4j2
public class FriendManager extends ArrayList<String> {
    public static File FRIEND_DIRECTORY;

    public FriendManager() {
        init();
    }


    public void init() {
        FRIEND_DIRECTORY = new File(FileManager.DIRECTORY, FileType.FRIEND.getName());
        if (!FRIEND_DIRECTORY.exists()) {
            if (!FRIEND_DIRECTORY.mkdir()) {
                log.error("Не удалось создать папку {}", FileType.FRIEND.getName());
                System.exit(0);
            }
        }

        Obsidian.eventHandler().subscribe(this);
    }

    public FriendFile get() {
        final File file = new File(FRIEND_DIRECTORY, FileType.FRIEND.getName() + Constants.FILE_FORMAT);
        return new FriendFile(file);
    }

    public void set() {
        final File file = new File(FRIEND_DIRECTORY, FileType.FRIEND.getName() + Constants.FILE_FORMAT);
        FriendFile friendFile = get();
        if (friendFile == null) {
            friendFile = new FriendFile(file);
        }
        friendFile.write();
    }


    public void addFriend(String name) {
        if (isFriend(name)) return;
        this.add(name);
        set();
    }

    public String getFriend(String name) {
        return this.stream().filter(friend -> friend.equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public boolean isFriend(String name) {
        return this.stream().anyMatch(friend -> friend.equalsIgnoreCase(name));
    }

    public void removeFriend(String name) {
        this.removeIf(friend -> friend.equalsIgnoreCase(name));
        set();
    }

    public void clearFriends() {
        this.clear();
        set();
    }

}
