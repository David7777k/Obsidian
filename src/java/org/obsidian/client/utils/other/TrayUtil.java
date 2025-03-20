package org.obsidian.client.utils.other;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.obsidian.client.api.client.Constants;

import java.awt.*;

@UtilityClass
public class TrayUtil {
    public void info(String name, String desc) {
        send(name, desc, TrayIcon.MessageType.INFO);
    }

    public void error(String name, String desc) {
        send(name, desc, TrayIcon.MessageType.ERROR);
    }

    public void warn(String name, String desc) {
        send(name, desc, TrayIcon.MessageType.WARNING);
    }

    @SneakyThrows
    private void send(String name, String desc, TrayIcon.MessageType type) {
        if (SystemTray.isSupported()) {
            SystemTray systemTray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("");
            TrayIcon trayIcon = new TrayIcon(image, Constants.NAMESPACE);
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip(name);
            systemTray.add(trayIcon);
            trayIcon.displayMessage(name, desc, type);
        }
    }
}
