package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.util.Namespaced;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.RenderSystem;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.AttackEvent;
import org.obsidian.client.managers.events.player.MotionEvent;
import org.obsidian.client.managers.events.render.Render3DPosedEvent;
import org.obsidian.client.managers.events.world.WorldChangeEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.managers.module.settings.impl.*;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.RenderUtil3D;
import org.obsidian.common.impl.fastrandom.FastRandom;
import org.obsidian.lib.util.time.StopWatch;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Particles", category = Category.RENDER)
public class Particles extends Module {
    public static Particles getInstance() {
        return Instance.get(Particles.class);
    }

    private final MultiBooleanSetting events = new MultiBooleanSetting(this, "Спавнить при",
            BooleanSetting.of("Движении", true),
            BooleanSetting.of("Атаке", true),
            BooleanSetting.of("Крите", false).setVisible(() -> events().getValue("Атаке"))
    );
    private final SliderSetting countAttack = new SliderSetting(this, "Кол-во при атаке", 2, 1, 25, 1).setVisible(() -> events.getValue("Атаке"));
    private final SliderSetting countMove = new SliderSetting(this, "Кол-во при движении", 2, 1, 25, 1).setVisible(() -> events.getValue("Движении"));
    private final SliderSetting size = new SliderSetting(this, "Размер", 0.2F, 0.0F, 1F, 0.1F);
    private final SliderSetting strength = new SliderSetting(this, "Сила движения", 1.0F, 0.1F, 2.0F, 0.1F);
    private final SliderSetting opacity = new SliderSetting(this, "Прозрачность", 1.0F, 0.1F, 1.0F, 0.1F);
    private final BooleanSetting glowing = new BooleanSetting(this, "Свечение", true);
    private final BooleanSetting physic = new BooleanSetting(this, "Физика", false);
    private final ModeSetting colorMode = new ModeSetting(this, "Режим цвета", "Клиентский", "Радужный", "Свой");
    private final ColorSetting color = new ColorSetting(this, "Цвет").setVisible(() -> colorMode.is("Свой"));
    private final ModeSetting particleMode = new ModeSetting(this, "Тип частиц",
            "Random",
            "Amongus",
            "Circle",
            "Crown",
            "Dollar",
            "Heart",
            "Polygon",
            "Quad",
            "Skull",
            "Star",
            "Cross",
            "Triangle",
            "Bloom"
    ).set("Bloom");
    private final List<Particle> targetParticles = new ArrayList<>();
    private final List<Particle> flameParticles = new ArrayList<>();

    private void clear() {
        targetParticles.clear();
        flameParticles.clear();
    }

    @Override
    public void toggle() {
        super.toggle();
        clear();
    }

    @EventHandler
    public void onEvent(WorldChangeEvent event) {
        clear();
    }

    private void spawnParticle(List<Particle> particles, Vector3d position, Vector3d velocity) {
        float size = 0.05F + (this.size.getValue() * 0.2F);
        int color = switch (this.colorMode.getValue()) {
            case "Клиентский" -> ColorUtil.fade(particles.size() * 100);
            case "Радужный" -> ColorUtil.rainbow(Theme.getInstance().getSpeed(), particles.size() * 100, 0.5F, 1F, 1F);
            case "Свой" ->
                    ColorUtil.fade(Theme.getInstance().getSpeed(), particles.size() * 100, this.color.getValue(), ColorUtil.multDark(this.color.getValue(), 0.5F));
            default -> -1;
        };

        ParticleType type = switch (this.particleMode.getValue()) {
            case "Amongus" -> ParticleType.AMONGUS;
            case "Circle" -> ParticleType.CIRCLE;
            case "Crown" -> ParticleType.CROWN;
            case "Dollar" -> ParticleType.DOLLAR;
            case "Heart" -> ParticleType.HEART;
            case "Polygon" -> ParticleType.POLYGON;
            case "Quad" -> ParticleType.QUAD;
            case "Skull" -> ParticleType.SKULL;
            case "Star" -> ParticleType.STAR;
            case "Cross" -> ParticleType.CROSS;
            case "Triangle" -> ParticleType.TRIANGLE;
            case "Bloom" -> ParticleType.BLOOM;
            default -> ParticleType.getRandom();
        };

        particles.add(new Particle(type,
                position.add(0, size, 0),
                velocity,
                particles.size(),
                (int) Mathf.step(Mathf.randomValue(0, 360), 15),
                color,
                size)
        );
    }

