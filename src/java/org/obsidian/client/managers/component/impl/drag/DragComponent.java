package org.obsidian.client.managers.component.impl.drag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.gui.screen.ChatScreen;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.api.interfaces.IMouse;
import org.obsidian.client.api.interfaces.IWindow;
import org.obsidian.client.managers.component.Component;
import org.obsidian.client.managers.events.input.MousePressEvent;
import org.obsidian.client.managers.events.input.MouseReleaseEvent;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.settings.Setting;
import org.obsidian.client.managers.module.settings.impl.DragSetting;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.math.ScaleMath;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.common.impl.taskript.Script;
import org.joml.Vector2f;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

@Getter
public class DragComponent extends Component implements IWindow, IMouse {
    private DragSetting selected;
    private final Vector2f offset = new Vector2f();
    private final List<Module> modules = new CopyOnWriteArrayList<>();
    public static List<Line> lines = new CopyOnWriteArrayList<>();
    private final Script script = new Script();

    public void post(MatrixStack matrix) {
        script.update();

        final int width = mw.getScaledWidth();
        final int height = mw.getScaledHeight();

        int color = ColorUtil.getColor(255, 64);

        boolean shouldRender = mc.currentScreen instanceof ChatScreen;

        if (!shouldRender) {
            selected = null;
        }

        initModules();

        handleAnimation();

        if (selected != null) {
            if (!selected.active) return;
            final float mouseX = (float) (mc.mouseHelper.getMouseX() / mw.getScaleFactor());
            final float mouseY = (float) (mc.mouseHelper.getMouseY() / mw.getScaleFactor());
            final float positionX = mouseX + offset.x;
            final float positionY = mouseY + offset.y;

            selected.targetPosition.set(positionX, positionY);

            lines.clear();

            initSnaps(width, height);

            handleSnaps(matrix, color);

            handleDrags(width, height);
        }
    }

    private void handleDrags(int width, int height) {
        for (Module module : modules) {
            List<Setting<?>> positionSettings = module.getSettings().stream().filter(setting -> setting instanceof DragSetting).toList();
            for (Setting<?> setting : positionSettings) {
                if (setting instanceof DragSetting dragSetting) {
                    if (!dragSetting.active) continue;

                    dragSetting.position.x = (float) dragSetting.animationX.getValue();
                    dragSetting.position.y = (float) dragSetting.animationY.getValue();

                    dragSetting.position.x = Math.max(0, dragSetting.position.x);
                    dragSetting.position.x = Math.min(width - dragSetting.size.x, dragSetting.position.x);

                    dragSetting.position.y = Math.max(0, dragSetting.position.y);
                    dragSetting.position.y = Math.min(height - dragSetting.size.y, dragSetting.position.y);

                    dragSetting.targetPosition.x = Math.max(0, dragSetting.targetPosition.x);
                    dragSetting.targetPosition.x = Math.min(width - dragSetting.size.x, dragSetting.targetPosition.x);

                    dragSetting.targetPosition.y = Math.max(0, dragSetting.targetPosition.y);
                    dragSetting.targetPosition.y = Math.min(height - dragSetting.size.y, dragSetting.targetPosition.y);
                }
            }
        }
    }

    private void handleSnaps(MatrixStack matrix, int color) {
        double closest;

        for (Line snap : lines) {
            switch (snap.direction) {
                case VERTICAL:
                    closest = Double.MAX_VALUE;
                    for (float y = -selected.size.y; y <= 0; y += selected.size.y / 2F) {
                        if ((y == -selected.size.y / 2F && !snap.center) || (y == -selected.size.y && !snap.left) || (y == 0 && !snap.right)) {
                            continue;
                        }
                        double distance = Math.abs(selected.targetPosition.y - (snap.position + y));
                        if (distance < snap.distance && distance < closest) {
                            closest = distance;
                            selected.targetPosition.y = snap.position + y;
                            RectUtil.drawRect(matrix, 0, snap.position - 0.5F, scaled().x, 1F, color);
                        }
                    }
                    break;

                case HORIZONTAL:
                    closest = Double.MAX_VALUE;
                    for (float x = -selected.size.x; x <= 0; x += selected.size.x / 2F) {
                        if ((x == -selected.size.x / 2F && !snap.center) || (x == -selected.size.x && !snap.left) || (x == 0 && !snap.right)) {
                            continue;
                        }
                        float distance = Math.abs(selected.targetPosition.x - (snap.position + x));
                        if (distance < snap.distance && distance < closest) {
                            closest = distance;
                            selected.targetPosition.x = snap.position + x;
                            RectUtil.drawRect(matrix, snap.position - 0.5F, 0, 1F, scaled().y, color);
                        }
                    }
                    break;
            }
        }
    }

