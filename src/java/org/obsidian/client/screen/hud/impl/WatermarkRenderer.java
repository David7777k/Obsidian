package org.obsidian.client.screen.hud.impl;

import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Namespaced;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import net.optifine.render.Blender;
import org.joml.Vector2f;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.client.Constants;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.managers.module.impl.render.Hud;
import org.obsidian.client.screen.hud.IRenderer;
import org.obsidian.client.utils.render.color.ColorFormatting;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.font.Fonts;
import org.obsidian.client.utils.render.gif.GifRender;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
public class WatermarkRenderer implements IRenderer {
    private final Vector2f position = new Vector2f(5, 5);
    private final Vector2f size = new Vector2f();
    private final GifRender gifRender = new GifRender(new Namespaced("texture/duck.gif"));

    @Override
    public void render(Render2DEvent event) {
        MatrixStack matrix = event.getMatrix();
        String namespace = Obsidian.devMode() ? "" : Constants.RELEASE;
        String textAccentColor = ColorFormatting.getColor(theme().textAccentColor());

        StringBuilder sb = new StringBuilder(Constants.NAME + " " + textAccentColor + namespace);

        Hud hud = Hud.getInstance();

        if (hud.watermarkChecks().getValue("Time")) {
            LocalTime currentTime = LocalTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            String formattedTime = currentTime.format(formatter);
            sb.append(ColorFormatting.getColor(128, 128, 128, 64)).append(" | ").append(textAccentColor).append("Time: ").append(formattedTime);
        }
        if (hud.watermarkChecks().getValue("Fps")) {
            sb.append(ColorFormatting.getColor(128, 128, 128, 64)).append(" | ").append(textAccentColor).append("Fps: ").append(Minecraft.getDebugFPS());
        }

        float margin = 4;

        float size = 16F;
        float gifSpeed = 100;
        float gifAlpha = 1F;

        this.size.x = margin + size + margin + font.getWidth(sb.toString(), fontSize);
        this.size.y = size;

        theme().drawClientRect(matrix, position.x, position.y, this.size.x, this.size.y);

        if (Hud.getInstance().duck().getValue()) {
            RenderSystem.pushMatrix();
            Blender.setupBlend(0, 1F);
            gifRender.draw(matrix, position.x, position.y, size, size, gifAlpha, gifSpeed, false);
            Blender.setupBlend(6, 1F);
            gifRender.draw(matrix, position.x, position.y, size, size, gifAlpha, gifSpeed, false);
            RenderSystem.popMatrix();
        } else {
            Fonts.CLICKGUI.draw(matrix, "g", position.x + (size / 2F - 5) + 1F, position.y + (size / 2F - 5) + 1F, theme().iconColor(), 10);
        }

        RectUtil.drawRect(matrix, position.x + size, position.y + (this.size.y / 2F) - (this.size.y / 3F) / 2F, 1F, (this.size.y / 3F), ColorUtil.getColor(255, 64));

        font.draw(matrix, sb.toString(), position.x + margin + size, position.y + (this.size.y / 2F) - (fontSize / 2F), theme().textColor(), fontSize);
    }

}


 