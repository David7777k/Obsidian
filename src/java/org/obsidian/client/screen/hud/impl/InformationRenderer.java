package org.obsidian.client.screen.hud.impl;

import net.minecraft.client.gui.AbstractGui;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.interfaces.IWindow;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.screen.hud.IRenderer;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.player.MoveUtil;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.client.utils.render.color.ColorFormatting;
import org.obsidian.client.utils.render.color.ColorUtil;

import java.util.ArrayList;
import java.util.List;

public class InformationRenderer implements IRenderer, IWindow {
    @Override
    public void render(Render2DEvent event) {
        MatrixStack matrix = event.getMatrix();

        // Подготовка информации для левой части
        List<String> leftInfo = new ArrayList<>();
        int textColor = theme().textColor();
        int textAccentColor = theme().textAccentColor();

        leftInfo.add(ColorFormatting.getColor(textColor) + "Pos: " + ColorFormatting.getColor(textAccentColor)
                + Mathf.round(mc.player.getPosX(), 1) + ", " + Mathf.round(mc.player.getPosY(), 1) + ", " + Mathf.round(mc.player.getPosZ(), 1)
                + ColorFormatting.getColor(ColorUtil.overCol(ColorUtil.WHITE, ColorUtil.RED, 0.75F))
                + " (%s, %s)".formatted(Mathf.round(mc.player.getPosX() / 8F, 1), Mathf.round(mc.player.getPosZ() / 8F, 1)));
        leftInfo.add(ColorFormatting.getColor(textColor) + "Bps: " + ColorFormatting.getColor(textAccentColor)
                + Mathf.round(MoveUtil.speedSqrt() * 20.0F, 1));

        leftInfo.sort((s1, s2) -> Float.compare(-font.getWidth(s1, fontSize), -font.getWidth(s2, fontSize)));

        // Вычисляем максимальную ширину для левой информации
        float leftMaxWidth = 0;
        for (String s : leftInfo) {
            leftMaxWidth = Math.max(leftMaxWidth, font.getWidth(s, fontSize));
        }

        // Базовые координаты и отступы
        float baseY = height() - fontSize - 2.5F;
        float leftX = 2.5F;
        float padding = 2.5F;
        int bgColor = 0x80000000; // полупрозрачный чёрный цвет

        // Определяем координаты фона для левой информации
        float leftTopY = baseY - (leftInfo.size() - 1) * fontSize - padding;
        float leftBottomY = baseY + fontSize + padding;
        float leftBgX = leftX - padding;
        float leftBgWidth = leftMaxWidth + 2 * padding;

        // Отрисовка фона для левой информации
        AbstractGui.fill(matrix, (int) leftBgX, (int) leftTopY, (int) (leftBgX + leftBgWidth), (int) leftBottomY, bgColor);

        // Отрисовка левой информации
        float y = baseY;
        float leftOffset = 0;
        for (String s : leftInfo) {
            font.drawOutline(matrix, s, leftX, y + leftOffset, -1, fontSize);
            leftOffset -= fontSize;
        }

        // Подготовка информации для правой части
        List<String> rightInfo = new ArrayList<>();
        rightInfo.add(ColorFormatting.getColor(textColor) + "Tps: " + ColorFormatting.getColor(textAccentColor)
                + Mathf.round(Obsidian.inst().serverTps().getTPS(), 1));
        rightInfo.add(ColorFormatting.getColor(textColor) + "Ping: " + ColorFormatting.getColor(textAccentColor)
                + PlayerUtil.getPing(mc.player));

        rightInfo.sort((s1, s2) -> Float.compare(-font.getWidth(s1, fontSize), -font.getWidth(s2, fontSize)));

        // Вычисляем максимальную ширину для правой информации
        float rightMaxWidth = 0;
        for (String s : rightInfo) {
            rightMaxWidth = Math.max(rightMaxWidth, font.getWidth(s, fontSize));
        }

        float rightX = width() - 2.5F;

        // Определяем координаты фона для правой информации
        float rightTopY = baseY - (rightInfo.size() - 1) * fontSize - padding;
        float rightBottomY = baseY + fontSize + padding;
        float rightBgX = rightX - rightMaxWidth - padding;
        float rightBgWidth = rightMaxWidth + 2 * padding;

        // Отрисовка фона для правой информации
        AbstractGui.fill(matrix, (int) rightBgX, (int) rightTopY, (int) (rightBgX + rightBgWidth), (int) rightBottomY, bgColor);

        // Отрисовка правой информации
        float rightOffset = 0;
        for (String s : rightInfo) {
            font.drawRightOutline(matrix, s, rightX, baseY + rightOffset, -1, fontSize);
            rightOffset -= fontSize;
        }
    }
}
