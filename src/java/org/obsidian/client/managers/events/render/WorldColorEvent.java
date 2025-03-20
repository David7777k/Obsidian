package org.obsidian.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.obsidian.client.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public class WorldColorEvent extends Event {
    private float red, green, blue;
}
