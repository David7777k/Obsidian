package net.minecraft.util.concurrent;

public abstract class RecursiveEventLoop<R extends Runnable> extends ThreadTaskExecutor<R> {
    private int runningTasks;

    public RecursiveEventLoop(String name) {
        super(name);
    }

    @Override
    protected boolean shouldDeferTasks() {
        return isTaskRunning() || super.shouldDeferTasks();
    }

    protected boolean isTaskRunning() {
        return runningTasks != 0;
    }

    @Override
    protected void run(R taskIn) {
        runningTasks++;

        try {
            super.run(taskIn);
        } finally {
            runningTasks--;
        }
    }
}

