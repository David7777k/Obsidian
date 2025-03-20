package org.obsidian.client.managers.module.settings.impl;

import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.settings.Setting;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MultiBooleanSetting extends Setting<Map<String, BooleanSetting>> {

    private final Map<String, BooleanSetting> settingsMap = new LinkedHashMap<>();

    public MultiBooleanSetting(Module parent, String name, BooleanSetting... values) {
        super(parent, name, new LinkedHashMap<>());
        for (BooleanSetting value : values) {
            getValue().put(value.getName().toLowerCase(), value);
            settingsMap.put(value.getName().toLowerCase(), value);
        }
    }

    public BooleanSetting get(String name) {
        return settingsMap.get(name.toLowerCase());
    }

    public boolean getValue(String name) {
        BooleanSetting setting = get(name);
        return setting != null && setting.getValue() && getVisible().get();
    }

    public Collection<BooleanSetting> getValues() {
        return getValue().values();
    }

    @Override
    public MultiBooleanSetting setVisible(Supplier<Boolean> value) {
        return (MultiBooleanSetting) super.setVisible(value);
    }

    @Override
    public MultiBooleanSetting onAction(Runnable action) {
        return (MultiBooleanSetting) super.onAction(action);
    }

    @Override
    public MultiBooleanSetting onSetVisible(Runnable action) {
        return (MultiBooleanSetting) super.onSetVisible(action);
    }

    public boolean isAnyTrue() {
        for (BooleanSetting setting : settingsMap.values()) {
            if (setting.getValue()) {
                return true;
            }
        }
        return false;
    }
}