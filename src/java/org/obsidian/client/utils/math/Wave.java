package org.obsidian.client.utils.math;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Wave {
    public float sinWave(double value, double delayMS) {
        return (float) (Mathf.clamp01((Math.sin(System.currentTimeMillis() / delayMS) + 1F) / 2F) * value);
    }

    public float cosWave(double value, double delayMS) {
        return (float) (Mathf.clamp01((Math.cos(System.currentTimeMillis() / delayMS) + 1F) / 2F) * value);
    }

    public float triangleWave(double value, double delayMS) {
        double time = (System.currentTimeMillis() % delayMS) / delayMS;
        return (float) (Mathf.clamp01(1 - Math.abs(2 * time - 1)) * value);
    }

    public float sawtoothWave(double value, double delayMS) {
        double time = (System.currentTimeMillis() % delayMS) / delayMS;
        return (float) (Mathf.clamp01(time) * value);
    }

    public float squareWave(double value, double delayMS) {
        double time = (System.currentTimeMillis() % delayMS) / delayMS;
        return (float) ((time < 0.5 ? 1 : 0) * value);
    }

    public float pulseWave(double value, double delayMS, double dutyCycle) {
        double time = (System.currentTimeMillis() % delayMS) / delayMS;
        return (float) ((time < dutyCycle ? 1 : 0) * value);
    }

    public float bounceWave(double value, double delayMS) {
        double time = (System.currentTimeMillis() % delayMS) / delayMS;
        return (float) (Math.abs(Math.sin(time * Math.PI)) * value);
    }

    public float exponentialDecayWave(double value, double delayMS) {
        double time = (System.currentTimeMillis() % delayMS) / delayMS;
        return (float) (Math.pow(2, -10 * time) * value);
    }

    public float elasticWave(double value, double delayMS) {
        double time = (System.currentTimeMillis() % delayMS) / delayMS;
        return (float) (Math.sin(-13 * (time + 1) * Math.PI / 2) * Math.pow(2, -10 * time) * value);
    }

    public float heartbeatWave(double value, double delayMS) {
        double time = (System.currentTimeMillis() % delayMS) / delayMS;
        return (float) ((Math.sin(2 * Math.PI * time) >= 0 ? 1 : 0) * value);
    }

    public float dampedOscillationWave(double value, double delayMS) {
        double time = (System.currentTimeMillis() % delayMS) / delayMS;
        return (float) (Math.exp(-3 * time) * Math.cos(2 * Math.PI * time) * value);
    }
    public float flickerWave(double value, double delayMS) {
        double time = (System.currentTimeMillis() % delayMS) / delayMS;
        return (float) ((Math.random() > 0.5 ? 1 : 0) * value);
    }

    public float rippleWave(double value, double delayMS) {
        double time = (System.currentTimeMillis() % delayMS) / delayMS;
        return (float) (Math.sin(6 * Math.PI * time) * Math.exp(-2 * time) * value);
    }

    public float slowRiseWave(double value, double delayMS) {
        double time = (System.currentTimeMillis() % delayMS) / delayMS;
        return (float) (Math.pow(time, 3) * value);
    }

    public float springWave(double value, double delayMS) {
        double time = (System.currentTimeMillis() % delayMS) / delayMS;
        return (float) ((1 - Math.exp(-6 * time)) * Math.cos(8 * Math.PI * time) * value);
    }

    public float flashWave(double value, double delayMS) {
        double time = (System.currentTimeMillis() % delayMS) / delayMS;
        return (float) ((time < 0.1 || (time > 0.3 && time < 0.4)) ? value : 0);
    }
}
