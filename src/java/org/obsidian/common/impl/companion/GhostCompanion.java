package org.obsidian.common.impl.companion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Namespaced;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.gen.Heightmap;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import net.mojang.blaze3d.systems.RenderSystem;
import org.obsidian.client.api.events.Event;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.api.interfaces.IRender;
import org.obsidian.client.managers.events.render.Render3DLastEvent;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.math.Interpolator;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.math.Wave;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RenderUtil3D;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.lwjgl.opengl.GL11;

@Data
@Accessors(fluent = true)
public class GhostCompanion implements IMinecraft, IRender {
    private final Vector3d position = new Vector3d();
    private final Vector3d destination = new Vector3d();
    private final Vector3d motion = new Vector3d();
    private static float size = 0.5F;
    private float yaw;
    private float pitch;
    private final ResourceLocation texture = new Namespaced("texture/ghost.png");
    private State currentState = State.IDLE;

    public void updatePosition() {
        position.add(motion);
        motion.mul(0.9);
    }

    public void moveTo(Vector3d targetPosition) {
        Vector3d direction = new Vector3d(targetPosition).sub(position);
        double distance = direction.length();

        if (distance < 10) {
            motion.mul(0.8);
        }

        direction.normalize().mul(Mathf.clamp01(distance * 0.01));
        motion.lerp(direction, Easings.SINE_OUT.ease(Mathf.clamp01(Math.min(distance * 0.03, 0.25))));
    }

    public void followDestination() {
        double dist = mc.player.getDistanceVec(position.x, position.y, position.z);
        if (currentState.equals(State.IDLE) && dist > 32) {
            currentState = State.MOVE_TO_PLAYER;
        } else if (currentState.equals(State.MOVE_TO_PLAYER) && dist < 5) {
            currentState = State.IDLE;
        }
        boolean check = currentState.equals(State.MOVE_TO_PLAYER);

        float distance = Wave.cosWave(0.5F, 1483) + Wave.sinWave(0.5F, 2689) + Wave.cosWave(0.5F, 782);

        double yawRad = Math.toRadians(Wave.sinWave(360F, 8423) - Wave.cosWave(45F, 6875) + Wave.cosWave(15F, 834));
        double xOffset = -Math.sin(yawRad) * distance;
        double zOffset = Math.cos(yawRad) * distance;
        double x = position.x + xOffset;
        double z = position.z + zOffset;
        BlockPos pos = mc.world.getHeight(Heightmap.Type.MOTION_BLOCKING, new BlockPos(x, 0, z));

        double y = pos.getY() + 0.5F + Wave.cosWave(5 - Wave.sinWave(1, 3561), 19250) + Wave.sinWave(Wave.cosWave(0.5, 1856), 4865);
        setDestination(x, y, z);
        moveTo(check ? mc.player.getCustomPositionVector().add(0, 0.5F, 0) : destination);
    }

    public void onEvent(Event event) {
        if (event instanceof Render3DLastEvent render) {
            updateRotation();
            followDestination();
            updatePosition();

            final MatrixStack matrix = render.getMatrix();

            matrix.push();
            Vector3d vec = new Vector3d(RenderUtil3D.cameraPos().x, RenderUtil3D.cameraPos().y, RenderUtil3D.cameraPos().z).mul(-1).add(position.x, position.y, position.z);
            matrix.translate(vec.x, vec.y, vec.z);
            matrix.rotate(Vector3f.YP.rotationDegrees(-yaw));
            matrix.rotate(Vector3f.XP.rotationDegrees(pitch));
            matrix.translate(-vec.x, -vec.y, -vec.z);

            matrix.push();
            RenderUtil3D.setupOrientationMatrix(matrix, position.x, position.y, position.z);
            drawModel(matrix);
            matrix.pop();

            matrix.pop();
        }
    }

