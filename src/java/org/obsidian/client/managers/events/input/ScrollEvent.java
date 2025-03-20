package org.obsidian.client.managers.events.input;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.obsidian.client.api.events.CancellableEvent;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ScrollEvent extends CancellableEvent {
    @Getter
    private static final ScrollEvent instance = new ScrollEvent();
    private double scroll;
    private double mouseX;
    private double mouseY;

    public void set(double scroll, double mouseX, double mouseY) {
        this.scroll = scroll;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }
}
