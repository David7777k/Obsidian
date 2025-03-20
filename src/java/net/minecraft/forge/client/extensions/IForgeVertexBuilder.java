package net.minecraft.forge.client.extensions;

import net.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.model.BakedQuad;

public interface IForgeVertexBuilder
{
default void addVertexData(MatrixStack.Entry matrixStack, BakedQuad bakedQuad, float red, float green, float blue, int lightmapCoord, int overlayColor, boolean readExistingColor)
    {
    }
}
