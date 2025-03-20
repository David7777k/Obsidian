package org.obsidian.client.screen.hud;

import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.events.render.RenderScoreBoardEvent;

public interface IScoreBoardRenerer extends IMinecraft {
    void renderScoreBoard(RenderScoreBoardEvent event);
}
