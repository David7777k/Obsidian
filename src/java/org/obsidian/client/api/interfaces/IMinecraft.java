package org.obsidian.client.api.interfaces;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;

public interface IMinecraft {
    Minecraft mc = Minecraft.getInstance();
    MainWindow mw = mc.getMainWindow();
}
