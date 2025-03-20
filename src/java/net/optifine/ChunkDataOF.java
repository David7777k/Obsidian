package net.optifine;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChunkDataOF {
    private ChunkSectionDataOF[] chunkSectionDatas;

    public ChunkDataOF(ChunkSectionDataOF[] chunkSectionDatas) {
        this.chunkSectionDatas = chunkSectionDatas;
    }

}
