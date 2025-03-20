package org.obsidian.client.managers.events.render;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.entity.Entity;
import org.obsidian.client.api.events.CancellableEvent;

@Getter
@AllArgsConstructor
public final class RenderNameEvent extends CancellableEvent {
    private Entity entity;
}