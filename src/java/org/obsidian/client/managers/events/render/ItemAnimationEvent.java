package org.obsidian.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.events.CancellableEvent;

@Getter
@Setter
@AllArgsConstructor
public class ItemAnimationEvent extends CancellableEvent {
    private MatrixStack matrix;
    private final Hand hand;
    private final HandSide handSide;
    private float swingProgress;
    private float equipProgress;
    private final ItemStack mainHandStack;
    private final ItemStack offHandStack;
}
