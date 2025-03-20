package org.obsidian.client.managers.other.theme;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.obsidian.client.utils.render.color.ColorUtil;

@Getter
@AllArgsConstructor
public enum ClientColor {
    TEXT(ColorUtil.getColor(255, 200)),
    RECT(ColorUtil.getColor(200, 128));
    private final int color;

    public int getColor(int alpha) {
        return ColorUtil.replAlpha(getColor(), alpha);
    }
}
