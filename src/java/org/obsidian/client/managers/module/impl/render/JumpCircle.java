package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.IRenderCall;
import net.mojang.blaze3d.systems.RenderSystem;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.api.interfaces.IRender;
import org.obsidian.client.managers.events.player.JumpEvent;
import org.obsidian.client.managers.events.render.Render3DLastEvent;
import org.obsidian.client.managers.events.world.WorldChangeEvent;
import org.obsidian.client.managers.events.world.WorldLoadEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RenderUtil3D;
import org.obsidian.lib.util.time.StopWatch;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14C;

import java.util.ArrayList;
import java.util.List;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "JumpCircle", category = Category.RENDER)
public class JumpCircle extends Module implements IRender {
    public static JumpCircle getInstance() {
        return Instance.get(JumpCircle.class);
    }

    private final SliderSetting lifetime = new SliderSetting(this, "Время жизни", 1500, 500, 3000, 100);
    private final SliderSetting radius = new SliderSetting(this, "Радиус", 1F, 0.5F, 3F, 0.1F);
    private final SliderSetting margin = new SliderSetting(this, "Отступ", 3, 1, 5, 1);
    private final SliderSetting opacity = new SliderSetting(this, "Прозрачность", 1F, 0.05F, 1F, 0.05F);
    private final static List<Circle> circles = new ArrayList<>();

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
    public void onEvent(WorldLoadEvent event) {
        clear();
    }

    @EventHandler
    public void onEvent(JumpEvent event) {
        addCircle(mc.player);
    }

    @EventHandler
    public void onEvent(Render3DLastEvent event) {
        if (circles.isEmpty()) return;
        circles.removeIf(this::finished);
        if (circles.isEmpty()) return;
        final MatrixStack matrix = event.getMatrix();
        RenderUtil3D.setupWorldRenderer();
        Vector3d cameraPos = RenderUtil3D.cameraPos();
        matrix.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        setupDraw(matrix, () -> circles.forEach(circle -> drawCircle(matrix, circle)));
        matrix.translate(cameraPos.x, cameraPos.y, cameraPos.z);
        RenderUtil3D.cleanupWorldRenderer();
    }

    private void drawCircle(final MatrixStack matrix, final Circle circle) {
        circle.animation.update();

        final float percent = circle.time.elapsedTime() / lifetime.getValue();
        final boolean finished = percent >= 0.5F;
        circle.animation.run(finished ? 0 : 1, finished ? (this.lifetime.getValue() / 1000) : (this.lifetime.getValue() / 1000) / 2D, finished ? Easings.SINE_IN_OUT : Easings.SINE_OUT, true);

        final float radius = finished ? this.radius.getValue() : circle.animation.get() * this.radius.getValue();

        final long elapsedTime = circle.time.elapsedTime();
        final double animationTime = 1500; // ms
        final double rotate = (elapsedTime % animationTime) / animationTime;

        matrix.push();

        matrix.translate(circle.pos.x, 0, circle.pos.z);
        matrix.rotate(Vector3f.YP.rotationDegrees(((float) rotate * (((360F / margin.getValue().intValue())) * (margin.getValue().intValue())))));
        matrix.translate(-circle.pos.x, 0, -circle.pos.z);

        double pi2 = MathHelper.PI2;

        double xVal = circle.pos.x;
        double yVal = circle.pos.y;
        double zVal = circle.pos.z;

        Matrix4f matrix4f = matrix.getLast().getMatrix();

        float mult = 1.25F;
        float newAnim = radius * mult + (finished ? (1F - circle.animation.get()) : 0);

        int first = Theme.getInstance().clientColor();
        int second = ColorUtil.multDark(Theme.getInstance().clientColor(), 0.25F);

        BUFFER.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 90; ++i) {
            float value = (float) Math.sin((i * (margin.getValue().intValue() * 2) * 2) * (MathHelper.PI / 180));
            int color = ColorUtil.overCol(first, second, Math.abs(value));
            BUFFER.pos(matrix4f, (float) (xVal + radius * Math.cos(i * pi2 / 45)), (float) yVal, (float) (zVal + radius * Math.sin(i * pi2 / 45))).color(0).endVertex();
            BUFFER.pos(matrix4f, (float) (xVal + newAnim * Math.cos(i * pi2 / 45)), (float) yVal, (float) (zVal + newAnim * Math.sin(i * pi2 / 45))).color(ColorUtil.multAlpha(color, opacity.getValue() * (circle.animation.get() * circle.animation.get()))).endVertex();
        }
        TESSELLATOR.draw();

        BUFFER.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 90; ++i) {
            float value = (float) Math.sin((i * (margin.getValue().intValue() * 2) * 2) * (MathHelper.PI / 180));
            int color = ColorUtil.overCol(first, second, Math.abs(value));
            BUFFER.pos(matrix4f, (float) (xVal + newAnim * Math.cos(i * pi2 / 45) * mult), (float) yVal, (float) (zVal + newAnim * Math.sin(i * pi2 / 45) * mult)).color(0).endVertex();
            BUFFER.pos(matrix4f, (float) (xVal + newAnim * Math.cos(i * pi2 / 45)), (float) yVal, (float) (zVal + newAnim * Math.sin(i * pi2 / 45))).color(ColorUtil.multAlpha(color, opacity.getValue() * (circle.animation.get() * circle.animation.get()))).endVertex();
        }
        TESSELLATOR.draw();

        matrix.pop();
    }

    private void addCircle(final Entity entity) {
        Vector3d vec = getEntityPosition(entity).add(0.D, .005D, 0.D);
        final BlockPos pos = new BlockPos(vec);
        final BlockState state = mc.world.getBlockState(pos);
        if (state.getBlock() == Blocks.SNOW) {
            vec = vec.add(0.D, .125D, 0.D);
        }
        circles.add(new Circle(vec, circles.size()));
    }

    private Vector3d getEntityPosition(final Entity entity) {
        return RenderUtil3D.interpolate(entity, mc.getRenderPartialTicks());
    }

    private void clear() {
        if (!circles.isEmpty()) circles.clear();
    }

    private boolean finished(final Circle circle) {
        return circle.time().finished(lifetime.getValue().intValue() * 2F);
    }

    private void setupDraw(final MatrixStack matrix, final IRenderCall render) {
        final boolean light = GL11.glIsEnabled(GL11.GL_LIGHTING);
        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.alphaFunc(GL14C.GL_GREATER, 0f);
        RenderSystem.depthMask(false);
        RenderSystem.disableCull();
        if (light) RenderSystem.disableLighting();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        RenderSystem.blendFunc(GL14C.GL_SRC_ALPHA, GL14C.GL_ONE_MINUS_CONSTANT_ALPHA);

        render.execute();

        RenderSystem.blendFunc(GL14C.GL_SRC_ALPHA, GL14C.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.clearCurrentColor();
        RenderSystem.shadeModel(GL11.GL_FLAT);
        if (light) RenderSystem.enableLighting();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.alphaFunc(GL14C.GL_GREATER, 0.1F);
        RenderSystem.enableAlphaTest();
        matrix.pop();
    }

    @Getter
    @RequiredArgsConstructor
    private static final class Circle {
        private final StopWatch time = new StopWatch();
        private final Animation animation = new Animation();
        private final Vector3d pos;
        private final int index;
    }
}