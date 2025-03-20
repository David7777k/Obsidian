package org.obsidian.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.vector.Matrix4f;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.events.Event;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public final class Render3DPosedEvent extends Event {
    @Getter
    public static Render3DPosedEvent instance = new Render3DPosedEvent();
    private MatrixStack matrix;
    private Matrix4f projectionMatrix;
    private ActiveRenderInfo activeRenderInfo;
    private WorldRenderer context;
    private float partialTicks;
    private long finishTimeNano;
    private double x, y, z;

    public void set(MatrixStack matrix, Matrix4f projectionMatrix, ActiveRenderInfo activeRenderInfo, WorldRenderer context, float partialTicks, long finishTimeNano, double x, double y, double z) {
        this.matrix = matrix;
        this.projectionMatrix = projectionMatrix;
        this.activeRenderInfo = activeRenderInfo;
        this.context = context;
        this.partialTicks = partialTicks;
        this.finishTimeNano = finishTimeNano;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}