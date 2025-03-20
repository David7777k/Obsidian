package org.obsidian.client.managers.events.world;

import lombok.Getter;
import org.obsidian.client.api.events.Event;

public class WorldLoadEvent extends Event {
    @Getter
    private static final WorldLoadEvent instance = new WorldLoadEvent();
}