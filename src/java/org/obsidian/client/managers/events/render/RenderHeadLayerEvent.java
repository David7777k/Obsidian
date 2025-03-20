package org.obsidian.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.events.Event;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RenderHeadLayerEvent extends Event {
    @Getter
    public static RenderHeadLayerEvent instance = new RenderHeadLayerEvent();
    private LivingEntity entity;
    private MatrixStack matrix;
    private EntityModel<?> model;

    public void set(LivingEntity entity, MatrixStack matrix, EntityModel<?> model) {
        this.entity = entity;
        this.matrix = matrix;
        this.model = model;
    }
}
