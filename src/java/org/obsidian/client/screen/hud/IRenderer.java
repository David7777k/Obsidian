package org.obsidian.client.screen.hud;


import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.utils.render.font.Font;
import org.obsidian.client.utils.render.font.Fonts;

public interface IRenderer extends IMinecraft {
    Font font = Fonts.SF_SEMIBOLD;
    float fontSize = 7;

    void render(Render2DEvent event);

    default Theme theme() {
        return Theme.getInstance();
    }

}
