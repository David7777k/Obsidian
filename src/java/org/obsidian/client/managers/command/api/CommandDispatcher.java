package org.obsidian.client.managers.command.api;


import org.obsidian.client.managers.command.DispatchResult;

public interface CommandDispatcher {
    DispatchResult dispatch(String command);
}
