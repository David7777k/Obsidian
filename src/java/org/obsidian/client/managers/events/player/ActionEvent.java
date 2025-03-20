package org.obsidian.client.managers.events.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.obsidian.client.api.events.Event;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ActionEvent extends Event {
    private boolean sprintState;
}