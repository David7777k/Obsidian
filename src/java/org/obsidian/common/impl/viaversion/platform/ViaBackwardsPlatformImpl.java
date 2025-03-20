package org.obsidian.common.impl.viaversion.platform;

import com.viaversion.viabackwards.api.ViaBackwardsPlatform;
import org.obsidian.common.impl.viaversion.ViaLoadingBase;

import java.io.File;
import java.util.logging.Logger;

public class ViaBackwardsPlatformImpl
        implements ViaBackwardsPlatform {
    private final File directory;

    public ViaBackwardsPlatformImpl(File directory) {
        this.directory = directory;
        this.init(this.directory);
    }

    @Override
    public Logger getLogger() {
        return ViaLoadingBase.LOGGER;
    }

    @Override
    public boolean isOutdated() {
        return false;
    }

    @Override
    public void disable() {
    }

    @Override
    public File getDataFolder() {
        return this.directory;
    }
}

