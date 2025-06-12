package org.obsidian.client.utils.render.font;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

@Getter
@RequiredArgsConstructor
public class Fonts {
    public static Font
            SF_BOLD,
            SF_LIGHT,
            SF_MEDIUM,
            SF_REGULAR,
            SF_SEMIBOLD;

    public static Font CLICKGUI, HUD, MUSIC_ICONS, ACCOUNT, montserrat, icons;

    public static void loadFonts() {
        SF_BOLD = new Font("sf_bold");
        SF_LIGHT = new Font("sf_light");
        SF_MEDIUM = new Font("sf_medium");
        SF_REGULAR = new Font("sf_regular");
        SF_SEMIBOLD = new Font("sf_semibold");

        CLICKGUI = new Font("clickgui");
        HUD = new Font("hud");
        MUSIC_ICONS = new Font("music_icons");
        ACCOUNT = new Font("account");
        montserrat = new Font("sf_regular");
        icons = new Font("music_icons");
    }

    public static Font valueOf(String name) {
        try {
            Field field = Fonts.class.getDeclaredField(name.toUpperCase());
            return (Font) field.get(null);
        } catch (NoSuchFieldException | IllegalAccessException exception) {
            throw new IllegalArgumentException("Font not found: " + name, exception);
        }
    }
}
