package org.obsidian.client.managers.events.world;

import lombok.Getter;
import org.obsidian.client.api.events.Event;

public final class WorldChangeEvent extends Event {
    @Getter
    private static final WorldChangeEvent instance = new WorldChangeEvent();
}