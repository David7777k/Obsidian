package org.obsidian.client.utils.render.font;


import lombok.Getter;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.util.Namespaced;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import net.mojang.blaze3d.vertex.IVertexBuilder;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.render.color.ColorFormatting;
import org.obsidian.client.utils.render.color.ColorUtil;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Getter
public final class MsdfFont {

    private final String name;
    private final Texture texture;
    private final FontData.AtlasData atlas;
    private final FontData.MetricsData metrics;
    private final Map<Integer, MsdfGlyph> glyphs;
    private boolean filtered = false;

    private MsdfFont(String name, Texture texture, FontData.AtlasData atlas, FontData.MetricsData metrics, Map<Integer, MsdfGlyph> glyphs) {
        this.name = name;
        this.texture = texture;
        this.atlas = atlas;
        this.metrics = metrics;
        this.glyphs = glyphs;
    }

    public void bind() {
        RenderSystem.bindTexture(this.texture.getGlTextureId());
        if (!this.filtered) {
            this.texture.setBlurMipmap(true, false);
            this.filtered = true;
        }
    }

    public void unbind() {
        RenderSystem.bindTexture(0);
    }

    public void applyGlyphs(MatrixStack matrix, IVertexBuilder processor, float size, String text, float thickness, float x, float y, float z, float space, int color) {
        int defaultRed = ColorUtil.red(color);
        int defaultGreen = ColorUtil.green(color);
        int defaultBlue = ColorUtil.blue(color);
        int defaultAlpha = ColorUtil.alpha(color);
        int currentRed = defaultRed;
        int currentGreen = defaultGreen;
        int currentBlue = defaultBlue;
        int currentAlpha = defaultAlpha;

        StringBuilder preText = new StringBuilder();
        Matcher matcher = ColorFormatting.PATTERN.matcher(text);
        int lastIndex = 0;

        while (matcher.find()) {
            preText.append(text, lastIndex, matcher.start());
            x = renderProcessedText(matrix, processor, size, preText.toString(), thickness, x, y, z, space, currentRed, currentGreen, currentBlue, currentAlpha);
            preText.setLength(0);

            if (matcher.group().equalsIgnoreCase(ColorFormatting.reset())) {
                currentRed = defaultRed;
                currentGreen = defaultGreen;
                currentBlue = defaultBlue;
                currentAlpha = defaultAlpha;
            } else {
                String type = matcher.group(1).toLowerCase();
                currentRed = Integer.parseInt(matcher.group(2));
                currentGreen = Integer.parseInt(matcher.group(3));
                currentBlue = Integer.parseInt(matcher.group(4));
                currentAlpha = (type.equals(ColorFormatting.typeRGBA()) && matcher.group(5) != null) ? Integer.parseInt(matcher.group(5)) : defaultAlpha;
            }

            lastIndex = matcher.end();
        }

        preText.append(text.substring(lastIndex));
        renderProcessedText(matrix, processor, size, preText.toString(), thickness, x, y, z, space, currentRed, currentGreen, currentBlue, currentAlpha);
    }

    private float renderProcessedText(MatrixStack matrix, IVertexBuilder processor, float size, String text, float thickness, float x, float y, float z, float space, int red, int green, int blue, int alpha) {
        String replaced = ColorFormatting.removeFormatting(TextFormatting.removeFormatting(text));
        for (char character : replaced.toCharArray()) {
            MsdfGlyph glyph = this.glyphs.get((int) character);
            if (glyph != null) {
                x += glyph.apply(matrix, processor, size, x, y, z, space, ColorUtil.getColor(Mathf.clamp(0, 255, red), Mathf.clamp(0, 255, green), Mathf.clamp(0, 255, blue), Mathf.clamp(0, 255, alpha))) + thickness;
            }
        }
        return x;
    }

    public void applyGlyphs(MatrixStack matrix, IVertexBuilder processor, float size, String text, float thickness, float x, float y, float z, int color) {
        this.applyGlyphs(matrix, processor, size, text, thickness, x, y, z, 0, color);
    }

    public float getWidth(String text, float size, float space) {
        String replaced = ColorFormatting.removeFormatting(TextFormatting.removeFormatting(text));
        float width = 0.0f;
        for (char character : replaced.toCharArray()) {
            MsdfGlyph glyph = this.glyphs.get((int) character);
            if (glyph != null) {
                width += glyph.getWidth(size) + space;
            }
        }
        return Math.max(width - space, 0);
    }

    public float getWidth(String text, float size) {
        return getWidth(text, size, 0);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        public static final String MSDF_PATH = "fonts/";
        private String name = "undefined";
        private ResourceLocation dataFile;
        private ResourceLocation atlasFile;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }


        public Builder withData(String dataFile) {
            this.dataFile = new Namespaced(MSDF_PATH + dataFile);
            return this;
        }


        public Builder withAtlas(String atlasFile) {
            this.atlasFile = new Namespaced(MSDF_PATH + atlasFile);
            return this;
        }


        public MsdfFont build() {
            FontData data = IOUtils.fromJsonToInstance(this.dataFile, FontData.class);
            Texture texture = IOUtils.toTexture(this.atlasFile);

            if (data == null)
                throw new RuntimeException("Failed to read font data file: " + this.dataFile.toString() + "; Are you sure this is json file? Try to check the correctness of its syntax.");

            float aWidth = data.atlas().width();
            float aHeight = data.atlas().height();
            Map<Integer, MsdfGlyph> glyphs = data.glyphs().stream()
                    .collect(Collectors.<FontData.GlyphData, Integer, MsdfGlyph>toMap(
                            FontData.GlyphData::unicode,
                            (glyphData) -> new MsdfGlyph(glyphData, aWidth, aHeight)
                    ));

            return new MsdfFont(this.name, texture, data.atlas(), data.metrics(), glyphs);
        }

    }

}