package org.obsidian.client.managers.events.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.obsidian.client.api.events.Event;

@Getter
@Setter
@AllArgsConstructor
public class ChunkPositionEvent extends Event {
    private final int x, y, z;
    private final ChunkRenderDispatcher.ChunkRender chunkRender;
}
