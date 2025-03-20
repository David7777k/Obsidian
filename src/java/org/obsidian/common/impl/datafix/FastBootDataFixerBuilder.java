package org.obsidian.common.impl.datafix;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;

import java.util.concurrent.Executor;

public class FastBootDataFixerBuilder extends DataFixerBuilder {
    private static final Executor NO_OP_EXECUTOR = (command) -> {
    };

    public FastBootDataFixerBuilder(int dataVersion) {
        super(dataVersion);
    }

    public DataFixer build(Executor executor) {
        return super.build(NO_OP_EXECUTOR);
    }
}