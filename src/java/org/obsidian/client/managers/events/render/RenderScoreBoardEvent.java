package org.obsidian.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.scoreboard.ScoreObjective;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.events.CancellableEvent;

@Getter
@Setter
@AllArgsConstructor
public class RenderScoreBoardEvent extends CancellableEvent {
    private MatrixStack matrix;
    private ScoreObjective objective;
    private FontRenderer fontRenderer;
    private int scaledWidth;
    private int scaledHeight;
}
