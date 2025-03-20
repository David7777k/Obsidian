package org.obsidian.client.managers.module.settings;

import java.util.function.Supplier;

public interface ISetting {
    Setting<?> setVisible(Supplier<Boolean> value);
}