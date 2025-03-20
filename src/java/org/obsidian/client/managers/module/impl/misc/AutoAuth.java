package org.obsidian.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.TextFormatting;
import org.apache.commons.lang3.RandomStringUtils;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.StringSetting;
import org.obsidian.client.utils.chat.ChatUtil;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.lib.util.time.StopWatch;

import java.util.HashMap;
import java.util.Map;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoAuth", category = Category.MISC)
public class AutoAuth extends Module {
    public static AutoAuth getInstance() {
        return Instance.get(AutoAuth.class);
    }

    private final StringSetting password = new StringSetting(this, "Введите пароль", RandomStringUtils.randomAlphanumeric(10));
    private final StopWatch time = new StopWatch();

    @EventHandler
    public void onEvent(PacketEvent event) {
        if (event.isSend()) return;
        final IPacket<?> packet = event.getPacket();
        if (packet instanceof SChatPacket wrapper) {
            String message = TextFormatting.removeFormatting(wrapper.getChatComponent().getString());
            if (message == null) return;
            String password = this.password.getValue();
            Map<String, String> commands = new HashMap<>();
            commands.put("/login", String.format("/login %s", password));
            commands.put("/l", String.format("/l %s", password));
            commands.put("/register", String.format("/register %s %s", password, password));
            commands.put("/reg", String.format("/reg %s %s", password, password));
            String[] split = message.split(" ");
            if (time.finished(1000)) {
                StringBuilder builder = new StringBuilder();
                for (String msg : split) {
                    if (commands.containsKey(msg.toLowerCase())) {
                        builder.append(commands.get(msg.toLowerCase()));
                        time.reset();
                        break;
                    }
                }
                if (!builder.isEmpty()) {
                    ChatUtil.sendText(builder.toString().trim());
                }
            }
        }
    }
}
