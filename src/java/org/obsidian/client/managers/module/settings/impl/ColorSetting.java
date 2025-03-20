package org.obsidian.client.managers.module.settings.impl;

import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.settings.Setting;
import org.obsidian.client.utils.render.color.ColorUtil;

import java.util.function.Supplier;

public class ColorSetting extends Setting<Integer> {
    private Integer cachedValue;

    public ColorSetting(Module parent, String name) {
        super(parent, name, ColorUtil.RED);
        cachedValue = ColorUtil.RED;
    }

    public ColorSetting(Module parent, String name, Integer value) {
        super(parent, name, value);
        cachedValue = value;
    }

    @Override
    public ColorSetting set(Integer value) {
        ColorSetting set = (ColorSetting) super.set(value);
        this.cachedValue = super.getValue();
        return set;
    }

    @Override
    public ColorSetting setVisible(Supplier<Boolean> value) {
        return (ColorSetting) super.setVisible(value);
    }

    @Override
    public ColorSetting onAction(Runnable action) {
        return (ColorSetting) super.onAction(() -> {
            action.run();
            this.cachedValue = super.getValue();
        });
    }

    @Override
    public ColorSetting onSetVisible(Runnable action) {
        return (ColorSetting) super.onSetVisible(action);
    }

    @Override
    public Integer getValue() {
        if (cachedValue == null) {
            cachedValue = super.getValue();
        }
        return cachedValue;
    }

    public Integer getColor() {
        return getValue();
    }
}