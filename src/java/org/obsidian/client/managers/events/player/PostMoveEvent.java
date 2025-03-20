package org.obsidian.client.managers.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.obsidian.client.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public class PostMoveEvent extends Event {
    private double horizontalMove;
}