package org.obsidian.client.managers.events.other;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.obsidian.client.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public class AspectRatioEvent extends Event {
    private float aspectRatio;
}
