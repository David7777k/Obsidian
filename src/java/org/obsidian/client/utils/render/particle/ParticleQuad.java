package org.obsidian.client.utils.render.particle;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParticleQuad {
    private float left;
    private float top;
    private float right;
    private float bottom;

    public void set(float left, float top, float right, float bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public boolean intersects(ParticleQuad other) {
        return this.left < other.right
                && this.right > other.left
                && this.top < other.bottom
                && this.bottom > other.top;
    }

    public boolean contains(ParticleQuad other) {
        return this.left <= other.left
                && this.right >= other.right
                && this.top <= other.top
                && this.bottom >= other.bottom;
    }

    public boolean isInside(ParticleQuad other) {
        return other.contains(this);
    }

    public boolean touches(ParticleQuad other) {
        return this.left == other.right
                || this.right == other.left
                || this.top == other.bottom
                || this.bottom == other.top;
    }

    public float[] getCenter() {
        return new float[]{(left + right) / 2, (top + bottom) / 2};
    }

    public ParticleQuad expand(float amount) {
        return new ParticleQuad(left - amount, top - amount, right + amount, bottom + amount);
    }

    public ParticleQuad contract(float amount) {
        return new ParticleQuad(left + amount, top + amount, right - amount, bottom - amount);
    }

    public ParticleQuad translate(float dx, float dy) {
        return new ParticleQuad(left + dx, top + dy, right + dx, bottom + dy);
    }

    public ParticleQuad union(ParticleQuad other) {
        return new ParticleQuad(
                Math.min(this.left, other.left),
                Math.min(this.top, other.top),
                Math.max(this.right, other.right),
                Math.max(this.bottom, other.bottom)
        );
    }

    public ParticleQuad intersection(ParticleQuad other) {
        if (!this.intersects(other)) {
            return null;
        }
        return new ParticleQuad(
                Math.max(this.left, other.left),
                Math.max(this.top, other.top),
                Math.min(this.right, other.right),
                Math.min(this.bottom, other.bottom)
        );
    }

    public boolean equals(ParticleQuad other) {
        return this.left == other.left
                && this.top == other.top
                && this.right == other.right
                && this.bottom == other.bottom;
    }

    public float area() {
        return (right - left) * (bottom - top);
    }

    public float perimeter() {
        return 2 * ((right - left) + (bottom - top));
    }

    public boolean isEmpty() {
        return this.left == this.right || this.top == this.bottom;
    }

    public ParticleQuad scale(float scaleFactor) {
        float centerX = (left + right) / 2;
        float centerY = (top + bottom) / 2;
        float halfWidth = (right - left) / 2 * scaleFactor;
        float halfHeight = (bottom - top) / 2 * scaleFactor;
        return new ParticleQuad(centerX - halfWidth, centerY - halfHeight, centerX + halfWidth, centerY + halfHeight);
    }

    public ParticleQuad inflate(float amount) {
        return new ParticleQuad(left - amount, top - amount, right + amount, bottom + amount);
    }

    public ParticleQuad deflate(float amount) {
        return new ParticleQuad(left + amount, top + amount, right - amount, bottom - amount);
    }

    public boolean containsPoint(float x, float y) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }

    public float getWidth() {
        return right - left;
    }

    public float getHeight() {
        return bottom - top;
    }

    public boolean intersectsWithMargin(ParticleQuad other, float margin) {
        return this.left < other.right + margin
                && this.right > other.left - margin
                && this.top < other.bottom + margin
                && this.bottom > other.top - margin;
    }

    public float[][] getCorners() {
        return new float[][]{
                {left, top}, {right, top}, {right, bottom}, {left, bottom}
        };
    }

    public ParticleQuad resize(float newWidth, float newHeight) {
        float centerX = (left + right) / 2;
        float centerY = (top + bottom) / 2;
        float halfWidth = newWidth / 2;
        float halfHeight = newHeight / 2;
        return new ParticleQuad(centerX - halfWidth, centerY - halfHeight, centerX + halfWidth, centerY + halfHeight);
    }

    public static ParticleQuad getBoundingBoxForPoints(float[][] points) {
        if (points == null || points.length == 0) {
            throw new IllegalArgumentException("Points array must not be null or empty");
        }
        float minX = points[0][0];
        float minY = points[0][1];
        float maxX = points[0][0];
        float maxY = points[0][1];
        for (float[] point : points) {
            if (point[0] < minX) minX = point[0];
            if (point[1] < minY) minY = point[1];
            if (point[0] > maxX) maxX = point[0];
            if (point[1] > maxY) maxY = point[1];
        }
        return new ParticleQuad(minX, minY, maxX, maxY);
    }
}