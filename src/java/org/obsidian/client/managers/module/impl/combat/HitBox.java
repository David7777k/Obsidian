package org.obsidian.client.managers.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.EntityHitBoxEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "HitBox", category = Category.COMBAT)
public class HitBox extends Module {
    public static HitBox getInstance() {
        return Instance.get(HitBox.class);
    }

    private final MultiBooleanSetting targets = new MultiBooleanSetting(this, "Сущности",
            BooleanSetting.of("Игроки", true),
            BooleanSetting.of("Мобы", true)
    );

    private final SliderSetting playersSize = new SliderSetting(this, "Игроки", 0.3F, 0F, 2F, 0.05F).setVisible(() -> targets.getValue("Игроки"));
    private final SliderSetting mobsSize = new SliderSetting(this, "Мобы", 0.3F, 0F, 2F, 0.05F).setVisible(() -> targets.getValue("Мобы"));

    @EventHandler
    public void onEvent(EntityHitBoxEvent event) {
        if (!(event.getEntity() instanceof LivingEntity)) return;
        if (targets.getValue("Игроки") && event.getEntity() instanceof PlayerEntity)
            event.setSize(playersSize.getValue());
        if (targets.getValue("Мобы") && event.getEntity() instanceof MobEntity) event.setSize(mobsSize.getValue());
    }
}