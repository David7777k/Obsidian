package net.minecraft.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface IFutureReloadListener {
    CompletableFuture<Void> reload(IFutureReloadListener.IStage stage, IResourceManager resourceManager, Executor backgroundExecutor, Executor gameExecutor);

    default String getSimpleName() {
        return this.getClass().getSimpleName();
    }

    interface IStage {
        <T> CompletableFuture<T> markCompleteAwaitingOthers(T backgroundResult);
    }
}
