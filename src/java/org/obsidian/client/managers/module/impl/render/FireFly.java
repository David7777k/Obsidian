package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.Namespaced;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.Heightmap;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.RenderSystem;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.MotionEvent;
import org.obsidian.client.managers.events.render.Render3DPosedEvent;
import org.obsidian.client.managers.events.world.WorldChangeEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.ColorSetting;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
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
@ModuleInfo(name = "FireFly", category = Category.RENDER)
public class FireFly extends Module {
    public static FireFly getInstance() {
        return Instance.get(FireFly.class);
    }

    private final SliderSetting count = new SliderSetting(this, "Кол-во", 5, 1, 25, 1);
    private final SliderSetting size = new SliderSetting(this, "Размер", 0.5F, 0.0F, 1F, 0.1F);
    private final SliderSetting range = new SliderSetting(this, "Дистанция", 16, 4, 32, 1);
    private final SliderSetting duration = new SliderSetting(this, "Время жизни", 3500, 500, 5000, 250);
    private final SliderSetting strength = new SliderSetting(this, "Сила движения", 1.0F, 0.1F, 2.0F, 0.1F);
    private final SliderSetting opacity = new SliderSetting(this, "Прозрачность", 1.0F, 0.1F, 1.0F, 0.1F);
    private final BooleanSetting glowing = new BooleanSetting(this, "Свечение", true);
    private final BooleanSetting onlyMove = new BooleanSetting(this, "Только в движении", false);
    private final BooleanSetting ground = new BooleanSetting(this, "Спавнить на земле", false);
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

    private final List<Particle> particles = new ArrayList<>();

    private void clear() {
        particles.clear();
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

    @EventHandler
    public void onEvent(MotionEvent event) {
        int range = this.range.getValue().intValue();
        if (onlyMove.getValue() && !hasPlayerMoved()) return;
        for (int i = 0; i < count.getValue().intValue(); i++) {
            Vector3d additional = mc.player.getPositionVec().add(Mathf.randomValue(-range, range), 0, Mathf.randomValue(-range, range));
            BlockPos pos = mc.world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(additional));
            spawnParticle(new Vector3d(pos.getX() + Mathf.randomValue(0, 1), ground.getValue() ? pos.getY() : mc.player.getPosY() + Mathf.randomValue(mc.player.getHeight(), range), pos.getZ() + Mathf.randomValue(0, 1)), new Vector3d(0, Mathf.randomValue(0.0, strength.getValue()) * (ground.getValue() ? 1 : -1), 0));
        }
    }

    @EventHandler
    public void onEvent(Render3DPosedEvent event) {
        MatrixStack matrix = event.getMatrix();

        setupRenderState();
        renderParticles(matrix, particles, duration.getValue().doubleValue(), duration.min);
        resetRenderState();
    }

    private void renderParticles(MatrixStack matrix, List<Particle> particles, double lifetime, double duration) {
        removeExpiredParticles(particles, lifetime + duration);
        if (particles.isEmpty()) return;

        matrix.push();
        for (Particle particle : particles) {
            particle.update(physic.getValue());
            Animation animation = particle.animation();
            animation.update();
            float alpha = animation.get();

            if (alpha != opacity.getValue() && !particle.time().finished(duration)) {
                animation.run(opacity.getValue(), (duration / 1000), Easings.CUBIC_OUT, true);
            }
            if (alpha != 0.0F && particle.time().finished(lifetime)) {
                animation.run(0.0F, (duration / 1000), Easings.CUBIC_OUT, true);
            }

            int color = ColorUtil.multAlpha(ColorUtil.replAlpha(particle.color(), alpha), (float) ((Math.sin((System.currentTimeMillis() - particle.spawnTime()) / 200D) + 1F) / 2F));
            Vector3d vec = particle.position();
            float x = (float) vec.x;
            float y = (float) vec.y;
            float z = (float) vec.z;

            renderParticle(matrix, particle, x, y, z, color);
        }
        matrix.pop();
    }

    private void removeExpiredParticles(List<Particle> particles, double lifespan) {
        particles.removeIf(particle -> !PlayerUtil.isInView(particle.box));
        particles.removeIf(particle -> particle.time().finished(lifespan));
    }

    private void renderParticle(MatrixStack matrix, Particle particle, float x, float y, float z, int color) {
        float pos = particle.size;
        matrix.push();
        RenderUtil3D.setupOrientationMatrix(matrix, x, y, z);
        matrix.rotate(mc.getRenderManager().getCameraOrientation());
        matrix.push();
        matrix.rotate(Vector3f.ZP.rotationDegrees(180F));
        if (particle.type().rotatable()) matrix.rotate(Vector3f.ZP.rotationDegrees(particle.rotate()));
        matrix.push();
        matrix.translate(0, -pos, -pos);
        if (glowing.getValue()) {
            RenderUtil.bindTexture(ParticleType.BLOOM.texture());
            RectUtil.drawRect(matrix, -pos * 4, -pos * 4, pos * 8, pos * 8, ColorUtil.multAlpha(color, 0.1F), true, true);
        }
        RenderUtil.bindTexture(particle.type().texture());
        RectUtil.drawRect(matrix, -pos, -pos, pos * 2, pos * 2, color, true, true);
        if (particle.type.equals(ParticleType.BLOOM)) {
            RectUtil.drawRect(matrix, -pos / 2, -pos / 2, pos, pos, color, true, true);
        }
        matrix.pop();
        matrix.pop();
        matrix.pop();
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

    private void spawnParticle(Vector3d position, Vector3d velocity) {
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
                color,
                size,
                (int) Mathf.step(Mathf.randomValue(0, 360), 15))
        );
    }

    private boolean hasPlayerMoved() {
        return mc.player.lastTickPosX != mc.player.getPosX()
                || mc.player.lastTickPosY != mc.player.getPosY()
                || mc.player.lastTickPosZ != mc.player.getPosZ();
    }

    @Getter
    @Accessors(fluent = true)
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

        private final ResourceLocation texture;
        private final boolean rotatable;

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
    public static class Particle {
        private final long spawnTime = System.currentTimeMillis();
        private final ParticleType type;
        private final AxisAlignedBB box;
        private Vector3d position;
        private Vector3d velocity;
        private final int rotate;
        private final int index;
        private final int color;
        private final float size;

        private final StopWatch time = new StopWatch();
        private final Animation animation = new Animation();

        public Particle(ParticleType type, final Vector3d position, final Vector3d velocity, final int index, int color, float size, int rotate) {
            this.type = type;
            this.rotate = rotate;
            this.box = new AxisAlignedBB(position, position).grow(size);
            this.position = position;
            this.velocity = velocity.mul(0.01F);
            this.index = index;
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
