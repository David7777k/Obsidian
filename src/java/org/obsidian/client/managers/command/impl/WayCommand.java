package org.obsidian.client.managers.command.impl;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Namespaced;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.api.events.Handler;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.command.CommandException;
import org.obsidian.client.managers.command.api.*;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.Project;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.font.Font;
import org.obsidian.client.utils.render.font.Fonts;
import org.joml.Vector2f;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class WayCommand extends Handler implements Command, CommandWithAdvice, MultiNamedCommand, IMinecraft {
    final Prefix prefix;
    final Logger logger;
    final Font font = Fonts.SF_SEMIBOLD;
    final Map<String, Vector3i> waysMap = new LinkedHashMap<>();
    final Namespaced arrow = new Namespaced("texture/arrow.png");

    public WayCommand(Prefix prefix, Logger logger) {
        this.prefix = prefix;
        this.logger = logger;
    }

    @Override
    public void execute(Parameters parameters) {
        String commandType = parameters.asString(0).orElse("");

        switch (commandType) {
            case "add" -> addWayPoint(parameters);
            case "remove" -> removeWayPoint(parameters);
            case "clear" -> {
                waysMap.clear();
                logger.log("Все пути были удалены!");
            }
            case "list" -> {
                logger.log("Список путей:");

                for (String s : waysMap.keySet()) {
                    logger.log("- " + s + " " + waysMap.get(s));
                }
            }
            default ->
                    throw new CommandException(TextFormatting.RED + "Укажите тип команды:" + TextFormatting.GRAY + " add, remove, clear");
        }
    }

    private void addWayPoint(Parameters param) {
        String name = param.asString(1).orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите имя координаты!"));
        int x = param.asInt(2).orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите первую координату!"));
        int y = param.asInt(3).orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите вторую координату!"));
        int z = param.asInt(4).orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите третью координату!"));

        Vector3i vec = new Vector3i(x, y, z);
        if (!name.isEmpty()) {
            waysMap.put(name, vec);
            logger.log("Путь " + name + " был добавлен!");
        } else {
            logger.log("Название не может быть пустым!");
        }
    }

    private void removeWayPoint(Parameters param) {
        String name = param.asString(1)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите имя координаты!"));

        waysMap.remove(name);
        logger.log("Путь " + name + " был удалён!");
    }

    @Override
    public String name() {
        return "way";
    }

    @Override
    public String description() {
        return "Позволяет работать с координатами путей";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(commandPrefix + "waypoint add <имя, x, y, z> - Проложить путь к WayPoint'у",
                commandPrefix + "waypoint remove <имя> - Удалить WayPoint",
                commandPrefix + "waypoint list - Список WayPoint'ов",
                commandPrefix + "waypoint clear - Очистить список WayPoint'ов",
                "Пример: " + TextFormatting.RED + commandPrefix + "way add аирдроп 1000 100 1000"
        );
    }

    @EventHandler
    public void onRender2D(Render2DEvent event) {
        if (waysMap.isEmpty()) {
            return;
        }
        for (String name : waysMap.keySet()) {
            Vector3d vec3d = Vector3d.copyCentered(waysMap.get(name));
            Vector2f vec2f = Project.project2D(vec3d.x, vec3d.y, vec3d.z);
            int distance = (int) Minecraft.getInstance().player.getPositionVec().distanceTo(vec3d);
            String text = name + " (" + distance + "м)";
            float fontHeight = 7;
            float textWith = font.getWidth(text, fontHeight);
            float posX = vec2f.x - textWith / 2;
            float posY = vec2f.y - fontHeight / 2;
            float padding = 1;

            if (vec2f.x == Float.MAX_VALUE && vec2f.y == Float.MAX_VALUE) continue;

            RectUtil.drawRect(event.getMatrix(), posX - padding, posY - padding, padding + textWith + padding, padding + fontHeight + padding, ColorUtil.getColor(0, 0, 0, 128));
            font.draw(event.getMatrix(), text, posX, posY, -1, fontHeight);
        }
    }

    @Override
    public List<String> aliases() {
        return List.of("waypoint");
    }


}
