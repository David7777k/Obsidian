package net.minecraft.client.resources;

import net.minecraft.resources.ResourcePackType;
import net.minecraft.resources.VanillaPack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Predicate;

public class VirtualAssetsPack extends VanillaPack {
    private final ResourceIndex resourceIndex;

    public VirtualAssetsPack(ResourceIndex resourceIndex) {
        super("minecraft");
        this.resourceIndex = resourceIndex;
    }

    @Nullable
    protected InputStream getInputStreamVanilla(ResourcePackType type, ResourceLocation location) {
        if (type == ResourcePackType.CLIENT_RESOURCES) {
            File file = this.resourceIndex.getFile(location);

            if (file != null && file.exists()) {
                try {
                    return new FileInputStream(file);
                } catch (FileNotFoundException ignored) {
                }
            }
        }

        return super.getInputStreamVanilla(type, location);
    }

    public boolean resourceExists(ResourcePackType type, ResourceLocation location) {
        if (type == ResourcePackType.CLIENT_RESOURCES) {
            File file = this.resourceIndex.getFile(location);

            if (file != null && file.exists()) {
                return true;
            }
        }

        return super.resourceExists(type, location);
    }

    @Nullable
    protected InputStream getInputStreamVanilla(String pathIn) {
        File file = this.resourceIndex.getFile(pathIn);

        if (file != null && file.exists()) {
            try {
                return new FileInputStream(file);
            } catch (FileNotFoundException ignored) {
            }
        }

        return super.getInputStreamVanilla(pathIn);
    }

    public Collection<ResourceLocation> getAllResourceLocations(ResourcePackType type, String namespaceIn, String pathIn, int maxDepthIn, Predicate<String> filterIn) {
        Collection<ResourceLocation> collection = super.getAllResourceLocations(type, namespaceIn, pathIn, maxDepthIn, filterIn);
        collection.addAll(this.resourceIndex.getFiles(pathIn, namespaceIn, maxDepthIn, filterIn));
        return collection;
    }
}
