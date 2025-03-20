package org.obsidian.client.utils.render.particle;

import lombok.Data;
import net.minecraft.util.math.MathHelper;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.lib.util.time.StopWatch;

import java.util.List;

@Data
public class Particle {
    private float baseX, baseY, size, motionX, motionY, mass, gravity, gravityMultiplier;
    private float offsetX, offsetY;
    private boolean gravityEnabled, collisionEnabled, delayEnabled;
    private double delay;
    private ParticleQuad particleQuad;
    private final StopWatch time = new StopWatch();
    private final Animation animation = new Animation();

    public Particle(float baseX, float baseY, float size, float motionX, float motionY, float mass, float gravity, float gravityMultiplier, boolean gravityEnabled, boolean collisionEnabled, boolean delayEnabled, double delay) {
        this.baseX = baseX;
        this.baseY = baseY;
        this.size = size;
        this.motionX = motionX;
        this.motionY = motionY;
        this.mass = mass;
        this.particleQuad = new ParticleQuad(baseX, baseY, baseX + size, baseY + size);
        this.gravity = gravity;
        this.gravityMultiplier = gravityMultiplier;
        this.gravityEnabled = gravityEnabled;
        this.collisionEnabled = collisionEnabled;
        this.delayEnabled = delayEnabled;
        this.delay = delay;
        this.time.reset();
    }

    public Particle(float baseX, float baseY, float size, float motionX, float motionY, float mass, float gravity, float gravityMultiplier, boolean collisionEnabled, double delay) {
        this(baseX, baseY, size, motionX, motionY, mass, gravity, gravityMultiplier, true, collisionEnabled, true, delay);
    }

    public Particle(float baseX, float baseY, float size, float motionX, float motionY, float mass, float gravity, float gravityMultiplier, double delay) {
        this(baseX, baseY, size, motionX, motionY, mass, gravity, gravityMultiplier, true, false, true, delay);
    }

    public Particle(float baseX, float baseY, float size, float motionX, float motionY, float mass, float gravity, float gravityMultiplier, boolean collisionEnabled) {
        this(baseX, baseY, size, motionX, motionY, mass, gravity, gravityMultiplier, true, collisionEnabled, false, 0);
    }

    public Particle(float baseX, float baseY, float size, float motionX, float motionY, float mass) {
        this(baseX, baseY, size, motionX, motionY, mass, 0, 0, false, false, false, 0);
    }

    public Particle(float baseX, float baseY, float size, float motionX, float motionY, float mass, boolean collisionEnabled, double delay) {
        this(baseX, baseY, size, motionX, motionY, mass, 0, 0, false, collisionEnabled, true, delay);
    }

    public Particle(float baseX, float baseY, float size, float motionX, float motionY, float mass, double delay) {
        this(baseX, baseY, size, motionX, motionY, mass, 0, 0, false, false, true, delay);
    }

    public Particle(float baseX, float baseY, float size, float motionX, float motionY, float mass, boolean collisionEnabled) {
        this(baseX, baseY, size, motionX, motionY, mass, 0, 0, false, collisionEnabled, false, 0);
    }

    public Particle(float baseX, float baseY, float size, float motionX, float motionY, float mass, float gravity, float gravityMultiplier) {
        this(baseX, baseY, size, motionX, motionY, mass, gravity, gravityMultiplier, true, false, false, 0);
    }

    public void update(float deltaTime) {
        animation.update();
        if (gravityEnabled) {
            motionY += gravity * deltaTime;
            motionY *= 1 - deltaTime * gravityMultiplier;
            if (!collisionEnabled) motionX *= 1 - deltaTime * (1F - 0.9F);
        }

        offsetX += motionX * deltaTime;
        offsetY += motionY * deltaTime;

        this.getParticleQuad().set(baseX + offsetX, baseY + offsetY, baseX + offsetX + size, baseY + offsetY + size);
    }