    private void updateRotation() {
        float targetYaw = calculateYaw(next().x, next().z);
        float targetPitch = calculatePitch(next().x, next().y, next().z);
        float yawDiff = MathHelper.wrapDegrees(targetYaw - yaw);
        float yawStep = 10;

        if (Math.abs(yawDiff) > yawStep) {
            yaw = Interpolator.lerp(yaw, yaw + Math.copySign(yawStep, yawDiff), 0.1F);
        } else {
            yaw = targetYaw % 360;
        }

        pitch = Interpolator.lerp(pitch, targetPitch, 0.1F);
        pitch = Mathf.clamp(-90, 90, pitch);
    }

    private float calculateYaw(double x, double z) {
        return (float) (MathHelper.atan2(z - position.z, x - position.x) * (180 / Math.PI)) - 90.0F;
    }

    private float calculatePitch(double x, double y, double z) {
        double xDist = x - position.x;
        double yDist = y - position.y;
        double zDist = z - position.z;
        double horizontalDist = MathHelper.sqrt(xDist * xDist + zDist * zDist);
        return (float) (-(MathHelper.atan2(yDist, horizontalDist) * (180 / Math.PI)));
    }

    public void setDestination(Vector3d vec) {
        setDestination(vec.x, vec.y, vec.z);
    }

    public void setDestination(double x, double y, double z) {
        destination.set(x, y, z);
    }

    private Vector3d next() {
        return new Vector3d(position).add(motion);
    }

    private void drawModel(MatrixStack matrix) {
        boolean light = GL11.glIsEnabled(GL11.GL_LIGHTING);

        int headColor = ColorUtil.getColor(255, 1F);

        mc.getTextureManager().bindTexture(texture);
        startRender(matrix, light);
        for (TextureCube value : TextureCube.values()) {
            final TextureSide side = value.side;
            final Vector3d origin = side.origin;

            matrix.push();
            matrix.rotate(side.rotation);
            drawSide(matrix, origin.x, origin.y, origin.z, side.size.x, side.size.y, side, headColor);
            matrix.pop();
        }
        endRender(matrix, light);
    }

