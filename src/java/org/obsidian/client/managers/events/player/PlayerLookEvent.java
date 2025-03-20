package org.obsidian.client.managers.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.obsidian.client.api.events.Event;
import org.joml.Vector2f;

@Getter
@Setter
@AllArgsConstructor
public final class PlayerLookEvent extends Event {
    private Vector2f rotation;
}