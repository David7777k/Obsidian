package org.obsidian.client.api.interfaces;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;

public interface IRender {
    Tessellator TESSELLATOR = Tessellator.getInstance();
    BufferBuilder BUFFER = TESSELLATOR.getBuffer();
}