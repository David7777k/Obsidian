package org.obsidian.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public class RenderItemEvent extends Event {
    private MatrixStack matrix;
    private final Hand hand;
    private final HandSide handSide;
    private float swingProgress;
    private float equippedProgress;
}