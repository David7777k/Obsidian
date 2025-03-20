package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "NoRender", category = Category.RENDER)
public class NoRender extends Module {
    public static NoRender getInstance() {
        return Instance.get(NoRender.class);
    }

    private final MultiBooleanSetting elements = new MultiBooleanSetting(this, "Элементы",
            BooleanSetting.of("Огонь", true),
            BooleanSetting.of("Тряска", true),
            BooleanSetting.of("Боссбар", false),
            BooleanSetting.of("Плохие эффекты", true),
            BooleanSetting.of("Скорборд", false),
            BooleanSetting.of("Анимация тотема", false)
    );
}
