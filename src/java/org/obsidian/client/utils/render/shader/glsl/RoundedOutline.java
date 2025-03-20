package org.obsidian.client.utils.render.shader.glsl;

import org.obsidian.client.utils.render.shader.IShader;

public class RoundedOutline implements IShader {
    @Override
    public String shader() {
        return """
                #version 120
                
                uniform vec4 color1;
                uniform vec4 color2;
                uniform vec4 color3;
                uniform vec4 color4;
                
                uniform vec2 size;
                uniform vec4 round;
                uniform float value;
                uniform vec2 smoothness;
                uniform vec2 softness;
                uniform vec2 thickness;
                
                float roundedBox(vec2 center, vec2 size, vec4 radius) {
                    radius.xy = (center.x > 0.0) ? radius.xy : radius.zw;
                    radius.x  = (center.y > 0.0) ? radius.x : radius.y;
                
                    vec2 q = abs(center) - size + radius.x;
                    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius.x;
                }
                
                vec4 createGradient(vec2 pos) {
                    return mix(mix(color1, color2, pos.y), mix(color3, color4, pos.y), pos.x);
                }
                
                void main() {
                    vec2 tex = (abs(gl_TexCoord[0].st - 0.5) + 0.5) * size;
                
                    float distance = roundedBox(tex - (size / 2.0), (size / 2.0) - value, round);
                
                    float smoothedAlpha = (1.0 - smoothstep(smoothness.x, smoothness.y, distance));
                    float smoothedborderAlpha = (1.0 - smoothstep(softness.x, softness.y, distance));
                    float borderAlpha = (1.0 - smoothstep(thickness.x, thickness.y, abs(distance)));
                
                    vec4 gradient = createGradient(gl_TexCoord[0].st);
                
                    if (smoothedAlpha > 0.5) {
                        gl_FragColor = vec4(gradient.rgb, gradient.a * smoothedAlpha * borderAlpha);
                    } else {
                        gl_FragColor = vec4(gradient.rgb, gradient.a * smoothedborderAlpha * borderAlpha);
                    }
                }""";
    }
}
