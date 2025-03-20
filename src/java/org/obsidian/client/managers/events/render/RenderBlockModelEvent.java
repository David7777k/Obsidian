package org.obsidian.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.events.CancellableEvent;

@Getter
@Setter
@AllArgsConstructor
public class RenderBlockModelEvent extends CancellableEvent {
    private MatrixStack matrix;
    private BlockState blockState;
    private BlockPos blockPos;
}
