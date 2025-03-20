package org.obsidian.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.events.CancellableEvent;

@Getter
@Setter
@AllArgsConstructor
public final class RenderCrosshairEvent extends CancellableEvent {
    private MatrixStack matrix;
    private float partialTicks;
    private double centerX, centerY;
}