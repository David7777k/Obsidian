package org.obsidian.client.managers.events.player;

import lombok.Getter;
import org.obsidian.client.api.events.Event;

public class UpdateEvent extends Event {
    @Getter
    private static final UpdateEvent instance = new UpdateEvent();
}