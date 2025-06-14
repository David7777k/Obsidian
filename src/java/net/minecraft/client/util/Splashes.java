package net.minecraft.client.util;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.ReloadListener;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Session;
import org.obsidian.common.impl.fastrandom.FastRandom;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Splashes extends ReloadListener<List<String>> {
    private static final ResourceLocation SPLASHES_LOCATION = new ResourceLocation("texts/splashes.txt");
    private static final Random RANDOM = new FastRandom();
    private final List<String> possibleSplashes = Lists.newArrayList();
    private final Session gameSession;

    public Splashes(Session gameSessionIn) {
        this.gameSession = gameSessionIn;
    }

    protected List<String> prepare(IResourceManager resourceManagerIn) {
        try (
                IResource iresource = Minecraft.getInstance().getResourceManager().getResource(SPLASHES_LOCATION);
                BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(iresource.getInputStream(), StandardCharsets.UTF_8))
        ) {
            return bufferedreader.lines().map(String::trim).filter((p_215277_0_) ->
            {
                return p_215277_0_.hashCode() != 125780783;
            }).collect(Collectors.toList());
        } catch (IOException ioexception) {
            return Collections.emptyList();
        }
    }

    protected void apply(List<String> objectIn, IResourceManager resourceManagerIn) {
        this.possibleSplashes.clear();
        this.possibleSplashes.addAll(objectIn);
    }

    @Nullable
    public String getSplashText() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        if (calendar.get(2) + 1 == 12 && calendar.get(5) == 24) {
            return "Merry X-mas!";
        } else if (calendar.get(2) + 1 == 1 && calendar.get(5) == 1) {
            return "Happy new year!";
        } else if (calendar.get(2) + 1 == 10 && calendar.get(5) == 31) {
            return "OOoooOOOoooo! Spooky!";
        } else if (this.possibleSplashes.isEmpty()) {
            return null;
        } else {
            return this.gameSession != null && RANDOM.nextInt(this.possibleSplashes.size()) == 42 ? this.gameSession.getUsername().toUpperCase(Locale.ROOT) + " IS YOU" : this.possibleSplashes.get(RANDOM.nextInt(this.possibleSplashes.size()));
        }
    }
}
