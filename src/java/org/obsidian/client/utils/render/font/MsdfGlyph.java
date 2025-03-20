package org.obsidian.client.utils.render.font;

import net.minecraft.util.math.vector.Matrix4f;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.vertex.IVertexBuilder;

public final class MsdfGlyph {

    private final int code;
    private final float minU, maxU, minV, maxV;
    private final float advance, topPosition, width, height;

    public MsdfGlyph(FontData.GlyphData data, float atlasWidth, float atlasHeight) {
        this.code = data.unicode();
        this.advance = data.advance();

        FontData.BoundsData atlasBounds = data.atlasBounds();
        if (atlasBounds != null) {
            this.minU = atlasBounds.left() / atlasWidth;
            this.maxU = atlasBounds.right() / atlasWidth;
            this.minV = 1.0F - atlasBounds.top() / atlasHeight;
            this.maxV = 1.0F - atlasBounds.bottom() / atlasHeight;
        } else {
            this.minU = 0.0f;
            this.maxU = 0.0f;
            this.minV = 0.0f;
            this.maxV = 0.0f;
        }

        FontData.BoundsData planeBounds = data.planeBounds();
        if (planeBounds != null) {
            this.width = planeBounds.right() - planeBounds.left();
            this.height = planeBounds.top() - planeBounds.bottom();
            this.topPosition = planeBounds.top();
        } else {
            this.width = 0.0f;
            this.height = 0.0f;
            this.topPosition = 0.0f;
        }
    }

    public float apply(MatrixStack matrix, IVertexBuilder processor, float size, float x, float y, float z, float space, int color) {
        y -= this.topPosition * size;
        y -= 0.125f * size;
        x -= 0.5f;
        float width = this.width * size;
        float height = this.height * size;

        Matrix4f matrix4f = matrix.getLast().getMatrix();

        processor.pos(matrix4f, x, y, z).tex(this.minU, this.minV).color(color).endVertex();
        processor.pos(matrix4f, x, y + height, z).tex(this.minU, this.maxV).color(color).endVertex();
        processor.pos(matrix4f, x + width, y + height, z).tex(this.maxU, this.maxV).color(color).endVertex();
        processor.pos(matrix4f, x + width, y, z).tex(this.maxU, this.minV).color(color).endVertex();

        return this.width * (size - 1) + (Character.isSpaceChar(code) ? this.advance * size : 0) + space;
    }

    public float apply(MatrixStack matrix, IVertexBuilder processor, float size, float x, float y, float z, int color) {
        return this.apply(matrix, processor, size, x, y, z, 0, color);
    }

    public float getWidth(float size) {
        return this.width * (size - 1) + (Character.isSpaceChar(code) ? this.advance * size : 0);
    }

    public int getCharCode() {
        return code;
    }

}