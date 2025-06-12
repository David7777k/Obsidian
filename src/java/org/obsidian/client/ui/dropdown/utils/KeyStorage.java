package org.obsidian.client.ui.dropdown.utils;

import org.lwjgl.glfw.GLFW;

public class KeyStorage {
    public static String getKey(int key) {
        if (key == -1) return null;
        String name = GLFW.glfwGetKeyName(key, 0);
        if (name == null) return "" + key;
        return name.toUpperCase();
    }

    public static String getReverseKey(int key) {
        return getKey(key);
    }
}
