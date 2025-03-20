package org.obsidian.client.managers.command.impl;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.command.api.Command;
import org.obsidian.client.managers.command.api.Logger;
import org.obsidian.client.managers.command.api.Parameters;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServerInfoCommand implements Command, IMinecraft {
    final Logger logger;

    @Override
    public void execute(Parameters parameters) {
        ServerData data = mc.getCurrentServerData();
        if (data == null || mc.isSingleplayer()) {
            logger.log(TextFormatting.GRAY + "Эта команда не работает в одиночном мире.");
            return;
        }

        ServerAddress serverAddress = ServerAddress.fromString(data.serverIP);

        logger.log(TextFormatting.GRAY + "Server info:");
        logger.log(TextFormatting.GRAY + "Name: " + TextFormatting.DARK_GRAY + data.serverName);
        logger.log(TextFormatting.GRAY + "IP: " + TextFormatting.DARK_GRAY + serverAddress.getIP() + ":" + serverAddress.getPort());
        logger.log(TextFormatting.GRAY + "Players: " + TextFormatting.DARK_GRAY + data.populationInfo.getString());
        logger.log(TextFormatting.GRAY + "MOTD: " + TextFormatting.DARK_GRAY + data.serverMOTD.getString());
        logger.log(TextFormatting.GRAY + "ServerVersion: " + TextFormatting.DARK_GRAY + data.gameVersion.getString());
        logger.log(TextFormatting.GRAY + "ProtocolVersion: " + TextFormatting.DARK_GRAY + data.version + " (" + ProtocolVersion.getProtocol(data.version).getName() + ")");
        logger.log(TextFormatting.GRAY + "Ping: " + TextFormatting.DARK_GRAY + data.pingToServer);

        // Проверка на античит
        String antiCheatInfo = getAntiCheatInfo(data);
        if (antiCheatInfo != null && !antiCheatInfo.isEmpty()) {
            logger.log(TextFormatting.GRAY + "AntiCheat: " + TextFormatting.DARK_GRAY + antiCheatInfo);
        } else {
            logger.log(TextFormatting.RED + "Античит не обнаружен! Будьте осторожны.");
        }
    }

    @Override
    public String name() {
        return "serverinfo";
    }

    @Override
    public String description() {
        return "Отображает информацию про сервер.";
    }

    private String getAntiCheatInfo(ServerData data) {
        // Пример: проверяем MOTD на наличие ключевых слов об античите
        if (data.serverMOTD.getString().toLowerCase().contains("anticheat")) {
            return "Enabled (detected in MOTD)";
        }
        // Здесь можно добавить дополнительную логику проверки на плагины/античит
        return null;
    }
}
