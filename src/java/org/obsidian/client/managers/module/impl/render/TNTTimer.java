package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.Project;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.draw.RenderUtil3D;
import org.joml.Vector2f;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "TNTTimer", category = Category.RENDER)
public class TNTTimer extends Module {
    public static TNTTimer getInstance() {
        return Instance.get(TNTTimer.class);
    }

    @EventHandler
    public void onEvent(Render2DEvent event) {
        for (Entity entity : mc.world.getAllEntities()) {
            if (entity instanceof TNTEntity tnt) {
                final String name = Mathf.round(tnt.getFuse() / 20.0F, 1) + " сек";
                Vector3d pos = RenderUtil3D.interpolate(tnt, mc.getRenderPartialTicks());
                Vector2f vec = Project.project2D(pos.x, pos.y + tnt.getHeight() + 0.5, pos.z);
                if (vec.x == Float.MAX_VALUE && vec.y == Float.MAX_VALUE) return;

                final FontRenderer font = mc.fontRenderer;

                float width = font.getStringWidth(name);
                int black = ColorUtil.getColor(0, 0.5F);

                float halfWidth = width / 2.0F;
                RectUtil.drawRect(event.getMatrix(), vec.x - halfWidth, vec.y, width, font.FONT_HEIGHT, black);
                font.drawString(event.getMatrix(), TextFormatting.RED + name, 0.5F + vec.x - halfWidth, 1F + vec.y, ColorUtil.WHITE);
            }
        }
    }
}