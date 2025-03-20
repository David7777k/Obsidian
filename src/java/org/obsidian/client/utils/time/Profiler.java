package org.obsidian.client.utils.time;

import lombok.Getter;


public class Profiler {
    @Getter
    private long totalTimeMS = 0;
    private long lastTimeMS = 0;

    public Profiler() {
        reset();
    }

    public void start() {
        lastTimeMS = System.currentTimeMillis();
    }

    public long stop() {
        if (lastTimeMS == 0) {
            System.err.println("Profiler stop called without prior start.");
            return -1;
        }
        long elapsedTime = System.currentTimeMillis() - lastTimeMS;
        totalTimeMS += elapsedTime;
        lastTimeMS = 0;
        return elapsedTime;
    }

    public long getTotalTimeS() {
        return totalTimeMS / 1000;
    }

    public void reset() {
        totalTimeMS = 0;
    }
}
