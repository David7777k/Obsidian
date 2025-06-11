package org.obsidian.client.managers.module.impl.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.obsidian.client.utils.render.color.ColorUtil;

@Getter
@RequiredArgsConstructor
public enum ThemeType {
    DARK(
            ColorUtil.getColor(130, 90, 255),
            ColorUtil.getColor(20, 20, 30),
            ColorUtil.getColor(255, 255, 255)
    ),
    MAGENTA(
            ColorUtil.getColor(200, 40, 160),
            ColorUtil.getColor(20, 10, 20),
            ColorUtil.getColor(255, 210, 255)
    ),
    AQUA(
            ColorUtil.getColor(60, 180, 200),
            ColorUtil.getColor(15, 25, 30),
            ColorUtil.getColor(230, 255, 255)
    ),
    LIGHT(
            ColorUtil.getColor(120, 120, 160),
            ColorUtil.getColor(235, 235, 245),
            ColorUtil.getColor(50, 50, 60)
    );

    private final int accentColor;
    private final int backgroundColor;
    private final int textColor;
}