    private void initSnaps(int width, int height) {
        float edgeSnap = 5F;
        float distance = 5F;
        lines.add(new Line(width / 4f, distance, Direction.HORIZONTAL, true, true, true));
        lines.add(new Line(height / 4f, distance, Direction.VERTICAL, true, true, true));

        lines.add(new Line(width - (width / 4f), distance, Direction.HORIZONTAL, true, true, true));
        lines.add(new Line(height - (height / 4f), distance, Direction.VERTICAL, true, true, true));

        lines.add(new Line(width / 2f, distance, Direction.HORIZONTAL, true, true, true));
        lines.add(new Line(height / 2f, distance, Direction.VERTICAL, true, true, true));

        lines.add(new Line(height - edgeSnap, distance, Direction.VERTICAL, false, false, true));
        lines.add(new Line(edgeSnap, distance, Direction.VERTICAL, false, true, false));

        lines.add(new Line(width - edgeSnap, distance, Direction.HORIZONTAL, false, false, true));
        lines.add(new Line(edgeSnap, distance, Direction.HORIZONTAL, false, true, false));

        for (Module module : modules) {
            Stream<Setting<?>> positionSettings = module.getSettings()
                    .stream()
                    .filter(setting -> setting instanceof DragSetting);
            positionSettings.forEach(positionSetting -> {
                if (positionSetting instanceof DragSetting dragSetting && dragSetting != selected) {
                    lines.add(new Line(dragSetting.position.x + dragSetting.size.x + edgeSnap, distance, Direction.HORIZONTAL, false, true, false));
                    lines.add(new Line(dragSetting.position.x - edgeSnap, distance, Direction.HORIZONTAL, false, false, true));

                    lines.add(new Line(dragSetting.position.y - edgeSnap, distance, Direction.VERTICAL, false, false, true));
                    lines.add(new Line(dragSetting.position.y + dragSetting.size.y + edgeSnap, distance, Direction.VERTICAL, false, true, false));
                }
            });
        }
    }

    private void handleAnimation() {
        modules.forEach(module -> module.getSettings().forEach(setting -> {
            if (setting instanceof DragSetting dragSetting) {
                dragSetting.animationX.update();
                dragSetting.animationY.update();
                if (script.isFinished()) {
                    dragSetting.animationX.run(dragSetting.targetPosition.x, 0.25, Easings.CUBIC_OUT, true);
                    dragSetting.animationY.run(dragSetting.targetPosition.y, 0.25, Easings.CUBIC_OUT, true);
                }
            }
        }));
    }

    private void initModules() {
        modules.clear();
        Obsidian.inst().moduleManager().values().stream()
                .filter(module -> module.isEnabled() && module.getSettings().stream()
                        .anyMatch(setting -> setting instanceof DragSetting))
                .forEach(modules::add);
    }

    @EventHandler
    public void onEvent(MousePressEvent event) {
        if (event.getKey() != 0) {
            return;
        }
        if (event.getScreen() instanceof ChatScreen) {
            for (final Module module : modules) {
                for (final Setting<?> setting : module.getSettings()) {
                    if (setting instanceof final DragSetting dragSetting) {
                        if (!dragSetting.active) continue;
                        final Vector2f position = dragSetting.position;
                        final Vector2f scale = dragSetting.size;
                        final Vector2f mouse = ScaleMath.getMouse(event.getMouseX(), event.getMouseY());
                        final double mouseX = mouse.x;
                        final double mouseY = mouse.y;
                        if (!dragSetting.active) return;
                        if (!dragSetting.structure && isHover(mouseX, mouseY, position.x, position.y, scale.x, scale.y)) {
                            selected = dragSetting;
                            offset.set(position.x - mouseX, position.y - mouseY);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEvent(MouseReleaseEvent event) {
        if (selected != null) {
            script.cleanup()
                    .addTickStep(0, () -> {
                        selected.targetPosition.set(selected.position);
                        selected = null;
                    }, () -> selected != null && selected.animationX.isFinished() && selected.animationY.isFinished());
        }
    }

    @AllArgsConstructor
    public static class Line {
        public float position, distance;
        public DragComponent.Direction direction;
        public boolean center, right, left;
    }

    public enum Direction {
        VERTICAL,
        HORIZONTAL
    }
}