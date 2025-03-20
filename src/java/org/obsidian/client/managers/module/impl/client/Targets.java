package org.obsidian.client.managers.module.impl.client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Targets", category = Category.CLIENT, autoEnabled = true, allowDisable = false)
public class Targets extends Module {
    public static Targets getInstance() {
        return Instance.get(Targets.class);
    }

    private final ModeSetting sortMode = new ModeSetting(this, "Режим сортировки", "Адаптивный", "Дистанция", "Здоровье", "Наводка")
            .set("Наводка");
    private final BooleanSetting throughWalls = new BooleanSetting(this, "Сквозь стены", true);
    private final MultiBooleanSetting targets = new MultiBooleanSetting(this, "Таргеты",
            BooleanSetting.of("Игроки", true),
            BooleanSetting.of("Невидимые", true),
            BooleanSetting.of("Голые", true),
            BooleanSetting.of("Тиммейты", true),
            BooleanSetting.of("Животные", false),
            BooleanSetting.of("Мобы", false)
    );
}