package org.obsidian.client.utils.render.font;


import lombok.Getter;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.api.interfaces.IRender;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.utils.render.color.ColorFormatting;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.shader.ShaderManager;
import org.obsidian.client.utils.render.text.TextUtils;
import org.obsidian.client.utils.tuples.Triplet;

import java.util.List;

import static net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX_COLOR;

@Getter
public class Font implements IMinecraft, IRender {
    private final MsdfFont font;

    public Font(String name) {
        font = create(name);
    }

    public void drawTextComponent(MatrixStack matrix, ITextComponent textComponent, float x, float y, int color, boolean shadow, float size) {
        StringBuilder sb = new StringBuilder();
        for (ITextComponent component : textComponent.getSiblings()) {
            if (!component.getSiblings().isEmpty()) {
                for (ITextComponent charComponent : component.getSiblings()) {
                    sb.append(ColorFormatting.getColor(charComponent.getStyle().getColor() != null ? ColorUtil.replAlpha(charComponent.getStyle().getColor().getColor(), ColorUtil.alphaf(color)) : color));
                    sb.append(replaceSymbols(charComponent.getString()));
                }
            } else {
                sb.append(ColorFormatting.getColor(component.getStyle().getColor() != null ? ColorUtil.replAlpha(component.getStyle().getColor().getColor(), ColorUtil.alphaf(color)) : color));
                sb.append(replaceSymbols(component.getString()));
            }
        }
        draw(matrix, sb.toString(), x, y, color, size);
    }

    public float getWidth(ITextComponent textComponent, float size) {
        StringBuilder sb = new StringBuilder();
        for (ITextComponent component : textComponent.getSiblings()) {
            if (!component.getSiblings().isEmpty()) {
                for (ITextComponent charComponent : component.getSiblings()) {
                    sb.append(ColorFormatting.getColor(-1));
                    sb.append(replaceSymbols(charComponent.getString()));
                }
            } else {
                sb.append(ColorFormatting.getColor(-1));
                sb.append(replaceSymbols(component.getString()));
            }
        }
        return getWidth(sb.toString(), size);
    }

    private String replaceSymbols(String string) {
        return string
                .replaceAll("⚡", "")
                .replaceAll("ᴀ", "a")
                .replaceAll("ʙ", "b")
                .replaceAll("ᴄ", "c")
                .replaceAll("ᴅ", "d")
                .replaceAll("ᴇ", "e")
                .replaceAll("ғ", "f")
                .replaceAll("ɢ", "g")
                .replaceAll("ʜ", "h")
                .replaceAll("ɪ", "i")
                .replaceAll("ᴊ", "j")
                .replaceAll("ᴋ", "k")
                .replaceAll("ʟ", "l")
                .replaceAll("ᴍ", "m")
                .replaceAll("ɴ", "n")
                .replaceAll("ᴏ", "o")
                .replaceAll("ᴘ", "p")
                .replaceAll("ǫ", "q")
                .replaceAll("ʀ", "r")
                .replaceAll("s", "s")
                .replaceAll("ᴛ", "t")
                .replaceAll("ᴜ", "u")
                .replaceAll("ᴠ", "v")
                .replaceAll("ᴡ", "w")
                .replaceAll("x", "x")
                .replaceAll("ʏ", "y")
                .replaceAll("ᴢ", "z");
    }

    private MsdfFont create(String name) {
        final MsdfFont font;
        font = MsdfFont.builder().withAtlas(name + ".png").withData(name + ".json").build();
        return font;
    }

    public void drawWithSpace(MatrixStack matrix, String text, float x, float y, int color, float size, float space) {
        draw(matrix, text, x, y, color, size, false, 0F, -1, space);
    }

