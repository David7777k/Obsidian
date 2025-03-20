package org.obsidian.client.managers.events.player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.obsidian.client.api.events.CancellableEvent;
import org.obsidian.client.api.interfaces.IMinecraft;

@Getter
@Setter
@AllArgsConstructor
public final class MotionEvent extends CancellableEvent implements IMinecraft {
    private double x, y, z;
    private float yaw, pitch;
    private boolean onGround;
    private boolean isSneaking;
    private boolean isSprinting;

    public void setYaw(float yaw) {
        this.yaw = yaw;
        mc.player.rotationYawHead = yaw;
        mc.player.renderYawOffset = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
        mc.player.rotationPitchHead = pitch;
    }

}