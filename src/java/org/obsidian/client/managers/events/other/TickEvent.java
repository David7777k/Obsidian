package org.obsidian.client.managers.events.other;

import lombok.Getter;
import org.obsidian.client.api.events.Event;

public class TickEvent extends Event {
    @Getter
    private static final TickEvent instance = new TickEvent();
}