    public void drawGradient(MatrixStack matrix, String text, float x, float y, int color1, int color2, float size) {
        int speed = Theme.getInstance().getSpeed();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            sb.append(ColorFormatting.getColor(ColorUtil.fade(speed, i * speed, color1, color2))).append(ch);
        }
        draw(matrix, sb.toString(), x, y, -1, size);
    }

    public void drawGradientCenter(MatrixStack matrix, String text, float x, float y, int color1, int color2, float size) {
        drawGradient(matrix, text, x - (getWidth(text, size) / 2F), y, color1, color2, size);
    }

    public void drawGradientRight(MatrixStack matrix, String text, float x, float y, int color1, int color2, float size) {
        drawGradient(matrix, text, x - getWidth(text, size), y, color1, color2, size);
    }

    public float drawSplitted(MatrixStack matrix, String text, String splitter, float x, float y, float width, int color, float size) {
        return splitted(matrix, text, splitter, width, size, (m, s, o) -> draw(m, s, x, y + o, color, size), true);
    }

    public float drawSplittedCenter(MatrixStack matrix, String text, String splitter, float x, float y, float width, int color, float size) {
        return splitted(matrix, text, splitter, width, size, (m, s, o) -> drawCenter(m, s, x + (width / 2F), y + o, color, size), true);
    }

    public float drawSplittedRight(MatrixStack matrix, String text, String splitter, float x, float y, float width, int color, float size) {
        return splitted(matrix, text, splitter, width, size, (m, s, o) -> drawRight(m, s, x + width, y + o, color, size), true);
    }

    public float splitted(MatrixStack matrix, String text, String splitter, float width, float size, Triplet.TriConsumer<MatrixStack, String, Float> drawFunction, boolean draw) {
        List<String> strings = TextUtils.splitLine(text, this, size, width, splitter);
        float offset = 0.0F;
        for (String str : strings) {
            if (draw) drawFunction.accept(matrix, str, offset);
            offset += size;
        }
        return offset;
    }

    public void draw(MatrixStack matrix, String text, float x, float y, int color, float size) {
        draw(matrix, text, x, y, color, size, false, 0F, -1);
    }

    public void drawShadow(MatrixStack matrix, String text, float x, float y, int color, float size) {
        draw(matrix, text, x + 0.25F, y + 0.25F, ColorUtil.multDark(color, 0.25F), size, false, 0F, -1);
        draw(matrix, text, x, y, color, size, false, 0F, -1);
    }

    public void drawOutline(MatrixStack matrix, String text, float x, float y, int color, float size) {
        draw(matrix, text, x, y, color, size, true, 0.25F, ColorUtil.multDark(ColorUtil.multAlpha(color, 0.5F), 0.25F));
    }

    public void drawRight(MatrixStack matrix, String text, float x, float y, int color, float size) {
        draw(matrix, text, x - (getWidth(text, size)), y, color, size);
    }

    public void drawRightShadow(MatrixStack matrix, String text, float x, float y, int color, float size) {
        drawShadow(matrix, text, x - (getWidth(text, size)), y, color, size);
    }

    public void drawRightOutline(MatrixStack matrix, String text, float x, float y, int color, float size) {
        drawOutline(matrix, text, x - (getWidth(text, size)), y, color, size);
    }

    public void drawCenter(MatrixStack matrix, String text, float x, float y, int color, float size) {
        draw(matrix, text, x - (getWidth(text, size) / 2F), y, color, size);
    }

    public void drawCenterShadow(MatrixStack matrix, String text, float x, float y, int color, float size) {
        drawShadow(matrix, text, x - (getWidth(text, size) / 2F), y, color, size);
    }

    public void drawCenterOutline(MatrixStack matrix, String text, float x, float y, int color, float size) {
        drawOutline(matrix, text, x - (getWidth(text, size) / 2F), y, color, size);
    }

    public void draw(MatrixStack matrix, String text, float x, float y, int color, float size, boolean outline, float outlineThickness, int outlineColor) {
        draw(matrix, text, x, y, color, size, outline, outlineThickness, outlineColor, 0);
    }

    public void draw(MatrixStack matrix, String text, float x, float y, int color, float size, boolean outline, float outlineThickness, int outlineColor, float space) {

        matrix.push();
        ShaderManager shader = ShaderManager.fontShader;
        FontData.AtlasData atlas = this.font.getAtlas();
        shader.load();
        shader.setUniformi("image", 0)
                .setUniformf("textureSize", atlas.width(), atlas.height())
                .setUniformf("range", atlas.range())
                .setUniformf("edgeStrength", -0.5F, 0.5F)
                .setUniformf("thickness", 0.0F)
                .setUniformi("outline", (outline ? 1 : 0))
                .setUniformf("outlineThickness", outlineThickness)
                .setUniformf("outlineColor", ColorUtil.getRGBAf(outlineColor));

        this.font.bind();
        IRender.BUFFER.begin(GL11.GL_QUADS, POSITION_TEX_COLOR);
        this.font.applyGlyphs(matrix, BUFFER, size, TextFormatting.removeFormatting(text), 0, x, y + font.getMetrics().baselineHeight() * size, 0, space, color);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        IRender.TESSELLATOR.draw();
        RenderSystem.disableBlend();
        this.font.unbind();
        shader.unload();
        matrix.pop();
    }

    public float getWidth(String text, float size) {
        return font.getWidth(text, size);
    }


    public float getWidth(String text, float size, float thickness) {
        return font.getWidth(text, size, thickness);
    }
}
