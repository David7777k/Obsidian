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
public final class KeyboardPressEvent extends Event {
    @Getter
    private static final KeyboardPressEvent instance = new KeyboardPressEvent();
    private int key;
    private Screen screen;

    public void set(int key, Screen screen) {
        this.key = key;
        this.screen = screen;
    }

    public boolean isKey(int key) {
        return this.key == key;
    }
}