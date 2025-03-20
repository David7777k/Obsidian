package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.math.BlockPos;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.render.ChunkRenderEvent;
import org.obsidian.client.managers.events.world.ChunkPositionEvent;
import org.obsidian.client.managers.events.world.WorldChangeEvent;
import org.obsidian.client.managers.events.world.WorldLoadEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.ListSetting;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.common.impl.chunkanimator.AnimationHandler;
import org.obsidian.common.impl.chunkanimator.easing.*;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ChunkAnimator", category = Category.RENDER)
public class ChunkAnimator extends Module {
    public static ChunkAnimator getInstance() {
        return Instance.get(ChunkAnimator.class);
    }

    private final AnimationHandler animationHandler;

    private final SliderSetting animationDuration = new SliderSetting(this, "Длительность анимации", 1000, 100, 5000, 100);
    private final ListSetting<AnimationMode> animationMode = new ListSetting<>(this, "Режим анимации", AnimationMode.values()).set(AnimationMode.BELOW);
    private final ModeSetting easingMode = new ModeSetting(this, "Режим интерполяции",
            "Linear",
            "Quad",
            "Cubic",
            "Quart",
            "Quint",
            "Expo",
            "Sine",
            "Circ",
            "Back",
            "Bounce",
            "Elastic"
    ).set("Sine");

    public ChunkAnimator() {
        this.animationHandler = new AnimationHandler();
    }

    @EventHandler
    public void onEvent(ChunkRenderEvent event) {
        animationHandler.preRender(event.getChunkRender());
    }

    @EventHandler
    public void onEvent(ChunkPositionEvent event) {
        animationHandler.setOrigin(event.getChunkRender(), new BlockPos(event.getX(), event.getY(), event.getZ()));
    }

    @EventHandler
    public void onEvent(WorldLoadEvent event) {
        animationHandler.clear();
    }

    @EventHandler
    public void onEvent(WorldChangeEvent event) {
        animationHandler.clear();
    }

    public float getFunctionValue(final float t, @SuppressWarnings("SameParameterValue") final float b, final float c, final float d) {
        return switch (easingMode.getValue()) {
            case "Quad" -> Quad.easeOut(t, b, c, d);
            case "Cubic" -> Cubic.easeOut(t, b, c, d);
            case "Quart" -> Quart.easeOut(t, b, c, d);
            case "Quint" -> Quint.easeOut(t, b, c, d);
            case "Expo" -> Expo.easeOut(t, b, c, d);
            case "Sine" -> Sine.easeOut(t, b, c, d);
            case "Circ" -> Circ.easeOut(t, b, c, d);
            case "Back" -> Back.easeOut(t, b, c, d);
            case "Bounce" -> Bounce.easeOut(t, b, c, d);
            case "Elastic" -> Elastic.easeOut(t, b, c, d);
            default -> Linear.easeOut(t, b, c, d);
        };

    }

    @Getter
    @RequiredArgsConstructor
    public enum AnimationMode {
        BELOW(0),
        ABOVE(1),
        HORIZONT(2),
        PLAYER_DIRECTION(3),
        DIRECTION(4);
        private final int mode;

        @Override
        public String toString() {
            String name = name().toLowerCase();
            String[] words = name.split("_");
            StringBuilder formattedName = new StringBuilder();

            for (String word : words) {
                formattedName.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
            return formattedName.toString().trim();
        }
    }
}