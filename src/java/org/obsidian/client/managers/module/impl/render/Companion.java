package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.events.render.Render3DLastEvent;
import org.obsidian.client.managers.events.world.WorldChangeEvent;
import org.obsidian.client.managers.events.world.WorldLoadEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.common.impl.companion.GhostCompanion;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Companion", category = Category.RENDER)
public class Companion extends Module {
    public static Companion getInstance() {
        return Instance.get(Companion.class);
    }

    private final GhostCompanion companion = new GhostCompanion();

    @Override
    protected void onEnable() {
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
    }

    @Override
    public void toggle() {
        super.toggle();
        companion.position().set(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ());
        companion.destination().set(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ());
        companion.motion().zero();
    }

    @EventHandler
    public void onEvent(WorldLoadEvent event) {
        companion.onEvent(event);
    }

    @EventHandler
    public void onEvent(WorldChangeEvent event) {
        companion.onEvent(event);
    }

    @EventHandler
    public void onEvent(Render3DLastEvent event) {
        companion.onEvent(event);
    }

    @EventHandler
    public void onEvent(UpdateEvent event) {
        companion.onEvent(event);
    }
}