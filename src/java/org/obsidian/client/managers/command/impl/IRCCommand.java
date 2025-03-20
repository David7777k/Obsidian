package org.obsidian.client.managers.command.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.command.api.*;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IRCCommand implements Command, CommandWithAdvice, IMinecraft {

    private static final String IRC_DISABLED_MESSAGE = TextFormatting.RED + "Для использования команды - включите модуль IRC.";
    private static final String NO_MESSAGE_PROVIDED = TextFormatting.RED + "Укажите текст для отправки сообщения.";

    final Prefix prefix;
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
//        String arg = parameters.asString(0)
//                .orElse(null);
//        int uid = parameters.asInt(1)
//                .orElse(-1);
//        if (Obsidian.devMode() && Obsidian.inst().excNetwork().isConnected() && arg != null && uid != -1 && arg.equalsIgnoreCase("kick")) {
//            Obsidian.inst().excNetwork().getCommunication().write(new CKickPacket(uid));
//            return;
//        }
//        if (!IRC.getInstance().isEnabled()) {
//            logger.log(IRC_DISABLED_MESSAGE);
//            return;
//        }
//
//        String message = parameters.collectMessage(0).trim();
//        if (message.isEmpty()) {
//            logger.log(NO_MESSAGE_PROVIDED);
//            return;
//        }
//
//        if (Obsidian.inst().excNetwork().isConnected()) {
//            Obsidian.inst().excNetwork().getCommunication().write(new CMessagePacket(message));
//        } else {
//            logger.log(TextFormatting.RED + "Соединение с сетью не установлено.");
//        }
    }

    @Override
    public String name() {
        return "irc";
    }

    @Override
    public String description() {
        return "Общение между юзерами.";
    }

    @Override
    public List<String> adviceMessage() {
        String commandPrefix = prefix.get();
        return List.of(commandPrefix + "irc <message> - отправить сообщение");
    }
}
