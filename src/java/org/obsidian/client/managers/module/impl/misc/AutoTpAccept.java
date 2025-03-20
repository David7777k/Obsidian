package org.obsidian.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.StringSetting;
import org.obsidian.client.utils.chat.ChatUtil;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.PlayerUtil;

import java.util.Arrays;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoTpAccept", category = Category.MISC)
public class AutoTpAccept extends Module {
    public static AutoTpAccept getInstance() {
        return Instance.get(AutoTpAccept.class);
    }

    private final StringSetting tpCommand = new StringSetting(this, "Команда", "/tpaccept");
    private final BooleanSetting onlyFriend = new BooleanSetting(this, "Только друзья", true);

    private final String[] teleportMessages = new String[]{
            "has requested teleport",
            "просит телепортироваться",
            "хочет телепортироваться к вам",
            "просит к вам телепортироваться"
    };

    @EventHandler
    public void onEvent(PacketEvent event) {
        if (PlayerUtil.isPvp()) return;
        if (event.isReceive() && event.getPacket() instanceof SChatPacket wrapper) {
            String message = TextFormatting.removeFormatting(wrapper.getChatComponent().getString());
            if (isTeleportMessage(message)) {
                if (onlyFriendsEnabled()) {
                    handleTeleportWithFriends(message);
                    return;
                }
                acceptTeleport();
            }
        }
    }

    private boolean isTeleportMessage(String message) {
        return Arrays.stream(this.teleportMessages)
                .map(String::toLowerCase)
                .anyMatch(message::contains);
    }

    private boolean onlyFriendsEnabled() {
        return onlyFriend.getValue();
    }

    private void handleTeleportWithFriends(String message) {
        for (String friend : Obsidian.inst().friendManager()) {
            StringBuilder builder = new StringBuilder();
            char[] charArray = message.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                char ch = charArray[i];
                if (ch == TextFormatting.COLOR_CODE) {
                    i++;
                } else {
                    builder.append(ch);
                }
            }

            if (builder.toString().contains(friend))
                acceptTeleport();
        }
    }

    private void acceptTeleport() {
        ChatUtil.sendText(tpCommand.getValue());
    }
}