package org.obsidian.client.managers.command.api.logger;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.obsidian.client.managers.command.api.Logger;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MultiLogger implements Logger {

    final List<Logger> loggers;

    @Override
    public void log(String message) {
        for (Logger logger : loggers) {
            logger.log(message);
        }
    }
}