    public void handleCollision(Particle other) {
        ParticleQuad thisBox = this.getParticleQuad();
        ParticleQuad otherBox = other.getParticleQuad();

        if (thisBox.intersects(otherBox)) {
            float dx = this.baseX - other.baseX;
            float dy = this.baseY - other.baseY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance == 0) return;

            float nx = dx / distance;
            float ny = dy / distance;

            float vx = this.motionX - other.motionX;
            float vy = this.motionY - other.motionY;
            float vn = vx * nx + vy * ny;

            if (vn > 0) return;

            float impulse = (2 * vn) / (this.mass + other.mass);

            this.motionX -= impulse * other.mass * nx;
            this.motionY -= impulse * other.mass * ny;
            other.motionX += impulse * this.mass * nx;
            other.motionY += impulse * this.mass * ny;
        }
    }

    public void handleBoundaryCollision(float minX, float minY, float maxX, float maxY) {
        ParticleQuad box = getParticleQuad();

        float multiply = -1;

        if (box.getLeft() < minX) {
            baseX = minX;
            motionX *= multiply;
        } else if (box.getRight() > maxX + (size / 2F)) {
            baseX = maxX - (size / 2F);
            motionX *= multiply;
        }

        if (box.getTop() < minY) {
            baseY = minY;
            motionY *= multiply;
        } else if (box.getBottom() > maxY + (size / 2F)) {
            baseY = maxY - (size / 2F);
            motionY *= multiply;
        }
    }

    public void handleShapeCollision(Polygon shape, boolean keepInside) {
        List<Polygon.Vec2f> vertices = shape.getVertices();
        int numVertices = vertices.size();

        for (int i = 0; i < numVertices; i++) {
            Polygon.Vec2f v1 = vertices.get(i);
            Polygon.Vec2f v2 = vertices.get((i + 1) % numVertices);

            if (lineIntersectsParticle(v1.getX(), v1.getY(), v2.getX(), v2.getY())) {
                handleCollisionWithLine(v1.getX(), v1.getY(), v2.getX(), v2.getY());
            }
            if (keepInside) keepParticleInsidePolygon(v1.getX(), v1.getY(), v2.getX(), v2.getY());
        }
    }

    public void handleShapeCollision(Polygon shape) {
        handleShapeCollision(shape, true);
    }

    private boolean lineIntersectsParticle(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float lengthSquared = dx * dx + dy * dy;
        float t = ((baseX - x1) * dx + (baseY - y1) * dy) / lengthSquared;

        if (t < 0.0f) t = 0.0f;
        else if (t > 1.0f) t = 1.0f;

        float nearestX = x1 + t * dx;
        float nearestY = y1 + t * dy;

        float distX = baseX - nearestX;
        float distY = baseY - nearestY;

        return Math.sqrt(distX * distX + distY * distY) <= Math.sqrt(size * size);
    }

    private void handleCollisionWithLine(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        dx /= length;
        dy /= length;

        float normalX = -dy;
        float normalY = dx;

        float dot = motionX * normalX + motionY * normalY;

        motionX -= 2 * dot * normalX;
        motionY -= 2 * dot * normalY;
    }

    private void keepParticleInsidePolygon(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float lengthSquared = dx * dx + dy * dy;
        float t = ((baseX - x1) * dx + (baseY - y1) * dy) / lengthSquared;

        if (t < 0.0f) t = 0.0f;
        else if (t > 1.0f) t = 1.0f;

        float nearestX = x1 + t * dx;
        float nearestY = y1 + t * dy;

        float distX = baseX - nearestX;
        float distY = baseY - nearestY;
        float distance = (float) Math.sqrt(distX * distX + distY * distY);

        if (distance < size / 2) {
            baseX += distX / (distance * distance);
            baseY += distY / (distance * distance);
        }
    }

    public void set(float x, float y, float size) {
        this.baseX = x;
        this.baseY = y;
        this.size = size;
        this.getParticleQuad().set(x + offsetX, y + offsetY, x + offsetX + size, y + offsetY + size);
    }

    public float getBaseX() {
        return baseX + offsetX;
    }

    public float getBaseY() {
        return baseY + offsetY;
    }

    public boolean isFinished() {
        return delayEnabled && time.finished(delay);
    }

    public float getTimePC(long maxMS) {
        return MathHelper.clamp(time.elapsedTime() / (float) maxMS, 0.F, 1.F);
    }
}
