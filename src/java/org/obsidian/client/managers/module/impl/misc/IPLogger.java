package org.obsidian.client.managers.module.impl.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@ModuleInfo(name = "IPLogger", category = Category.MISC)
public class IPLogger extends Module {

    private final Minecraft mc = Minecraft.getInstance();

    @Override
    public void onEnable() {
        new Thread(() -> {
            try {
                URL url = new URL("https://api.ipify.org"); // можно заменить на любой другой IP-сервис
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()));
                String ip = in.readLine();
                in.close();

                if (mc.player != null) {
                    mc.player.sendMessage(new StringTextComponent("🌐 Твой IP: " + ip), mc.player.getUUID());
                }

            } catch (Exception e) {
                if (mc.player != null) {
                    mc.player.sendMessage(new StringTextComponent("Ошибка получения IP: " + e.getMessage()), mc.player.getUUID());
                }
            }
        }).start();
    }
}
