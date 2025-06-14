package org.obsidian.client.utils.keyboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.util.InputMappings;
import org.lwjgl.glfw.GLFW;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.api.interfaces.IWindow;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum Keyboard implements IWindow {

    KEY_SPACE("SPACE", GLFW.GLFW_KEY_SPACE),
    KEY_APOSTROPHE("APOSTROPHE", GLFW.GLFW_KEY_APOSTROPHE),
    KEY_COMMA("COMMA", GLFW.GLFW_KEY_COMMA),
    KEY_MINUS("MINUS", GLFW.GLFW_KEY_MINUS),
    KEY_PERIOD("PERIOD", GLFW.GLFW_KEY_PERIOD),
    KEY_SLASH("SLASH", GLFW.GLFW_KEY_SLASH),
    KEY_0("0", GLFW.GLFW_KEY_0),
    KEY_1("1", GLFW.GLFW_KEY_1),
    KEY_2("2", GLFW.GLFW_KEY_2),
    KEY_3("3", GLFW.GLFW_KEY_3),
    KEY_4("4", GLFW.GLFW_KEY_4),
    KEY_5("5", GLFW.GLFW_KEY_5),
    KEY_6("6", GLFW.GLFW_KEY_6),
    KEY_7("7", GLFW.GLFW_KEY_7),
    KEY_8("8", GLFW.GLFW_KEY_8),
    KEY_9("9", GLFW.GLFW_KEY_9),
    KEY_SEMICOLON("SEMICOLON", GLFW.GLFW_KEY_SEMICOLON),
    KEY_EQUAL("EQUAL", GLFW.GLFW_KEY_EQUAL),
    KEY_A("A", GLFW.GLFW_KEY_A),
    KEY_B("B", GLFW.GLFW_KEY_B),
    KEY_C("C", GLFW.GLFW_KEY_C),
    KEY_D("D", GLFW.GLFW_KEY_D),
    KEY_E("E", GLFW.GLFW_KEY_E),
    KEY_F("F", GLFW.GLFW_KEY_F),
    KEY_G("G", GLFW.GLFW_KEY_G),
    KEY_H("H", GLFW.GLFW_KEY_H),
    KEY_I("I", GLFW.GLFW_KEY_I),
    KEY_J("J", GLFW.GLFW_KEY_J),
    KEY_K("K", GLFW.GLFW_KEY_K),
    KEY_L("L", GLFW.GLFW_KEY_L),
    KEY_M("M", GLFW.GLFW_KEY_M),
    KEY_N("N", GLFW.GLFW_KEY_N),
    KEY_O("O", GLFW.GLFW_KEY_O),
    KEY_P("P", GLFW.GLFW_KEY_P),
    KEY_Q("Q", GLFW.GLFW_KEY_Q),
    KEY_R("R", GLFW.GLFW_KEY_R),
    KEY_S("S", GLFW.GLFW_KEY_S),
    KEY_T("T", GLFW.GLFW_KEY_T),
    KEY_U("U", GLFW.GLFW_KEY_U),
    KEY_V("V", GLFW.GLFW_KEY_V),
    KEY_W("W", GLFW.GLFW_KEY_W),
    KEY_X("X", GLFW.GLFW_KEY_X),
    KEY_Y("Y", GLFW.GLFW_KEY_Y),
    KEY_Z("Z", GLFW.GLFW_KEY_Z),
    KEY_LEFT_BRACKET("LBRACKET", GLFW.GLFW_KEY_LEFT_BRACKET),
    KEY_BACKSLASH("BACKSLASH", GLFW.GLFW_KEY_BACKSLASH),
    KEY_RIGHT_BRACKET("RBRACKET", GLFW.GLFW_KEY_RIGHT_BRACKET),
    KEY_GRAVE("GRAVE", GLFW.GLFW_KEY_GRAVE_ACCENT),
    KEY_WORLD_1("WORLD1", GLFW.GLFW_KEY_WORLD_1),
    KEY_WORLD_2("WORLD2", GLFW.GLFW_KEY_WORLD_2),

    KEY_ESCAPE("ESCAPE", GLFW.GLFW_KEY_ESCAPE),
    KEY_ENTER("ENTER", GLFW.GLFW_KEY_ENTER),
    KEY_TAB("TAB", GLFW.GLFW_KEY_TAB),
    KEY_BACKSPACE("BACKSPACE", GLFW.GLFW_KEY_BACKSPACE),
    KEY_INSERT("INSERT", GLFW.GLFW_KEY_INSERT),
    KEY_DELETE("DELETE", GLFW.GLFW_KEY_DELETE),
    KEY_RIGHT("RIGHT", GLFW.GLFW_KEY_RIGHT),
    KEY_LEFT("LEFT", GLFW.GLFW_KEY_LEFT),
    KEY_DOWN("DOWN", GLFW.GLFW_KEY_DOWN),
    KEY_UP("UP", GLFW.GLFW_KEY_UP),
    KEY_PAGE_UP("PAGEUP", GLFW.GLFW_KEY_PAGE_UP),
    KEY_PAGE_DOWN("PAGEDOWN", GLFW.GLFW_KEY_PAGE_DOWN),
    KEY_HOME("HOME", GLFW.GLFW_KEY_HOME),
    KEY_END("END", GLFW.GLFW_KEY_END),
    KEY_CAPS_LOCK("CAPSLOCK", GLFW.GLFW_KEY_CAPS_LOCK),
    KEY_SCROLL_LOCK("SCROLLLOCK", GLFW.GLFW_KEY_SCROLL_LOCK),
    KEY_NUM_LOCK("NUMLOCK", GLFW.GLFW_KEY_NUM_LOCK),
    KEY_PRINT_SCREEN("PRINTSCREEN", GLFW.GLFW_KEY_PRINT_SCREEN),
    KEY_PAUSE("PAUSE", GLFW.GLFW_KEY_PAUSE),
    KEY_F1("F1", GLFW.GLFW_KEY_F1),
    KEY_F2("F2", GLFW.GLFW_KEY_F2),
    KEY_F3("F3", GLFW.GLFW_KEY_F3),
    KEY_F4("F4", GLFW.GLFW_KEY_F4),
    KEY_F5("F5", GLFW.GLFW_KEY_F5),
    KEY_F6("F6", GLFW.GLFW_KEY_F6),
    KEY_F7("F7", GLFW.GLFW_KEY_F7),
    KEY_F8("F8", GLFW.GLFW_KEY_F8),
    KEY_F9("F9", GLFW.GLFW_KEY_F9),
    KEY_F10("F10", GLFW.GLFW_KEY_F10),
    KEY_F11("F11", GLFW.GLFW_KEY_F11),
    KEY_F12("F12", GLFW.GLFW_KEY_F12),
    KEY_F13("F13", GLFW.GLFW_KEY_F13),
    KEY_F14("F14", GLFW.GLFW_KEY_F14),
    KEY_F15("F15", GLFW.GLFW_KEY_F15),
    KEY_F16("F16", GLFW.GLFW_KEY_F16),
    KEY_F17("F17", GLFW.GLFW_KEY_F17),
    KEY_F18("F18", GLFW.GLFW_KEY_F18),
    KEY_F19("F19", GLFW.GLFW_KEY_F19),
    KEY_F20("F20", GLFW.GLFW_KEY_F20),
    KEY_F21("F21", GLFW.GLFW_KEY_F21),
    KEY_F22("F22", GLFW.GLFW_KEY_F22),
    KEY_F23("F23", GLFW.GLFW_KEY_F23),
    KEY_F24("F24", GLFW.GLFW_KEY_F24),
    KEY_F25("F25", GLFW.GLFW_KEY_F25),
    KEY_KP_0("NUMPAD0", GLFW.GLFW_KEY_KP_0),
    KEY_KP_1("NUMPAD1", GLFW.GLFW_KEY_KP_1),
    KEY_KP_2("NUMPAD2", GLFW.GLFW_KEY_KP_2),
    KEY_KP_3("NUMPAD3", GLFW.GLFW_KEY_KP_3),
    KEY_KP_4("NUMPAD4", GLFW.GLFW_KEY_KP_4),
    KEY_KP_5("NUMPAD5", GLFW.GLFW_KEY_KP_5),
    KEY_KP_6("NUMPAD6", GLFW.GLFW_KEY_KP_6),
    KEY_KP_7("NUMPAD7", GLFW.GLFW_KEY_KP_7),
    KEY_KP_8("NUMPAD8", GLFW.GLFW_KEY_KP_8),
    KEY_KP_9("NUMPAD9", GLFW.GLFW_KEY_KP_9),
    KEY_KP_DECIMAL("KPDECIMAL", GLFW.GLFW_KEY_KP_DECIMAL),
    KEY_KP_DIVIDE("KPDIVIDE", GLFW.GLFW_KEY_KP_DIVIDE),
    KEY_KP_MULTIPLY("KPMULTIPLY", GLFW.GLFW_KEY_KP_MULTIPLY),
    KEY_KP_SUBTRACT("KPSUBTRACT", GLFW.GLFW_KEY_KP_SUBTRACT),
    KEY_KP_ADD("KPADD", GLFW.GLFW_KEY_KP_ADD),
    KEY_KP_ENTER("KPENTER", GLFW.GLFW_KEY_KP_ENTER),
    KEY_KP_EQUAL("KPEQUAL", GLFW.GLFW_KEY_KP_EQUAL),
    KEY_LEFT_SHIFT("LSHIFT", GLFW.GLFW_KEY_LEFT_SHIFT),
    KEY_LEFT_CONTROL("LCONTROL", GLFW.GLFW_KEY_LEFT_CONTROL),
    KEY_LEFT_ALT("LALT", GLFW.GLFW_KEY_LEFT_ALT),
    KEY_LEFT_SUPER("LSUPER", GLFW.GLFW_KEY_LEFT_SUPER),
    KEY_RIGHT_SHIFT("RSHIFT", GLFW.GLFW_KEY_RIGHT_SHIFT),
    KEY_RIGHT_CONTROL("RCONTROL", GLFW.GLFW_KEY_RIGHT_CONTROL),
    KEY_RIGHT_ALT("RALT", GLFW.GLFW_KEY_RIGHT_ALT),
    KEY_RIGHT_SUPER("RSUPER", GLFW.GLFW_KEY_RIGHT_SUPER),
    KEY_MENU("MENU", GLFW.GLFW_KEY_MENU),
    KEY_LAST("LAST", GLFW.GLFW_KEY_LAST),

    MOUSE_1("MOUSE1", GLFW.GLFW_MOUSE_BUTTON_1),
    MOUSE_2("MOUSE2", GLFW.GLFW_MOUSE_BUTTON_2),
    MOUSE_3("MOUSE3", GLFW.GLFW_MOUSE_BUTTON_3),
    MOUSE_4("MOUSE4", GLFW.GLFW_MOUSE_BUTTON_4),
    MOUSE_5("MOUSE5", GLFW.GLFW_MOUSE_BUTTON_5),
    MOUSE_6("MOUSE6", GLFW.GLFW_MOUSE_BUTTON_6),
    MOUSE_7("MOUSE7", GLFW.GLFW_MOUSE_BUTTON_7),
    MOUSE_8("MOUSE8", GLFW.GLFW_MOUSE_BUTTON_8),
    MOUSE_LAST("MOUSELAST", GLFW.GLFW_MOUSE_BUTTON_LAST),
    MOUSE_LEFT("MOUSELEFT", GLFW.GLFW_MOUSE_BUTTON_LEFT),
    MOUSE_RIGHT("MOUSERIGHT", GLFW.GLFW_MOUSE_BUTTON_RIGHT),
    MOUSE_MIDDLE("MOUSEMIDDLE", GLFW.GLFW_MOUSE_BUTTON_MIDDLE),


    KEY_NONE("NONE", GLFW.GLFW_KEY_UNKNOWN);

    private final String name;
    private final int key;

    @Override
    public String toString() {
        return name;
    }

    private static final Map<Integer, Keyboard> KEY_CODE_MAP = new HashMap<>();
    private static final Map<String, Keyboard> KEY_NAME_MAP = new HashMap<>();

    static {
        for (Keyboard key : Keyboard.values()) {
            KEY_CODE_MAP.put(key.key, key);
            KEY_NAME_MAP.put(key.name.toLowerCase(), key);
        }
    }

    public static String keyName(int keyCode) {
        Keyboard key = KEY_CODE_MAP.getOrDefault(keyCode, Keyboard.KEY_NONE);
        return key.name;
    }

    public static int keyCode(String keyName) {
        Keyboard key = KEY_NAME_MAP.getOrDefault(keyName.toLowerCase(), Keyboard.KEY_NONE);
        return key.key;
    }

    public static Keyboard findByKeyCode(int keyCode) {
        return KEY_CODE_MAP.getOrDefault(keyCode, Keyboard.KEY_NONE);
    }

    public static boolean isKeyDown(int keyCode) {

        if (keyCode < 0) return false;
        return InputMappings.isKeyDown(IMinecraft.mc.getMainWindow().getHandle(), keyCode);
    }

    public boolean isKey(int keyCode) {
        return keyCode == getKey();
    }

    public boolean isKeyDown() {
        if (this.key < 0) return false;
        return InputMappings.isKeyDown(IMinecraft.mc.getMainWindow().getHandle(), this.key);
    }
}