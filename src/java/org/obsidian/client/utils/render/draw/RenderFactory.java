package org.obsidian.client.utils.render.draw;

import net.mojang.blaze3d.systems.IRenderCall;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RenderFactory {
    private final Deque<IRenderCall> TASKS = new ConcurrentLinkedDeque<>();

    public void addTask(IRenderCall task) {
        if (task != null) {
            TASKS.add(task);
        }
    }

    public void renderAllTasks() {
        IRenderCall task;
        while ((task = TASKS.poll()) != null) {
            task.execute();
        }
    }
}