package org.obsidian.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.client.MainWindow;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class Render2DEvent extends Event {
    @Getter
    private static final Render2DEvent instance = new Render2DEvent();
    private MatrixStack matrix;
    private ActiveRenderInfo activeRender;
    private MainWindow mainWindow;
    private float partialTicks;

    public void set(MatrixStack matrix, ActiveRenderInfo activeRender, MainWindow mainWindow, float partialTicks) {
        this.matrix = matrix;
        this.activeRender = activeRender;
        this.mainWindow = mainWindow;
        this.partialTicks = partialTicks;
    }
}