package org.obsidian.client.managers.module.impl.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.obsidian.client.utils.render.color.ColorUtil;

@Getter
@RequiredArgsConstructor
public enum ThemeType {
    DARK(
            ColorUtil.getColor(90, 90, 255),
            ColorUtil.getColor(20, 20, 30),
            ColorUtil.getColor(33, 33, 43),
            ColorUtil.getColor(150, 100, 255),
            ColorUtil.getColor(120, 60, 255),
            ColorUtil.getColor(70, 70, 200),
            ColorUtil.getColor(50, 50, 150)
    ),
    CRIMSON(
            ColorUtil.getColor(200, 40, 60),
            ColorUtil.getColor(20, 10, 15),
            ColorUtil.getColor(40, 15, 20),
            ColorUtil.getColor(255, 80, 90),
            ColorUtil.getColor(230, 70, 80),
            ColorUtil.getColor(150, 30, 40),
            ColorUtil.getColor(90, 20, 30)
    ),
    AQUA(
            ColorUtil.getColor(60, 180, 200),
            ColorUtil.getColor(15, 25, 30),
            ColorUtil.getColor(25, 40, 45),
            ColorUtil.getColor(120, 200, 220),
            ColorUtil.getColor(100, 180, 200),
            ColorUtil.getColor(50, 130, 150),
            ColorUtil.getColor(30, 90, 110)
    ),
    LIGHT(
            ColorUtil.getColor(180, 180, 200),
            ColorUtil.getColor(235, 235, 245),
            ColorUtil.getColor(200, 200, 210),
            ColorUtil.getColor(50, 50, 60),
            ColorUtil.getColor(80, 80, 100),
            ColorUtil.getColor(150, 150, 170),
            ColorUtil.getColor(120, 120, 140)
    );

    private final int clientColor;
    private final int backgroundColor;
    private final int shadowColor;
    private final int textColor;
    private final int iconColor;
    private final int secondaryColor;
    private final int tertiaryColor;
}
