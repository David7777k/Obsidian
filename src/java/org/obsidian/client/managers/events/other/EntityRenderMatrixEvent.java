package org.obsidian.client.managers.events.other;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.events.Event;

@Getter
@AllArgsConstructor
public final class EntityRenderMatrixEvent extends Event {
    private final MatrixStack matrix;
    private final Entity entity;
}