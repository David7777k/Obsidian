package net.minecraft.resources;

import com.google.common.base.Stopwatch;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class DebugAsyncReloader extends AsyncReloader<DebugAsyncReloader.DataPoint> {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Stopwatch timer = Stopwatch.createUnstarted();

    public DebugAsyncReloader(IResourceManager p_i50694_1_, List<IFutureReloadListener> listeners, Executor backgroundExecutor, Executor gameExecutor, CompletableFuture<Unit> alsoWaitedFor) {
        super(backgroundExecutor, gameExecutor, p_i50694_1_, listeners, (p_219578_1_, p_219578_2_, p_219578_3_, p_219578_4_, p_219578_5_) ->
        {
            AtomicLong atomiclong = new AtomicLong();
            AtomicLong atomiclong1 = new AtomicLong();

            CompletableFuture<Void> completablefuture = p_219578_3_.reload(p_219578_1_, p_219578_2_, (p_219577_2_) -> p_219578_4_.execute(() -> {
                long i = Util.nanoTime();
                p_219577_2_.run();
                atomiclong.addAndGet(Util.nanoTime() - i);
            }), (p_219574_2_) -> p_219578_5_.execute(() -> {
                long i = Util.nanoTime();
                p_219574_2_.run();
                atomiclong1.addAndGet(Util.nanoTime() - i);
            }));
            return completablefuture.thenApplyAsync((p_219576_5_) -> new DataPoint(p_219578_3_.getSimpleName(), atomiclong, atomiclong1), gameExecutor);
        }, alsoWaitedFor);
        this.timer.start();
        this.resultListFuture.thenAcceptAsync(this::logStatistics, gameExecutor);
    }

    private void logStatistics(List<DebugAsyncReloader.DataPoint> datapoints) {
        this.timer.stop();
        int i = 0;
        LOGGER.info("Resource reload finished after " + this.timer.elapsed(TimeUnit.MILLISECONDS) + " ms");

        for (DebugAsyncReloader.DataPoint debugasyncreloader$datapoint : datapoints) {
            int j = (int) ((double) debugasyncreloader$datapoint.prepareDuration.get() / 1000000.0D);
            int k = (int) ((double) debugasyncreloader$datapoint.applyDuration.get() / 1000000.0D);
            int l = j + k;
            String s = debugasyncreloader$datapoint.className;
            LOGGER.info(s + " took approximately " + l + " ms (" + j + " ms preparing, " + k + " ms applying)");
            i += k;
        }

        LOGGER.info("Total blocking time: " + i + " ms");
    }

    public static class DataPoint {
        private final String className;
        private final AtomicLong prepareDuration;
        private final AtomicLong applyDuration;

        private DataPoint(String p_i50542_1_, AtomicLong prepareTime, AtomicLong applyTime) {
            this.className = p_i50542_1_;
            this.prepareDuration = prepareTime;
            this.applyDuration = applyTime;
        }
    }
}
