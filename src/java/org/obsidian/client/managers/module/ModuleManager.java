package org.obsidian.client.managers.module;


import org.obsidian.client.Obsidian;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.input.KeyboardPressEvent;
import org.obsidian.client.managers.events.input.MousePressEvent;
import org.obsidian.client.managers.module.impl.client.ClickGui;
import org.obsidian.client.managers.module.impl.client.Targets;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.managers.module.impl.combat.*;
import org.obsidian.client.managers.module.impl.misc.*;
import org.obsidian.client.managers.module.impl.movement.*;
import org.obsidian.client.managers.module.impl.player.*;
import org.obsidian.client.managers.module.impl.render.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public final class ModuleManager extends LinkedHashMap<Class<? extends Module>, Module> {

    public void init() {
        add(
                new AutoBuy(),
//                new IRC(),
                new WaterSpeed(),
                new Globals(),
                new AucHelper(),
                new Companion(),
                new ClickGui(),
                new ChunkAnimator(),
                new TriggerBot(),
                new AimAssist(),
                new AntiAFK(),
                new FullBright(),
                new AutoArmor(),
                new AspectRatio(),
                new EggMan(),
                new LeaveTracker(),
                new HitBox(),
                new TapeMouse(),
                new Parkour(),
                new AutoTool(),
                new AutoRespawn(),
                new DeathCoords(),
                new AutoFish(),
                new JumpCircle(),
                new TNTTimer(),
                new Trails(),
                new ChinaHat(),
                new HitColor(),
                new CustomHand(),
                new CustomWorld(),
                new NoFriendDamage(),
                new Notifications(),
                new Optimizer(),
                new NoPush(),
                new AutoSwap(),
                new AntiBot(),
                new FireFly(),
                new NoSlow(),
                new NameProtect(),
                new AutoPotion(),
                new KillAura(),
                new ShaderEsp(),
                new AutoGapple(),
                new Velocity(),
                new AutoTotem(),
                new NoEntityTrace(),
                new AutoAuth(),
                new AutoTpAccept(),
                new BetterMinecraft(),
                new FunTimeAssist(),
                new ItemScroller(),
                new SeeInvisibles(),
                new Flight(),
                new Sprint(),
                new Eagle(),
                new FreeCam(),
                new NoDelay(),
                new Hud(),
                new Nametags(),
                new Arrows(),
                new NoRender(),
                new Particles(),
                new Projectiles(),
                new ScreenWalk(),
                new TargetPearl(),
                new Targets(),
                new ContainerStealer(),
                new ClanUpgrade(),
                new FastBreak(),
                new NoFall(),
                new Nuker(),
                new ElytraHelper(),
                new Sneak(),
                new ContainerESP(),
                new ClickAction(),
                new AntiHunger(),
                new XRay(),
                new Speed(),
                new Criticals(),
                new EnderChestPlus(),
                new AutoBow(),
                new ElytraRecast(),
                new Spider(),
                new AutoFarm(),
                new AutoHeal(),
                new Theme(),
                new DiscordRPCModule(),
                new AutoMiner(),
                new AutoLeave(),
                new AutoSell(),
                new FastEXP(),
                new CordDroper()

        );
        this.values().stream().filter(Module::isAutoEnabled).forEach(module -> module.setEnabled(true, false));

        Obsidian.eventHandler().subscribe(this);
    }


    public void add(Module... modules) {
        Arrays.stream(modules).forEach(module -> this.put(module.getClass(), module));
    }

    public void unregister(Module... modules) {
        Arrays.stream(modules).forEach(module -> this.remove(module.getClass()));
    }

    @EventHandler
    public void onKeyboardPress(KeyboardPressEvent event) {
        if (event.getScreen() == null) {
            this.values().stream().filter(module -> module.getKey() == event.getKey()).forEach(Module::toggle);
        }
    }

    @EventHandler
    public void onMousePress(MousePressEvent event) {
        if (event.getScreen() == null) {
            this.values().stream().filter(module -> module.getKey() == event.getKey()).forEach(Module::toggle);
        }
    }

    public <T extends Module> T get(final String name) {
        return this.values().stream()
                .filter(module -> module.getName().equalsIgnoreCase(name))
                .map(module -> (T) module)
                .findFirst()
                .orElse(null);
    }

    public <T extends Module> T get(final Class<T> clazz) {
        return this.values().stream()
                .filter(module -> clazz.isAssignableFrom(module.getClass()))
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }

    public List<Module> get(final Category category) {
        return this.values().stream()
                .filter(module -> module.getCategory() == category)
                .collect(Collectors.toList());
    }
}