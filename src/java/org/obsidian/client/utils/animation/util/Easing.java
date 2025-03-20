package org.obsidian.client.utils.animation.util;

@FunctionalInterface
public interface Easing {
    double ease(double value);
}