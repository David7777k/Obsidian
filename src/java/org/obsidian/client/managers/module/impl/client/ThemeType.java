package org.obsidian.client.managers.module.impl.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.obsidian.client.utils.render.color.ColorUtil;

@Getter
@RequiredArgsConstructor
public enum ThemeType {
    FLAT(
            ColorUtil.getColor(224, 224, 224),
            ColorUtil.getColor(32, 32, 32),
            ColorUtil.getColor(224, 224, 224)
    ),
    MODERN(
            ColorUtil.getColor(79, 148, 205),
            ColorUtil.getColor(18, 18, 18),
            ColorUtil.getColor(236, 236, 236)
    );

    private final int accentColor;
    private final int backgroundColor;
    private final int textColor;
}
