package org.obsidian.client.api.events;

import org.obsidian.client.Obsidian;

public class Event {
    public String getName() {
        return this.getClass().getSimpleName().toLowerCase();
    }

    public void hook() {
        Obsidian.eventHandler().post(this);
    }
}