    @EventHandler
    public void onEvent(AttackEvent event) {
        Entity target = event.getTarget();
        float motion = strength.getValue();
        if (events.getValue("Атаке")) {
            if (events.getValue("Крите") && mc.player.fallDistance == 0) return;
            for (int i = 0; i < countAttack.getValue().intValue(); i++) {
                spawnParticle(targetParticles, new Vector3d(target.getPosX(), target.getPosY() + Mathf.randomValue(0, target.getHeight()), target.getPosZ()),
                        new Vector3d(Mathf.randomValue(-motion, motion), Mathf.randomValue(-motion, motion / 4F), Mathf.randomValue(-motion, motion)));
            }
        }
    }

    @EventHandler
    public void onEvent(MotionEvent event) {
        if (events.getValue("Движении")) {
            if (hasPlayerMoved()) {
                if (!mc.gameSettings.getPointOfView().equals(PointOfView.FIRST_PERSON)) {
                    for (int i = 0; i < countMove.getValue().intValue(); i++) {
                        spawnParticle(flameParticles, new Vector3d(mc.player.getPosX() + Mathf.randomValue(-0.5, 0.5), mc.player.getPosY() + Mathf.randomValue(0, mc.player.getHeight()), mc.player.getPosZ() + Mathf.randomValue(-0.5, 0.5)),
                                new Vector3d(mc.player.motion.x + Mathf.randomValue(-0.25, 0.25), Mathf.randomValue(-0.15, 0.15), mc.player.motion.z + Mathf.randomValue(-0.25, 0.25)).mul(strength.getValue()));
                    }
                }
            }
        }

        removeExpiredParticles(targetParticles, 5000);
        removeExpiredParticles(flameParticles, 3500);
    }

    @EventHandler
    public void onEvent(Render3DPosedEvent event) {
        MatrixStack matrix = event.getMatrix();

        setupRenderState();
        renderParticles(matrix, targetParticles, 500, 2000);
        renderParticles(matrix, flameParticles, 500, 3000);
        resetRenderState();
    }

    private boolean hasPlayerMoved() {
        return mc.player.lastTickPosX != mc.player.getPosX()
                || mc.player.lastTickPosY != mc.player.getPosY()
                || mc.player.lastTickPosZ != mc.player.getPosZ();
    }

    private void removeExpiredParticles(List<Particle> particles, long lifespan) {
        particles.removeIf(particle -> !PlayerUtil.isInView(particle.box));
        particles.removeIf(particle -> particle.time().finished(lifespan));
    }

    private void setupRenderState() {
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
    }

