package org.obsidian.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.lib.util.time.StopWatch;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ItemScroller", category = Category.MISC)
public class ItemScroller extends Module {
    public static ItemScroller getInstance() {
        return Instance.get(ItemScroller.class);
    }

    private final SliderSetting delay = new SliderSetting(this, "Задержка", 100, 0, 1000, 1);
    private final StopWatch time = new StopWatch();
}