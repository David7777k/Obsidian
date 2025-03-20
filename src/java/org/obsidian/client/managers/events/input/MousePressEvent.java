package org.obsidian.client.managers.events.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.gui.screen.Screen;
import org.obsidian.client.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MousePressEvent extends Event {
    @Getter
    private static final MousePressEvent instance = new MousePressEvent();
    private int key;
    private Screen screen;
    private double mouseX, mouseY;

    public void set(int key, Screen screen, double mouseX, double mouseY) {
        this.key = key;
        this.screen = screen;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public boolean isKey(int key) {
        return this.key == key;
    }
}