    private void resetRenderState() {
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO
        );
        RenderSystem.clearCurrentColor();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableAlphaTest();
    }

    @SuppressWarnings("SameParameterValue")
    private void renderParticles(MatrixStack matrix, List<Particle> particles, long fadeInTime, long fadeOutTime) {
        if (particles.isEmpty()) return;

        matrix.push();
        for (Particle particle : particles) {
            particle.update(physic.getValue());
            particle.animation.update();

            if (particle.animation().getValue() != opacity.getValue() && !particle.time().finished(fadeInTime)) {
                particle.animation().run(opacity.getValue(), 0.5, Easings.CUBIC_OUT, true);
            }
            if (particle.animation().getValue() != 0 && particle.time().finished(fadeOutTime)) {
                particle.animation().run(0, 0.5, Easings.CUBIC_OUT, true);
            }

            int color = ColorUtil.replAlpha(particle.color(), particle.animation.get());
            Vector3d vec = particle.position();
            float x = (float) vec.x;
            float y = (float) vec.y;
            float z = (float) vec.z;

            renderParticle(matrix, particle, x, y, z, particle.size, color);
        }
        matrix.pop();
    }

    private void renderParticle(MatrixStack matrix, Particle particle, float x, float y, float z, float pos, int color) {
        matrix.push();
        RenderUtil3D.setupOrientationMatrix(matrix, x, y, z);
        matrix.rotate(mc.getRenderManager().getCameraOrientation());
        matrix.push();
        matrix.rotate(Vector3f.ZP.rotationDegrees(180F));
        if (particle.type().rotatable()) matrix.rotate(Vector3f.ZP.rotationDegrees(particle.rotate()));
        matrix.push();
        matrix.translate(0, -pos, 0);
        if (glowing.getValue()) {
            RenderUtil.bindTexture(FireFly.ParticleType.BLOOM.texture());
            RectUtil.drawRect(matrix, -pos * 4, -pos * 4, pos * 8, pos * 8, ColorUtil.multAlpha(color, 0.1F), true, true);
        }
        RenderUtil.bindTexture(particle.type().texture());
        RectUtil.drawRect(matrix, -pos, -pos, pos * 2, pos * 2, color, color, color, color, true, true);
        if (particle.type.equals(ParticleType.BLOOM)) {
            RectUtil.drawRect(matrix, -pos / 2, -pos / 2, pos, pos, color, true, true);
        }
        matrix.pop();
        matrix.pop();
        matrix.pop();
    }

    @Getter
    @Accessors(fluent = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public enum ParticleType {
        AMONGUS("amongus", false),
        CIRCLE("circle", false),
        CROWN("crown", false),
        DOLLAR("dollar", false),
        HEART("heart", false),
        POLYGON("polygon", true),
        QUAD("quad", true),
        SKULL("skull", false),
        STAR("star", true),
        CROSS("cross", true),
        TRIANGLE("triangle", true),
        BLOOM("bloom", false);

        ResourceLocation texture;
        boolean rotatable;

        ParticleType(String name, boolean rotatable) {
            texture = new Namespaced("particle/" + name + ".png");
            this.rotatable = rotatable;
        }

        public static ParticleType getRandom() {
            ParticleType[] values = ParticleType.values();
            return values[new FastRandom().nextInt(values.length)];
        }
    }

    @Getter
    @Accessors(fluent = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class Particle {
        AxisAlignedBB box;
        ParticleType type;
        @NonFinal
        Vector3d position;
        @NonFinal
        Vector3d velocity;
        int index;
        int rotate;
        int color;

        float size;

        StopWatch time = new StopWatch();
        Animation animation = new Animation();

        public Particle(ParticleType type, final Vector3d position, final Vector3d velocity, final int index, int rotate, int color, float size) {
            this.box = new AxisAlignedBB(position, position).grow(size);
            this.type = type;
            this.position = position;
            this.velocity = velocity.mul(0.01F);
            this.index = index;
            this.rotate = rotate;
            this.color = color;
            this.size = size;
            this.time.reset();
        }

        public void update(boolean physic) {
            if (physic) {
                if (PlayerUtil.isBlockSolid(this.position.x, this.position.y, this.position.z + this.velocity.z)) {
                    this.velocity = this.velocity.mul(1, 1, -0.8);
                }
                if (PlayerUtil.isBlockSolid(this.position.x, this.position.y + this.velocity.y, this.position.z)) {
                    this.velocity = this.velocity.mul(0.999, -0.6, 0.999);
                }
                if (PlayerUtil.isBlockSolid(this.position.x + this.velocity.x, this.position.y, this.position.z)) {
                    this.velocity = this.velocity.mul(-0.8, 1, 1);
                }
                this.velocity = this.velocity.mul(0.999999).subtract(new Vector3d(0, 0.00005, 0));
            }
            this.position = this.position.add(this.velocity);
        }
    }
}