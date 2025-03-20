package org.obsidian.client.screen.hud.impl;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.StringUtils;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.managers.module.settings.impl.DragSetting;
import org.obsidian.client.screen.hud.AbstractHud;
import org.obsidian.client.screen.hud.IRenderer;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;
import org.obsidian.client.utils.render.font.Fonts;

import java.util.Collection;

public class PotionsRenderer extends AbstractHud implements IRenderer {
    private final DragSetting drag;

    public PotionsRenderer(DragSetting drag) {
        this.drag = drag;
    }

    @Override
    public void render(Render2DEvent event) {
        MatrixStack matrix = event.getMatrix();
        Collection<EffectInstance> activePotions = mc.player.getActivePotionEffects();
        for (EffectInstance effect : activePotions) {
            effect.getAnimation().update();
            effect.getAnimation().run((effect.getDuration() / 20F) <= 1F ? 0 : 1, 0.5F, Easings.EXPO_OUT, false);
        }

        String name = "Potions";

        boolean isEmpty = activePotions.isEmpty();

        float margin = 5F;

        float expaned = margin + fontSize + margin;
        drag.size.x = 100;

        float width = drag.size.x;
        float height = drag.size.y;

        float x = drag.position.x;
        float y = drag.position.y;
        boolean closeCondition = isEmpty && !(mc.currentScreen instanceof ChatScreen);
        update(closeCondition ? 0 : 1);

        drag.active = !closeCondition;

        if (closeCondition && animValue() == 0.0F) {
            return;
        }
        float scale = animValue();

        matrix.push();
        matrix.translate((x + width / 2F), (y + height / 2F), 0);
        matrix.scale(scale, scale, 1);
        matrix.translate(-(x + width / 2F), -(y + height / 2F), 0);

        theme().drawClientRect(matrix, x, y, width, drag.size.y, animValue());
        font.draw(matrix, name, x + margin, y + (expaned / 2F) - (fontSize / 2F), textColor(), fontSize);
        Fonts.HUD.drawRight(matrix, "b", x + width - margin, y + margin, iconColor(), 8);

        RectUtil.drawRect(matrix, x + 0.5F, y + expaned - 0.5F, width - 1F, 0.5, ColorUtil.multAlpha(textColor(), animValue() * 0.05F));
        RenderUtil.Rounded.smooth(matrix, x, y + expaned, width, height - expaned, 0, ColorUtil.getColor(0, animValue() * 0.25F), 0, ColorUtil.getColor(0, animValue() * 0.25F), Round.of(4, 0, 4, 0));

        float centerX = x + width / 2F;

        float offset = 0;
        for (EffectInstance potion : activePotions) {
            float animPC = potion.getAnimation().get();
            float centerY = y + expaned + margin + offset;
            matrix.push();
            matrix.translate(centerX, centerY, 0);
            matrix.scale(1, animPC, 0);
            matrix.translate(-centerX, -centerY, 0);
            boolean badpotion = potion.getPotion().equals(Effects.SLOWNESS) || potion.getPotion().equals(Effects.BLINDNESS) || potion.getPotion().equals(Effects.POISON) || potion.getPotion().equals(Effects.WITHER) || potion.getPotion().equals(Effects.HUNGER) || potion.getPotion().equals(Effects.NAUSEA) || potion.getPotion().equals(Effects.WEAKNESS);
            int color = ColorUtil.multAlpha(badpotion ? ColorUtil.RED : textAccentColor(), animPC);
            String keyName = getDuration(potion);
            potion.getStripFont().draw(font, matrix, potion.getPotion().getDisplayName().getString() + " " + (potion.getAmplifier() + 1), x + margin, y + expaned + margin + offset, width - (margin * 2) - font.getWidth(keyName, fontSize) - margin, color, fontSize, 1F);
            font.drawRight(matrix, keyName, x + width - margin, y + expaned + margin + offset, color, fontSize);
            matrix.pop();
            offset += fontSize * animPC;
        }

        drag.size.y = expaned + offset + (margin * 2F);

        matrix.pop();
    }

    public static String getDuration(EffectInstance potion) {
        if (potion.getIsPotionDurationMax()) {
            return "**:**";
        } else {
            return StringUtils.ticksToElapsedTime(potion.getDuration());
        }
    }
}