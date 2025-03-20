package org.obsidian.client.managers.events.other;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public class ContainerRenderEvent extends Event {
    private final MatrixStack matrix;
    private final ITextComponent title;
    private final Container container;
}
