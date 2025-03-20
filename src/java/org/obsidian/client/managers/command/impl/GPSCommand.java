package org.obsidian.client.managers.command.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.Namespaced;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import org.obsidian.client.api.events.Handler;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.command.CommandException;
import org.obsidian.client.managers.command.api.Command;
import org.obsidian.client.managers.command.api.CommandWithAdvice;
import org.obsidian.client.managers.command.api.Parameters;
import org.obsidian.client.managers.command.api.Prefix;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.font.Font;
import org.obsidian.client.utils.render.font.Fonts;

import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class GPSCommand extends Handler implements Command, CommandWithAdvice, IMinecraft {
    final Prefix prefix;
    Vector2f cordsMap = new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
    final Font font = Fonts.SF_SEMIBOLD;
    final Namespaced arrow = new Namespaced("texture/arrow.png");

    public GPSCommand(Prefix prefix) {
        this.prefix = prefix;
    }

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");

        switch (commandType) {
            case "set" -> addGPS(parameters);
            case "off" -> removeGPS();
            default ->
                    throw new CommandException(TextFormatting.RED + "Укажите тип команды:" + TextFormatting.GRAY + " set, off");
        }
    }

    private void addGPS(Parameters param) {
        int x = param.asInt(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите первую координату!"));
        int z = param.asInt(2)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите вторую координату!"));

        int temp = param.asInt(3).orElse(Integer.MAX_VALUE);

        if (temp != Integer.MAX_VALUE) {
            z = temp;
        }

        cordsMap = new Vector2f(x, z);

    }

    private void removeGPS() {
        cordsMap = new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
    }

    @Override
    public String name() {
        return "gps";
    }

    @Override
    public String description() {
        return "Показывает стрелочку которая ведёт к координатам";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(commandPrefix + "gps set <x, z> - Проложить путь",
                commandPrefix + "gps off - Удалить GPS",
                "Пример: " + TextFormatting.RED + commandPrefix + "gps set 100 150"
        );
    }

    @EventHandler
    public void onEvent(Render2DEvent event) {
        if (cordsMap.x == Float.MAX_VALUE && cordsMap.y == Float.MAX_VALUE) {
            return;
        }

        Vector3d pos = new Vector3d(
                cordsMap.x + 0.5,
                128 + 0.5,
                cordsMap.y + 0.5
        );

        int dst = (int) Math.sqrt(Math.pow(pos.x - mc.player.getPosX(), 2) + Math.pow(pos.z - mc.player.getPosZ(), 2));

        String text = dst + "м";
        Vector3d localVec = pos.subtract(mc.getRenderManager().info.getProjectedView());

        double x = localVec.getX();
        double z = localVec.getZ();

        double cos = MathHelper.cos((float) (mc.getRenderManager().info.getYaw() * (Math.PI * 2 / 360)));
        double sin = MathHelper.sin((float) (mc.getRenderManager().info.getYaw() * (Math.PI * 2 / 360)));

        double rotY = -(z * cos - x * sin);
        double rotX = -(x * cos + z * sin);

        float angle = (float) (Math.atan2(rotY, rotX) * 180 / Math.PI);

        double x2 = 30 * MathHelper.cos((float) Math.toRadians(angle)) + mw.getScaledWidth() / 2f;
        double y2 = 30 * MathHelper.sin((float) Math.toRadians(angle)) + mw.getScaledHeight() / 2f;

        GlStateManager.pushMatrix();
        GlStateManager.disableBlend();
        GlStateManager.translated(x2, y2, 0);

        font.drawCenterOutline(event.getMatrix(), text, 0, 15, -1, 6);

        GlStateManager.rotatef(angle, 0, 0, 1);

        int color = Theme.getInstance().clientColor();

        MatrixStack matrix = event.getMatrix();

        float size = 16;
        RenderUtil.bindTexture(arrow);
        matrix.translate(size / 2F, size / 2F, 0);
        matrix.push();

        matrix.translate(-(size / 2F), -(size / 2F), 0);
        matrix.rotate(Vector3f.ZN.rotationDegrees(-90));
        matrix.translate((size / 2F), (size / 2F), 0);

        RectUtil.drawRect(matrix, -size, -size, size, size, color, color, ColorUtil.multAlpha(color, 0), ColorUtil.multAlpha(color, 0), true, true);
        matrix.pop();
        matrix.translate(-(size / 2F), -(size / 2F), 0);

        GlStateManager.enableBlend();
        GlStateManager.popMatrix();
    }
}
