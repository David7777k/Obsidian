package org.obsidian.client.utils.render.shader.glsl;

import org.obsidian.client.utils.render.shader.IShader;

public class RoundedTextureGlsl implements IShader {
    @Override
    public String shader() {
        return """
                #version 120
                
                uniform sampler2D textureIn;
                uniform vec2 size;
                uniform vec4 round;
                uniform vec2 smoothness;
                uniform float value;
                uniform float alpha;
                
                float roundedBox(vec2 center, vec2 size, vec4 radius) {
                    radius.xy = (center.x > 0.0) ? radius.xy : radius.zw;
                    radius.x  = (center.y > 0.0) ? radius.x : radius.y;
                
                    vec2 q = abs(center) - size + radius.x;
                    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius.x;
                }
                
                void main() {
                    vec2 tex = gl_TexCoord[0].st * size;
                    float smoothedAlpha = 1.0 - smoothstep(smoothness.x, smoothness.y, roundedBox(tex - (size / 2.0), (size / 2.0) - value, round));
                    gl_FragColor = vec4(texture2D(textureIn, gl_TexCoord[0].st).rgb, smoothedAlpha * alpha);
                }""";
    }

}