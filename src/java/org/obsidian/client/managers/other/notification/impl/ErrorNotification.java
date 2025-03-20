package org.obsidian.client.managers.other.notification.impl;

import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.managers.other.notification.Notification;

public class ErrorNotification extends Notification {
    public ErrorNotification(String content, long delay, int index) {
        super(content, delay, index);
    }

    @Override
    public void render(MatrixStack matrix, int multiplier) {

    }

}