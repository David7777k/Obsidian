package org.obsidian.client.managers.component;


import org.obsidian.client.Obsidian;
import org.obsidian.client.managers.component.impl.aura.AuraComponent;
import org.obsidian.client.managers.component.impl.client.ClientComponent;
import org.obsidian.client.managers.component.impl.drag.DragComponent;
import org.obsidian.client.managers.component.impl.inventory.HandComponent;
import org.obsidian.client.managers.component.impl.inventory.InvComponent;
import org.obsidian.client.managers.component.impl.other.ConnectionComponent;
import org.obsidian.client.managers.component.impl.other.SyncFixComponent;
import org.obsidian.client.managers.component.impl.rotation.FreeLookComponent;
import org.obsidian.client.managers.component.impl.rotation.RotationComponent;
import org.obsidian.client.managers.component.impl.rotation.SmoothRotationComponent;
import org.obsidian.client.managers.component.impl.target.TargetComponent;

import java.util.HashMap;

public final class ComponentManager extends HashMap<Class<? extends Component>, Component> {


    public void init() {
        add(
                new SyncFixComponent(),
                new AuraComponent(),
                new TargetComponent(),
                new DragComponent(),
                new FreeLookComponent(),
                new RotationComponent(),
                new SmoothRotationComponent(),
                new HandComponent(),
                new InvComponent(),
                new ClientComponent(),
                new ConnectionComponent()
        );

        this.values().forEach(component -> Obsidian.eventHandler().subscribe(component));
    }

    public void add(Component... components) {
        for (Component component : components) {
            this.put(component.getClass(), component);
        }
    }

    public void unregister(Component... components) {
        for (Component component : components) {
            Obsidian.eventHandler().unsubscribe(component);
            this.remove(component.getClass());
        }
    }

    public <T extends Component> T get(final Class<T> clazz) {
        return this.values()
                .stream()
                .filter(component -> component.getClass() == clazz)
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }
}