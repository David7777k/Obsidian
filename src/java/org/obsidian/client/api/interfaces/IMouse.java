package org.obsidian.client.api.interfaces;

public interface IMouse {

    default boolean isHover(final double mouseX, final double mouseY, final double x, final double y, final double width, final double height) {
        return (mouseX >= x && mouseX <= (x + width)) && (mouseY >= y && mouseY <= (y + height));
    }

    default boolean isLClick(int button) {
        return button == 0;
    }

    default boolean isRClick(int button) {
        return button == 1;
    }

    default boolean isMClick(int button) {
        return button == 2;
    }
}