package org.obsidian.client.api.interfaces;

import net.minecraft.client.Minecraft;
import org.obsidian.client.utils.math.ScaleMath;
import org.joml.Vector2f;

public interface IWindow {
    default Vector2f scaled() {
        return ScaleMath.getMouse(Minecraft.getInstance().getMainWindow().getScaledWidth(), Minecraft.getInstance().getMainWindow().getScaledHeight());
    }

    default float width() {
        return ScaleMath.getScaled(Minecraft.getInstance().getMainWindow().getScaledWidth());
    }

    default float height() {
        return ScaleMath.getScaled(Minecraft.getInstance().getMainWindow().getScaledHeight());
    }
}
