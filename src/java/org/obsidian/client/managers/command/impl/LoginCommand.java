package org.obsidian.client.managers.command.impl;

import com.mojang.authlib.GameProfile;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.Session;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.command.CommandException;
import org.obsidian.client.managers.command.api.Command;
import org.obsidian.client.managers.command.api.Logger;
import org.obsidian.client.managers.command.api.MultiNamedCommand;
import org.obsidian.client.managers.command.api.Parameters;
import org.obsidian.client.managers.component.impl.other.ConnectionComponent;
import org.obsidian.client.screen.account.Account;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.common.user.LoginManager;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LoginCommand implements Command, MultiNamedCommand, IMinecraft {
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String username = parameters.asString(0)
                .orElseThrow(() -> new CommandException(TextFormatting.RED + "Укажите никнейм для авторизации."));

        if (PlayerUtil.isInvalidName(username)) {
            logger.log(TextFormatting.RED + "Недопустимое имя.");
            return;
        }

        mc.session = new Session(username);
        LoginManager.saveUsername(username);
        Obsidian.inst().accountManager().addAccount(new Account(LocalDateTime.now(), username));
        GameProfile gameProfile = mc.session.getProfile();

        logger.log(TextFormatting.GRAY + "Ваш никнейм изменён на - " + TextFormatting.WHITE + username);
        if (!mc.isSingleplayer()) {
            mc.world.sendQuittingDisconnectingPacket();
            ConnectionComponent.connectToServer(ConnectionComponent.ip, ConnectionComponent.port, gameProfile);
        }

    }

    @Override
    public String name() {
        return "login";
    }

    @Override
    public String description() {
        return "Авторизирует вас под ником, который вы ввели";
    }

    @Override
    public List<String> aliases() {
        return List.of("l");
    }
}
