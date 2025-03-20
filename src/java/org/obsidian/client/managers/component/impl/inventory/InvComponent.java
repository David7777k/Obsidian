package org.obsidian.client.managers.component.impl.inventory;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.client.settings.KeyBinding;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.api.events.orbit.EventPriority;
import org.obsidian.client.managers.component.Component;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.common.impl.taskript.Script;

@Getter
@Accessors(fluent = true)
public class InvComponent extends Component {
    private final Script script = new Script();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEvent(UpdateEvent event) {
        script.update();
    }

    public static void addTask(Runnable task, int priority) {
        addTask(task, priority, true);
    }

    public static void addTask(Runnable task, int priority, boolean cleanup) {
        Script script = getInstance().script();
        if (cleanup) script.cleanup();
        script.addTickStep(1, KeyBinding::unPressAllMoveKeys, priority)
                .addTickStep(1, () -> {
                    task.run();
                    KeyBinding.updateKeyBindState();
                }, priority);
    }

    public static InvComponent getInstance() {
        return Instance.getComponent(InvComponent.class);
    }
}
