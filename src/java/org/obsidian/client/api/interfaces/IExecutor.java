package org.obsidian.client.api.interfaces;

import org.obsidian.common.impl.thread.ThreadPool;

public interface IExecutor {
    ThreadPool THREAD_POOL = new ThreadPool();
}
