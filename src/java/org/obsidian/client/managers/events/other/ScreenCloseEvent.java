package org.obsidian.client.managers.events.other;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.screen.Screen;
import org.obsidian.client.api.events.CancellableEvent;

@Getter
@AllArgsConstructor
public class ScreenCloseEvent extends CancellableEvent {
    private Screen screen;
    private int windowId;
}