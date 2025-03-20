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

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "SeeInvisibles", category = Category.MISC)
public class SeeInvisibles extends Module {
    public static SeeInvisibles getInstance() {
        return Instance.get(SeeInvisibles.class);
    }

    private final SliderSetting alpha = new SliderSetting(this, "Прозрачность", 0.5F, 0.0F, 1.0F, 0.1F);
}