    public void drawSide(MatrixStack matrix, double x, double y, double z, double width, double height, TextureSide side, int color) {
        float[] uv = calculateUV(side);
        Matrix4f matrix4f = matrix.getLast().getMatrix();
        BUFFER.begin(GlStateManager.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        BUFFER.pos(matrix4f, (float) x, (float) (y + height), (float) z).tex(uv[0], uv[1]).color(color).endVertex();
        BUFFER.pos(matrix4f, (float) (x + width), (float) (y + height), (float) z).tex(uv[2], uv[1]).color(color).endVertex();
        BUFFER.pos(matrix4f, (float) (x + width), (float) y, (float) z).tex(uv[2], uv[3]).color(color).endVertex();
        BUFFER.pos(matrix4f, (float) x, (float) y, (float) z).tex(uv[0], uv[3]).color(color).endVertex();
        TESSELLATOR.draw();
    }

    private float[] calculateUV(TextureSide textureSide) {
        float u1 = textureSide.x / textureSide.textureWidth;
        float v1 = textureSide.y / textureSide.textureHeight;
        float u2 = (textureSide.x + textureSide.width) / textureSide.textureWidth;
        float v2 = (textureSide.y + textureSide.height) / textureSide.textureHeight;

        return new float[]{u1, v1, u2, v2};
    }

    private void startRender(MatrixStack matrix, boolean light) {
        RenderSystem.pushMatrix();
        matrix.push();
        RenderSystem.enableBlend();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableCull();
        if (light) RenderSystem.disableLighting();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    private void endRender(MatrixStack matrix, boolean light) {
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.clearCurrentColor();
        GL11.glShadeModel(GL11.GL_FLAT);
        if (light) RenderSystem.enableLighting();
        RenderSystem.enableCull();
        RenderSystem.enableAlphaTest();
        matrix.pop();
        RenderSystem.popMatrix();
    }

    public enum State {
        IDLE, MOVE_TO_PLAYER
    }

    @RequiredArgsConstructor
    public enum TextureCube {
        TOP(new TextureSide(36, 22, 7, 0, 7, 7, Vector3f.XN.rotationDegrees(90), new Vector3d(-(size / 2), -(size / 2), size / 2), new Vector2f(size))),
        BOTTOM(new TextureSide(36, 22, 14, 0, 7, 7, Vector3f.XN.rotationDegrees(90), new Vector3d(-(size / 2), -(size / 2), -(size / 2)), new Vector2f(size))),
        RIGHT(new TextureSide(36, 22, 0, 7, 7, 7, Vector3f.YP.rotationDegrees(90), new Vector3d(-(size / 2), -(size / 2), -(size / 2)), new Vector2f(size))),
        LEFT(new TextureSide(36, 22, 14, 7, 7, 7, Vector3f.YN.rotationDegrees(90), new Vector3d(-(size / 2), -(size / 2), -(size / 2)), new Vector2f(size))),
        FRONT(new TextureSide(36, 22, 7, 7, 7, 7, new Quaternion(), new Vector3d(-(size / 2), -(size / 2), size / 2), new Vector2f(size))),
        BACK(new TextureSide(36, 22, 21, 7, 7, 7, new Quaternion(), new Vector3d(-(size / 2), -(size / 2), -(size / 2)), new Vector2f(size))),

        EYES(new TextureSide(36, 22, 21, 5, 5, 2, new Quaternion(), new Vector3d(-(calcSize(7, 7, size, size, 5, 2).x / 2F), -(size / 2) + calcSize(7, 7, size, size, 5, 2).y / 2F, size / 2 + 0.01F), calcSize(7, 7, size, size, 5, 2))),

        LEG_RIGHT(new TextureSide(36, 22, 0, 21, 7, 1, Vector3f.YP.rotationDegrees(90), new Vector3d(-(size / 2), -size / 2 - TextureCube.calcSize(7, 7, 0.5F, 0.5F, 7, 1).y, -(size / 2)), new Vector2f(size, TextureCube.calcSize(7, 7, 0.5F, 0.5F, 7, 1).y))),
        LEG_LEFT(new TextureSide(36, 22, 14, 21, 7, 1, Vector3f.YN.rotationDegrees(90), new Vector3d(-(size / 2), -size / 2 - TextureCube.calcSize(7, 7, 0.5F, 0.5F, 7, 1).y, -(size / 2)), new Vector2f(size, TextureCube.calcSize(7, 7, 0.5F, 0.5F, 7, 1).y))),
        LEG_FRONT(new TextureSide(36, 22, 7, 21, 7, 1, new Quaternion(), new Vector3d(-(size / 2), -size / 2 - TextureCube.calcSize(7, 7, 0.5F, 0.5F, 7, 1).y, size / 2), new Vector2f(size, TextureCube.calcSize(7, 7, 0.5F, 0.5F, 7, 1).y))),
        LEG_BACK(new TextureSide(36, 22, 21, 21, 7, 1, new Quaternion(), new Vector3d(-(size / 2), -size / 2 - TextureCube.calcSize(7, 7, 0.5F, 0.5F, 7, 1).y, -(size / 2)), new Vector2f(size, TextureCube.calcSize(7, 7, 0.5F, 0.5F, 7, 1).y))),

        ;

        private final TextureSide side;

        public static Vector2f calcSize(float parentWidth, float parentHeight, float sizeX, float sizeY, float width, float height) {
            float scaleFactorX = sizeX / parentWidth;
            float scaleFactorY = sizeY / parentHeight;

            float scaledWidth = width * scaleFactorX;
            float scaledHeight = height * scaleFactorY;

            return new Vector2f(scaledWidth, scaledHeight);
        }
    }

    @Data
    @Accessors(fluent = true)
    @AllArgsConstructor
    public static class TextureSide {
        private int textureWidth;
        private int textureHeight;
        private float x, y, width, height;
        private Quaternion rotation;
        private Vector3d origin;
        private Vector2f size;
    }
}
