package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.api.events.orbit.EventPriority;
import org.obsidian.client.managers.events.player.AttackEvent;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.managers.events.render.RenderScoreBoardEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.DelimiterSetting;
import org.obsidian.client.managers.module.settings.impl.DragSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.screen.hud.impl.*;
import org.obsidian.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Hud", category = Category.RENDER, autoEnabled = true, allowDisable = false)
public class Hud extends Module {
    public static Hud getInstance() {
        return Instance.get(Hud.class);
    }

    private final WatermarkRenderer watermarkRenderer;
    private final TargetHudRenderer targetHudRenderer;
    private final KeybindsRenderer keybindsRenderer;
    private final PotionsRenderer potionsRenderer;
    private final InformationRenderer informationRenderer;
    private final ScoreBoardRenderer scoreBoardRenderer;
    private final ArmorHudRenderer armorHudRenderer;

    public final MultiBooleanSetting checks = new MultiBooleanSetting(this, "Элементы",
            BooleanSetting.of("Watermark", true),
            BooleanSetting.of("TargetHud", true),
            BooleanSetting.of("Keybinds", true),
            BooleanSetting.of("Potions", true),
            BooleanSetting.of("Information", true),
            BooleanSetting.of("ScoreBoard", false),
            BooleanSetting.of("ArmorHud", true)
    );
    private final DelimiterSetting watermarkDelimiter = new DelimiterSetting(this, "Настройки Watermark")
            .setVisible(() -> checks.getValue("Watermark"));
    private final BooleanSetting duck = new BooleanSetting(this, "Duck Mode", false)
            .setVisible(() -> checks.getValue("Watermark"));

    private final MultiBooleanSetting watermarkChecks = new MultiBooleanSetting(this, "Элементы Watermark",
            BooleanSetting.of("Time", true),
            BooleanSetting.of("Fps", true)
    ).setVisible(() -> checks.getValue("Watermark"));


    private final DragSetting targetHudRendererDrag = new DragSetting(this, "TargetHud");
    private final DragSetting keybindsRendererDrag = new DragSetting(this, "Keybinds");
    private final DragSetting potionsRendererDrag = new DragSetting(this, "Potions");
    private final DragSetting scoreBoardDrag = new DragSetting(this, "ScoreBoard");

    public Hud() {
        targetHudRenderer = new TargetHudRenderer(targetHudRendererDrag);
        keybindsRenderer = new KeybindsRenderer(keybindsRendererDrag);
        potionsRenderer = new PotionsRenderer(potionsRendererDrag);
        scoreBoardRenderer = new ScoreBoardRenderer(scoreBoardDrag);
        watermarkRenderer = new WatermarkRenderer();
        informationRenderer = new InformationRenderer();
        armorHudRenderer = new ArmorHudRenderer();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(Render2DEvent event) {
        if (checks.getValue("Watermark")) watermarkRenderer.render(event);
        if (checks.getValue("Information")) informationRenderer.render(event);
        if (checks.getValue("ArmorHud")) armorHudRenderer.render(event);
        if (checks.getValue("TargetHud")) targetHudRenderer.render(event);
        if (checks.getValue("Keybinds")) keybindsRenderer.render(event);
        if (checks.getValue("Potions")) potionsRenderer.render(event);
    }

    @EventHandler
    public void onEvent(RenderScoreBoardEvent event) {
        if (checks.getValue("ScoreBoard")) scoreBoardRenderer.renderScoreBoard(event);
    }


    @EventHandler
    public void onEvent(AttackEvent event) {
        if (checks.getValue("TargetHud")) targetHudRenderer.attack(event);
    }
}
