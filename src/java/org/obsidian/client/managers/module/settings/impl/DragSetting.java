package org.obsidian.client.managers.module.settings.impl;

import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.settings.Setting;
import org.obsidian.client.utils.animation.Animation;
import org.joml.Vector2f;

public class DragSetting extends Setting<Vector2f> {
    public final Vector2f targetPosition = new Vector2f();
    public final Vector2f position = new Vector2f();
    public final Vector2f size = new Vector2f();
    public boolean active = true;
    public boolean structure = false;
    public Animation animationX = new Animation();
    public Animation animationY = new Animation();

    public DragSetting(Module parent, String name) {
        super(parent, name, new Vector2f());
    }

    public DragSetting(Module parent, String name, Vector2f value) {
        super(parent, name, value);
        position.set(value);
        targetPosition.set(value);
    }

    public DragSetting(Module parent, String name, Vector2f value, boolean active) {
        super(parent, name, value);
        position.set(value);
        targetPosition.set(value);
        this.active = active;
    }

    public DragSetting(Module parent, String name, Vector2f value, boolean active, boolean structure) {
        super(parent, name, value);
        position.set(value);
        targetPosition.set(value);
        this.active = active && !structure;
        this.structure = structure;
    }

    @Override
    public Setting<?> set(Vector2f value) {
        position.set(value);
        targetPosition.set(value);
        return super.set(value);
    }

    @Override
    public Vector2f getValue() {
        return position;
    }

    @Override
    public DragSetting onAction(Runnable action) {
        return (DragSetting) super.onAction(action);
    }

    @Override
    public DragSetting onSetVisible(Runnable action) {
        return (DragSetting) super.onSetVisible(action);
    }
}