package org.obsidian.client.utils.render.particle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Getter
public class Polygon {
    private final List<Vec2f> vertices;

    public Polygon(List<Vec2f> vertices) {
        this.vertices = vertices;
    }

    @Data
    @AllArgsConstructor
    public static class Vec2f {
        private final float x, y;
    }
}
