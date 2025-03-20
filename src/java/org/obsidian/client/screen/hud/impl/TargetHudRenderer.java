package org.obsidian.client.screen.hud.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Namespaced;
import net.minecraft.util.ResourceLocation;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import org.obsidian.client.managers.events.player.AttackEvent;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.managers.module.impl.combat.KillAura;
import org.obsidian.client.managers.module.settings.impl.DragSetting;
import org.obsidian.client.screen.hud.IAttack;
import org.obsidian.client.screen.hud.IRenderer;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.*;
import org.obsidian.client.utils.render.particle.Particle;
import org.obsidian.common.impl.fastrandom.FastRandom;
import org.obsidian.lib.util.time.StopWatch;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.StreamSupport;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TargetHudRenderer implements IRenderer, IAttack {
    private final DragSetting drag;
    private final Animation openAnimation = new Animation();
    private final Animation healthAnimation = new Animation();
    private final StopWatch time = new StopWatch();
    public boolean inWorld;
    public LivingEntity target;

    private final List<Particle> particles = new ArrayList<>();
    private final ResourceLocation bloom = new Namespaced("particle/bloom.png");
    private final Random random = new FastRandom();

    final float radius = 3F;

    final Round round = Round.of(radius);

    public TargetHudRenderer(DragSetting drag) {
        this.drag = drag;
    }

    @Override
    public void render(Render2DEvent event) {
        KillAura killAura = KillAura.getInstance();
        if (mc.pointedEntity instanceof PlayerEntity player) {
            target = player;
            time.reset();
        }
        if (killAura.isEnabled() && killAura.target() != null) {
            target = killAura.target();
            time.reset();
        }
        if (mc.currentScreen instanceof ChatScreen) {
            target = mc.player;
            time.reset();
        }
        if (target == null) {
            inWorld = false;
            return;
        }

        openAnimation.update();
        healthAnimation.update();

        inWorld = StreamSupport.stream(mc.world.getAllEntities().spliterator(), true).anyMatch(entity -> entity.equals(target));

        boolean out = (!inWorld || time.finished(1000));

        openAnimation.run(out ? 0.0 : 1.0, 0.3, out ? Easings.CUBIC_IN : Easings.CUBIC_OUT, true);
        if (openAnimation.getValue() <= 0.0) {
            return;
        }

        particles.removeIf(Particle::isFinished);

        particles.forEach(particle -> particle.update(Mathf.deltaTime()));

        drawDefault(event);
    }

    private void drawDefault(Render2DEvent event) {
        MatrixStack matrix = event.getMatrix();
        boolean isPlayer = this.target instanceof AbstractClientPlayerEntity;

        float x = drag.position.x;
        float y = drag.position.y;

        float width = drag.size.x;
        float height = drag.size.y;


        float hpHeight = 2.0F;
        float margin = 4.0F;
        float avatarSize = 16;
        float hpWidth = width - margin * 2;

        drag.size.set(100, margin + avatarSize + margin + hpHeight + margin);

        float appendX = x + margin + (isPlayer ? avatarSize + margin : 0);
        float appendY = y + margin;

        matrix.push();
        float scale = this.openAnimation.get();
        matrix.translate((x + width / 2F), (y + height / 2F), 0);
        matrix.scale(scale, scale, 0);
        matrix.translate(-(x + width / 2F), -(y + height / 2F), 0);

        int color = ColorUtil.getColor(14, 18, 24, 0.5F);

        // background
        theme().drawClientRect(matrix, x, y, width, height);

        RenderUtil.bindTexture(bloom);
        for (Particle particle : particles) {
            particle.setBaseX(drag.position.x + margin + avatarSize / 2);
            particle.setBaseY(drag.position.y + margin + avatarSize / 2);
            particle.getAnimation().run(particle.getTimePC(1500) < 0.5 ? 1 : 0, 1.500 / 2D, Easings.EXPO_OUT, true);
            int white = ColorUtil.multAlpha(theme().clientColor(), particle.getAnimation().get() / 5F);
            RectUtil.drawRect(matrix, particle.getBaseX() - (particle.getSize() * 4), particle.getBaseY() - (particle.getSize() * 4), particle.getSize() * 8.0F, particle.getSize() * 8.0F, ColorUtil.multAlpha(white, 0.05F), true, true);
            RectUtil.drawRect(matrix, particle.getBaseX() - (particle.getSize()), particle.getBaseY() - (particle.getSize()), particle.getSize() * 2.0F, particle.getSize() * 2.0F, white, true, true);
        }

        // avatar
        if (target instanceof AbstractClientPlayerEntity player) {
            drawHead(matrix, player, x + margin, appendY, avatarSize);
        }

        StencilUtil.enable();
        RectUtil.drawRect(matrix, appendX, appendY, hpWidth - (avatarSize + margin), fontSize, 0);
        StencilUtil.read(1);

        // name
        font.draw(matrix, target.getName().getString(), appendX, appendY, theme().textColor(), fontSize);
        StencilUtil.disable();

        font.draw(matrix, "HP: " + Mathf.round(target.getHealthFixed(), 1), appendX, appendY + avatarSize - fontSize, theme().textAccentColor(), fontSize);

        float health;
        float maxHealth;
        if (target instanceof AbstractClientPlayerEntity player) {
            health = !inWorld ? 0 : (float) Mathf.round(player.getHealthFixed(), 1);
            maxHealth = player.getMaxHealth();
        } else {
            health = !inWorld ? 0 : (float) Mathf.round(target.getHealth(), 1);
            maxHealth = target.getMaxHealth();
        }

        float healthBar = (health / maxHealth);
        this.healthAnimation.run(Mathf.clamp01(healthBar), 0.5, Easings.EXPO_OUT, true);

        // health bar
        int hpColor1 = theme().textColor();
        int hpColor2 = theme().textAccentColor();

        float hpY = y + height - margin - hpHeight;

        RenderUtil.Rounded.smooth(matrix, x + margin, hpY, hpWidth, hpHeight, color, Round.of(hpHeight / 3F));
        RenderUtil.Shadow.drawShadow(matrix, x + margin, hpY, hpWidth * healthAnimation.get(), hpHeight, 4, hpColor1, hpColor1, hpColor2, hpColor2, Round.of(hpHeight / 3F));
        RenderUtil.Rounded.smooth(matrix, x + margin, hpY, hpWidth * healthAnimation.get(), hpHeight, hpColor1, hpColor1, hpColor2, hpColor2, Round.of(hpHeight / 3F));

        matrix.pop();
    }

    private void drawHead(MatrixStack matrix, final AbstractClientPlayerEntity player, final float x, final float y, final float size) {

        float hurtPC = (float) Math.sin(target.hurtTime * (18F * Math.PI / 180F));

        int color = ColorUtil.overCol(ColorUtil.getColor(0, 1F), ColorUtil.RED, hurtPC);

        RenderUtil.Shadow.drawShadow(matrix, x, y, size, size, 8, color, color, color, color, Round.of(4));

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0.0F);
        RenderSystem.enableTexture();
        mc.getTextureManager().bindTexture(player.getLocationSkin());
        int headColor = ColorUtil.overCol(ColorUtil.WHITE, ColorUtil.RED, hurtPC);
        float[] rgba = ColorUtil.getRGBAf(headColor);

        RenderSystem.color4f(rgba[0], rgba[1], rgba[2], rgba[3]);
        AbstractGui.blit(matrix, x, y, size, size, 4F, 4F, 4F, 4F, 32F, 32F);
        GLUtil.scale(matrix, x + size / 2F, y + size / 2F, 1.15F, () ->
                AbstractGui.blit(matrix, x, y, size, size, 20, 4, 4, 4, 32, 32)
        );
        RenderSystem.color4f(1F, 1F, 1F, 1F);
        RenderSystem.disableBlend();
    }

    private void spawnParticles(float x, float y, float size, float motion, float mass, float gravity, float gravityMultiplier, float delay) {
        float motionX = (random.nextFloat() - 0.5F) * motion;
        float motionY = (random.nextFloat() - 0.5F) * motion;
        particles.add(new Particle(motionX + x, motionY + y, size, motionX, motionY, mass, gravity, gravityMultiplier, delay));
    }

    @Override
    public void attack(AttackEvent event) {
        if (this.target != null && event.getTarget() == target && target instanceof AbstractClientPlayerEntity) {
            float margin = 4.0F;
            float avatarSize = 16;
            for (int i = 0; i < 50; i++) {
                spawnParticles(drag.position.x + margin + avatarSize / 2F, drag.position.y + margin + avatarSize / 2F, 2F, Mathf.randomValue(25F, 50F), 0F, 0F, 0F, 1500);
            }
        }
    }
}
