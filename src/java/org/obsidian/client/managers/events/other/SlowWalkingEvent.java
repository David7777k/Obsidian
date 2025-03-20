package org.obsidian.client.managers.events.other;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.obsidian.client.api.events.CancellableEvent;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SlowWalkingEvent extends CancellableEvent {
    private final float forward, strafe;
}