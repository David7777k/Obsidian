package org.obsidian.client.managers.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.obsidian.client.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public class MoveEvent extends Event {
    private Vector3d from, to, motion;
    private boolean toGround;
    private AxisAlignedBB aabbFrom;
    private boolean ignoreHorizontal, ignoreVertical, collidedHorizontal, collidedVertical;

}