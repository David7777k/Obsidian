package org.obsidian.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ArmorStandEntity;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.render.RenderBlockModelEvent;
import org.obsidian.client.managers.events.render.RenderNameEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Optimizer", category = Category.MISC)
public class Optimizer extends Module {
    public static Optimizer getInstance() {
        return Instance.get(Optimizer.class);
    }

    private final MultiBooleanSetting checks = new MultiBooleanSetting(this, "Убрать",
            BooleanSetting.of("Растительность", false),
            BooleanSetting.of("Стойки для брони", false),
            BooleanSetting.of("Ломание блоков", false)
    );

    @EventHandler
    public void onEvent(RenderNameEvent event) {
        if (checks.getValue("Стойки для брони") && event.getEntity() instanceof ArmorStandEntity) {
            event.cancel();
        }
    }

    @EventHandler
    public void onEvent(RenderBlockModelEvent event) {
        BlockState blockState = event.getBlockState();
        if (checks.getValue("Растительность") && (blockState.getMaterial().equals(Material.PLANTS)
                || blockState.getMaterial().equals(Material.TALL_PLANTS)
                || blockState.getMaterial().equals(Material.OCEAN_PLANT)
                || blockState.getMaterial().equals(Material.NETHER_PLANTS)
                || blockState.getMaterial().equals(Material.SEA_GRASS))) {
            event.cancel();
        }
    }

}
