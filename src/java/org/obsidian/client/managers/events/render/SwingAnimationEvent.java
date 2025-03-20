package org.obsidian.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.obsidian.client.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public final class SwingAnimationEvent extends Event {
    private int animation;
}