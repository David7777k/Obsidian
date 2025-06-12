package org.obsidian.client.ui.dropdown.utils;

import org.obsidian.client.utils.render.draw.StencilUtil;

public class Stencil {
    public static void initStencilToWrite() {
        StencilUtil.enable();
    }

    public static void readStencilBuffer(int ref) {
        StencilUtil.read(ref);
    }

    public static void uninitStencilBuffer() {
        StencilUtil.disable();
    }
}
