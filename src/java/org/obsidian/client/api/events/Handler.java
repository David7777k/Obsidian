package org.obsidian.client.api.events;


import org.obsidian.client.Obsidian;

public abstract class Handler {
    public Handler() {
        Obsidian.eventHandler().subscribe(this);
    }
}