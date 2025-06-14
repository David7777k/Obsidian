package net.minecraft.client.audio;

import com.google.common.collect.Lists;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.obsidian.common.impl.fastrandom.FastRandom;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class SoundEventAccessor implements ISoundEventAccessor<Sound> {
    private final List<ISoundEventAccessor<Sound>> accessorList = Lists.newArrayList();
    private final Random rnd = new FastRandom();
    private final ResourceLocation location;
    @Nullable
    private final ITextComponent subtitle;

    public SoundEventAccessor(ResourceLocation locationIn, @Nullable String subtitleIn) {
        this.location = locationIn;
        this.subtitle = subtitleIn == null ? null : new TranslationTextComponent(subtitleIn);
    }

    public int getWeight() {
        int i = 0;

        for (ISoundEventAccessor<Sound> isoundeventaccessor : this.accessorList) {
            i += isoundeventaccessor.getWeight();
        }

        return i;
    }

    public Sound cloneEntry() {
        int i = this.getWeight();

        if (!this.accessorList.isEmpty() && i != 0) {
            int j = this.rnd.nextInt(i);

            for (ISoundEventAccessor<Sound> isoundeventaccessor : this.accessorList) {
                j -= isoundeventaccessor.getWeight();

                if (j < 0) {
                    return isoundeventaccessor.cloneEntry();
                }
            }

            return SoundHandler.MISSING_SOUND;
        } else {
            return SoundHandler.MISSING_SOUND;
        }
    }

    public void addSound(ISoundEventAccessor<Sound> accessor) {
        this.accessorList.add(accessor);
    }

    @Nullable
    public ITextComponent getSubtitle() {
        return this.subtitle;
    }

    public void enqueuePreload(SoundEngine engine) {
        for (ISoundEventAccessor<Sound> isoundeventaccessor : this.accessorList) {
            isoundeventaccessor.enqueuePreload(engine);
        }
    }
}
