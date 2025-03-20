package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.play.server.SChangeGameStatePacket;
import net.minecraft.network.play.server.SUpdateTimePacket;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.events.player.MotionEvent;
import org.obsidian.client.managers.events.render.Render3DLastEvent;
import org.obsidian.client.managers.events.render.WorldColorEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.ColorSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.render.color.ColorUtil;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "CustomWorld", category = Category.RENDER)
public class CustomWorld extends Module {
    public static CustomWorld getInstance() {
        return Instance.get(CustomWorld.class);
    }

    private final ColorSetting color = new ColorSetting(this, "Цвет");
    private final SliderSetting distance = new SliderSetting(this, "Дистанция тумана", 1F, 0F, 1F, 0.01F);
    private final SliderSetting time = new SliderSetting(this, "Время суток", 16000, 0, 24000, 500);

    @Override
    public void toggle() {
        super.toggle();
        mc.world.setRainStrength(0);
        mc.world.getWorldInfo().setRaining(false);
    }

    @EventHandler
    public void onEvent(WorldColorEvent event) {
        float[] rgb = ColorUtil.getRGBf(color.getValue());
        event.setRed(rgb[0]);
        event.setGreen(rgb[1]);
        event.setBlue(rgb[2]);
    }

    @EventHandler
    public void onEvent(Render3DLastEvent event) {
        mc.world.setDayTime(time.getValue().intValue());
    }

    @EventHandler
    public void onEvent(MotionEvent event) {
        if (mc.player.ticksExisted % 20 == 0) {
            mc.world.setRainStrength(0);
            mc.world.getWorldInfo().setRaining(false);
        }
    }

    @EventHandler
    public void onEvent(PacketEvent event) {
        if (event.isReceive()) {
            if (event.getPacket() instanceof SUpdateTimePacket) {
                event.setCancelled(true);
            } else if (event.getPacket() instanceof SChangeGameStatePacket wrapper) {
                if (wrapper.getState() == SChangeGameStatePacket.field_241765_b_ || wrapper.getState() == SChangeGameStatePacket.field_241766_c_) {
                    event.setCancelled(true);
                }
            }
        }
    }
}