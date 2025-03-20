package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.network.play.server.SMultiBlockChangePacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.events.render.Render3DLastEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.render.draw.RenderUtil3D;

import java.awt.*;
import java.util.ArrayList;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "XRay", category = Category.RENDER)
public class XRay extends Module {
    public static XRay getInstance() {
        return Instance.get(XRay.class);
    }

    private final ModeSetting mode = new ModeSetting(this, "Режим", "Древние Обломки");
    private final ArrayList<BlockPos> ores = new ArrayList<>();

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (mode.is("Древние Обломки")) {
            if (e.getPacket() instanceof SMultiBlockChangePacket packet) {
                packet.func_244310_a((blockPos, blockState) -> {
                    blockPos.add(0, 0, 0);
                    if (blockState.getBlock().equals(Blocks.ANCIENT_DEBRIS) && !ores.contains(blockPos)) {
                        ores.add(blockPos);
                    }
                });
            }
        }
    }

    @EventHandler
    public void onRender3D(Render3DLastEvent e) {
        if (mode.is("Древние Обломки")) {
            ores.removeIf(pos -> !mc.world.getBlockState(pos).getBlock().equals(Blocks.ANCIENT_DEBRIS));
            ores.forEach(pos -> RenderUtil3D.drawBoundingBox(e.getMatrix(), new AxisAlignedBB(pos), new Color(0xC3A278).getRGB(), 1, false, false));
        }
    }
}
