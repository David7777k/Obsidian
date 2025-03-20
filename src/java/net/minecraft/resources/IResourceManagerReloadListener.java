package net.minecraft.resources;

import net.minecraft.util.Unit;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public interface IResourceManagerReloadListener extends IFutureReloadListener {
    default CompletableFuture<Void> reload(IFutureReloadListener.IStage stage, IResourceManager resourceManager, Executor backgroundExecutor, Executor gameExecutor) {
        return stage.markCompleteAwaitingOthers(Unit.INSTANCE).thenRunAsync(() ->
                this.onResourceManagerReload(resourceManager), gameExecutor);
    }

    void onResourceManagerReload(IResourceManager resourceManager);
}
