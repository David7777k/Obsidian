package org.obsidian.client.screen.hud.impl;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.screen.ChatScreen;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.Obsidian;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.settings.impl.DragSetting;
import org.obsidian.client.screen.hud.AbstractHud;
import org.obsidian.client.screen.hud.IRenderer;
import org.obsidian.client.utils.keyboard.Keyboard;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;
import org.obsidian.client.utils.render.font.Fonts;

import java.util.List;

public class KeybindsRenderer extends AbstractHud implements IRenderer {
    private final List<Module> bindings = Lists.newArrayList();
    private final DragSetting drag;

    public KeybindsRenderer(DragSetting drag) {
        this.drag = drag;
    }

    @Override
    public void render(Render2DEvent event) {
        Obsidian.inst().moduleManager().values().forEach(module -> module.getAnimation().update());
        sortModules();

        MatrixStack matrix = event.getMatrix();

        String name = "Keybinds";

        boolean isEmpty = bindings.isEmpty();

        float margin = 5F;

        float expaned = margin + fontSize + margin;
        drag.size.x = 100;

        float width = drag.size.x;
        float height = drag.size.y;

        float x = drag.position.x;
        float y = drag.position.y;
        boolean closeCondition = isEmpty && !(mc.currentScreen instanceof ChatScreen);
        update(closeCondition ? 0 : 1);

        drag.active = !closeCondition;

        if (closeCondition && animValue() == 0.0F) {
            return;
        }
        float scale = animValue();

        matrix.push();
        matrix.translate((x + width / 2F), (y + height / 2F), 0);
        matrix.scale(scale, scale, 1);
        matrix.translate(-(x + width / 2F), -(y + height / 2F), 0);

        theme().drawClientRect(matrix, x, y, width, drag.size.y, animValue());
        font.draw(matrix, name, x + margin, y + (expaned / 2F) - (fontSize / 2F), textColor(), fontSize);
        Fonts.HUD.drawRight(matrix, "a", x + width - margin, y + margin, iconColor(), 10F);

        RectUtil.drawRect(matrix, x + 0.5F, y + expaned - 0.5F, width - 1F, 0.5, ColorUtil.multAlpha(textColor(), animValue() * 0.05F));
        RenderUtil.Rounded.smooth(matrix, x, y + expaned, width, height - expaned, 0, ColorUtil.getColor(0, animValue() * 0.25F), 0, ColorUtil.getColor(0, animValue() * 0.25F), Round.of(4, 0, 4, 0));

        float centerX = x + width / 2F;

        float offset = 0;
        for (Module binding : bindings) {
            float animPC = binding.getAnimation().get();
            float centerY = y + expaned + margin + offset;
            matrix.push();
            matrix.translate(centerX, centerY, 0);
            matrix.scale(1, animPC, 0);
            matrix.translate(-centerX, -centerY, 0);
            int color = ColorUtil.multAlpha(textAccentColor(), animPC);
            String keyName = Keyboard.keyName(binding.getKey());
            binding.getStripFont().draw(font, matrix, binding.getName(), x + margin, y + expaned + margin + offset, width - (margin * 2) - font.getWidth(keyName, fontSize) - margin, color, fontSize, 1F);
            font.drawRight(matrix, keyName, x + width - margin, y + expaned + margin + offset, color, fontSize);
            matrix.pop();
            offset += fontSize * animPC;
        }

        drag.size.y = expaned + offset + (margin * 2F);

        matrix.pop();

    }

    public void sortModules() {
        bindings.clear();
        bindings.addAll(Obsidian.inst().moduleManager().values().stream()
                .filter(this::shouldDisplay)
                .toList());
    }

    public boolean shouldDisplay(Module module) {
        return module.isAllowDisable()
                && (module.isEnabled() || module.getAnimation().getValue() != 0.0)
                && module.getKey() != Keyboard.KEY_NONE.getKey();
    }

}
