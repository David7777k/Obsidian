package org.obsidian.client.managers.command.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.command.api.Command;
import org.obsidian.client.managers.command.api.Logger;
import org.obsidian.client.managers.command.api.MultiNamedCommand;
import org.obsidian.client.managers.command.api.Parameters;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NameCommand implements Command, MultiNamedCommand, IMinecraft {
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        String name = mc.player.getGameProfile().getName();
        logger.log(TextFormatting.GRAY + "Ваш игровой никнейм: " + TextFormatting.WHITE + name);
        mc.keyboardListener.setClipboardString(name);
    }

    @Override
    public String name() {
        return "name";
    }

    @Override
    public String description() {
        return "Отображает и копирует ваш никнейм в буффер обмена.";
    }

    @Override
    public List<String> aliases() {
        return List.of("nick");
    }
}
