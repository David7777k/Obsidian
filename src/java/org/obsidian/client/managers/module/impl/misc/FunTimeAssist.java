package org.obsidian.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SPlaySoundEffectPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.obsidian.client.api.annotations.Funtime;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.input.KeyboardPressEvent;
import org.obsidian.client.managers.events.input.MousePressEvent;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BindSetting;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.player.InvUtil;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.Project;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.tuples.Pair;
import org.joml.Vector2f;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.obsidian.client.screen.hud.IRenderer.font;
import static org.obsidian.client.screen.hud.IRenderer.fontSize;

@Funtime
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "FunTimeAssist", category = Category.MISC)
public class FunTimeAssist extends Module {
    private final MultiBooleanSetting checks = new MultiBooleanSetting(this, "Элементы",
            BooleanSetting.of("Горячие клавиши", false),
            BooleanSetting.of("Таймер Расходников", false));
    private final Supplier<Boolean> fastKeys = () -> checks.getValue("Горячие клавиши");
    private final BooleanSetting PPT = new BooleanSetting(this, "Трапка + Пласт", true).setVisible(fastKeys);
    private final Map<Item, BindSetting> keyBindings = new HashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public final List<Pair<Long, Vector3d>> consumables = new ArrayList<>();

    public FunTimeAssist() {
        keyBindings.put(Items.ENDER_EYE, new BindSetting(this, "Кнопка дезориентации").setVisible(fastKeys));
        keyBindings.put(Items.NETHERITE_SCRAP, new BindSetting(this, "Кнопка трапки").setVisible(fastKeys));
        keyBindings.put(Items.SUGAR, new BindSetting(this, "Кнопка явной пыли").setVisible(fastKeys));
        keyBindings.put(Items.FIRE_CHARGE, new BindSetting(this, "Кнопка огненного смерча").setVisible(fastKeys));
        keyBindings.put(Items.DRIED_KELP, new BindSetting(this, "Кнопка пласта").setVisible(fastKeys));
        keyBindings.put(Items.PHANTOM_MEMBRANE, new BindSetting(this, "Кнопка божьей ауры").setVisible(fastKeys));
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        IPacket<?> packet = e.getPacket();

        if (checks.getValue("Таймер Расходников") && packet instanceof SPlaySoundEffectPacket wrapper) {
            if (wrapper.getSound().getName().getPath().equals("block.piston.contract")) {
                consumables.add(Pair.of(System.currentTimeMillis() + 15000, Vector3d.copyCentered(new BlockPos(wrapper.getX(), wrapper.getY(), wrapper.getZ()))));
            } else if (wrapper.getSound().getName().getPath().equals("block.anvil.place")) {
                BlockPos soundPos = new BlockPos(wrapper.getX(), wrapper.getY(), wrapper.getZ());
                long delay = 250;
                scheduler.schedule(() -> {
                    PlayerUtil.getCube(soundPos, 4, 4).stream().filter(pos -> Mathf.getDistance(soundPos, pos) > 2 && mc.world.getBlockState(pos).getBlock().equals(Blocks.COBBLESTONE))
                            .min(Comparator.comparing(pos -> Mathf.getDistance(soundPos, pos)))
                            .ifPresent(pos -> {
                                long andesiteCount = PlayerUtil.getCube(pos, 1, 1).stream().filter(pos2 -> mc.world.getBlockState(pos2).getBlock().equals(Blocks.ANDESITE)).count();
                                if (andesiteCount == 16 || andesiteCount == 9 || andesiteCount == 10) {
                                    int time = andesiteCount == 16 ? 60000 : 20000;
                                    consumables.add(Pair.of(System.currentTimeMillis() + time - delay, Vector3d.copyCentered(pos).add(0, andesiteCount == 16 ? -0.5 : 0, 0)));
                                }
                            });
                }, delay, TimeUnit.MILLISECONDS);
            }
        }
    }

    @EventHandler
    public void onRender2D(Render2DEvent e) {
        if (!PlayerUtil.isFuntime()) return;

        if (checks.getValue("Таймер Расходников")) {
            consumables.removeIf(cons -> (double) (cons.getFirst() - System.currentTimeMillis()) / 1000 <= 0);
            consumables.forEach(cons -> {
                Vector2f vec2f = Project.project2D(cons.getSecond());
                double time = Mathf.round((double) (cons.getFirst() - System.currentTimeMillis()) / 1000, 1);
                String text = time + "с";
                float size = fontSize;
                float width = font.getWidth(text, size);
                float posX = vec2f.x - width / 2;
                float posY = vec2f.y;

                RectUtil.drawRect(e.getMatrix(), posX - 1, posY - 1, width + 2, size + 2, ColorUtil.getColor(0, 0, 0, 128));
                font.draw(e.getMatrix(), text, posX, posY, -1, size);
            });
        }
    }

    @EventHandler
    public void onKeyPress(KeyboardPressEvent event) {
        if (event.getScreen() != null) return;

        keyBindings.forEach((item, bindSetting) -> {
            if (event.isKey(bindSetting.getValue())) {
                InvUtil.findItemAndThrow(item, mc.player.rotationYaw, mc.player.rotationPitch);
                if (item.equals(Items.NETHERITE_SCRAP) && PPT.getValue() && InvUtil.getSlot(item) != null && !mc.player.getCooldownTracker().hasCooldown(item))
                    scheduler.schedule(() -> InvUtil.findItemAndThrow(Items.DRIED_KELP, mc.player.rotationYaw, -90), 750, TimeUnit.MILLISECONDS);
            }
        });
    }

    @EventHandler
    public void onMousePress(MousePressEvent event) {
        if (event.getScreen() != null) return;

        keyBindings.forEach((item, bindSetting) -> {
            if (event.isKey(bindSetting.getValue())) {
                InvUtil.findItemAndThrow(item, mc.player.rotationYaw, mc.player.rotationPitch);
                if (item.equals(Items.NETHERITE_SCRAP) && PPT.getValue() && InvUtil.getSlot(item) != null && !mc.player.getCooldownTracker().hasCooldown(item))
                    scheduler.schedule(() -> InvUtil.findItemAndThrow(Items.DRIED_KELP, mc.player.rotationYaw, -90), 750, TimeUnit.MILLISECONDS);
            }
        });
    }
}
