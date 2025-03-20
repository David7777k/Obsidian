package org.obsidian.client.managers.events.player;

import lombok.Getter;
import org.obsidian.client.api.events.Event;

public class PostUpdateEvent extends Event {
    @Getter
    private static final PostUpdateEvent instance = new PostUpdateEvent();
}