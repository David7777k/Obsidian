package org.obsidian.client.managers.module.settings.impl;

import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.settings.Setting;

import java.util.function.Supplier;

public class BindSetting extends Setting<Integer> {
    public final boolean allowMouse;

    public BindSetting(Module parent, String name) {
        super(parent, name, -1);
        this.allowMouse = true;
    }

    public BindSetting(Module parent, String name, boolean allowMouse) {
        super(parent, name, -1);
        this.allowMouse = allowMouse;
    }

    public BindSetting(Module parent, String name, Integer value) {
        super(parent, name, value);
        this.allowMouse = true;
    }

    public BindSetting(Module parent, String name, Integer value, boolean allowMouse) {
        super(parent, name, value);
        this.allowMouse = allowMouse;
    }

    @Override
    public BindSetting setVisible(Supplier<Boolean> value) {
        return (BindSetting) super.setVisible(value);
    }

    @Override
    public BindSetting set(Integer value) {
        return (BindSetting) super.set(value);
    }

    @Override
    public BindSetting onAction(Runnable action) {
        return (BindSetting) super.onAction(action);
    }

    @Override
    public BindSetting onSetVisible(Runnable action) {
        return (BindSetting) super.onSetVisible(action);
    }

    @Override
    public Integer getValue() {
        // Возвращаем всегда актуальное значение
        return super.getValue();
    }
}
