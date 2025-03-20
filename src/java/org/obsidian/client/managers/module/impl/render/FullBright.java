package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "FullBright", category = Category.RENDER)
public class FullBright extends Module {
    public static FullBright getInstance() {
        return Instance.get(FullBright.class);
    }

    private final ModeSetting mode = new ModeSetting(this, "Режим", "Гамма", "Ночное Зрение");
    
}