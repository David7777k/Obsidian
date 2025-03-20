package net.minecraft.client.resources;

import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public abstract class ReloadListener<T> implements IFutureReloadListener {
    public final CompletableFuture<Void> reload(IFutureReloadListener.IStage stage, IResourceManager resourceManager, Executor backgroundExecutor, Executor gameExecutor) {
        return CompletableFuture.supplyAsync(() ->
                this.prepare(resourceManager), backgroundExecutor).thenCompose(stage::markCompleteAwaitingOthers).thenAcceptAsync((p_215269_3_) ->
        {
            this.apply(p_215269_3_, resourceManager);
        }, gameExecutor);
    }

    /**
     * Performs any reloading that can be done off-thread, such as file IO
     */
    protected abstract T prepare(IResourceManager resourceManagerIn);

    protected abstract void apply(T objectIn, IResourceManager resourceManagerIn);
}
