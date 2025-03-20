package org.obsidian.client.managers.module.impl.render;

import org.lwjgl.glfw.GLFW;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;

import java.lang.reflect.Field;

@ModuleInfo(name = "AutoF5", category = Category.RENDER)
public class AutoF5 extends Module {

    // Если класс KeySetting существует, убедитесь, что он импортирован правильно
    private final KeySetting toggleKey = new KeySetting(this, "Переключатель", GLFW.GLFW_KEY_C);
    private boolean wasPressed = false;

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (isKeyHeldDown(toggleKey.getKey())) {
            if (!wasPressed) {
                toggleF5View();
                wasPressed = true;
            }
        } else {
            wasPressed = false;
        }
    }

    private void toggleF5View() {
        try {
            Field field = mc.gameSettings.getClass().getDeclaredField("thirdPersonView");
            field.setAccessible(true);
            int currentView = field.getInt(mc.gameSettings);
            // Переключение между режимами: 0 - первый вид, 1 - третий вид сзади, 2 - третий вид спереди.
            field.setInt(mc.gameSettings, (currentView + 1) % 3);
        } catch (Exception e) {
            // Вместо printStackTrace() стоит использовать ваш логгер.
            e.printStackTrace();
        }
    }

    private boolean isKeyHeldDown(int key) {
        // Если mc.getMainWindow() доступен, используем его для получения handle окна.
        return GLFW.glfwGetKey(mc.getMainWindow().getHandle(), key) == GLFW.GLFW_PRESS;
    }

    private record KeySetting(AutoF5 autoF5, String переключатель, int glfwKeyC) {
        public int getKey() {
            return glfwKeyC;

        }
    }
}
