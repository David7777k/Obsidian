package net.optifine;

import lombok.Getter;

@Getter
public class ChunkSectionDataOF {
    private final short blockRefCount;
    private final short tickRefCount;
    private final short fluidRefCount;

    public ChunkSectionDataOF(short blockRefCount, short tickRefCount, short fluidRefCount) {
        this.blockRefCount = blockRefCount;
        this.tickRefCount = tickRefCount;
        this.fluidRefCount = fluidRefCount;
    }

}
