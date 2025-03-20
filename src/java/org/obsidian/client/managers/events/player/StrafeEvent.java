package org.obsidian.client.managers.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.vector.Vector3d;
import org.obsidian.client.api.events.CancellableEvent;

@Getter
@Setter
@AllArgsConstructor
public final class StrafeEvent extends CancellableEvent {
    private float friction;
    private Vector3d relative;
    private float yaw;
}