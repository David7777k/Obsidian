package org.obsidian.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.math.vector.Matrix4f;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.events.Event;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RenderWorldEvent extends Event {
    @Getter
    public static RenderWorldEvent instance = new RenderWorldEvent();
    private MatrixStack matrix;
    private float partialTicks;
    private long finishTimeNano;
    private boolean drawBlockOutline;
    private ActiveRenderInfo activerenderinfo;
    private GameRenderer gameRenderer;
    private LightTexture lightmapTexture;
    private Matrix4f matrix4f;

    public void set(MatrixStack matrixStackIn, float partialTicks, long finishTimeNano, boolean drawBlockOutline, ActiveRenderInfo activerenderinfo, GameRenderer gameRenderer, LightTexture lightmapTexture, Matrix4f matrix4f) {
        this.matrix = matrixStackIn;
        this.partialTicks = partialTicks;
        this.finishTimeNano = finishTimeNano;
        this.drawBlockOutline = drawBlockOutline;
        this.activerenderinfo = activerenderinfo;
        this.gameRenderer = gameRenderer;
        this.lightmapTexture = lightmapTexture;
        this.matrix4f = matrix4f;
    }
}