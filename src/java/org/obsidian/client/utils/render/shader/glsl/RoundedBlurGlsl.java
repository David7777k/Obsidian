package org.obsidian.client.utils.render.shader.glsl;

import org.obsidian.client.utils.render.shader.IShader;

public class RoundedBlurGlsl implements IShader {
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
                uniform vec2 resolution;
                                
                #define NOISE_CONSTANT 0.5/255.0
                                
                float roundedBoxSDF(vec2 center, vec2 size, vec4 radius) {
                    radius.xy = (center.x > 0.0) ? radius.xy : radius.zw;
                    radius.x  = (center.y > 0.0) ? radius.x : radius.y;
                                
                    vec2 q = abs(center) - size + radius.x;
                    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius.x;
                }
                                
                void main() {
                    vec2 pos = gl_FragCoord.xy;
                    vec2 blurredPos = pos / resolution;
                    vec3 blurredColor = texture2D(textureIn, blurredPos).rgb;
                    
                    vec2 textureCoordinates = gl_TexCoord[0].st * size;
                    
                    float sampleAlpha = 1.0 - smoothstep(smoothness.x, smoothness.y, roundedBoxSDF(textureCoordinates - (size / 2.0), (size / 2.0) - value, round));
                    gl_FragColor = vec4(blurredColor, sampleAlpha * alpha);
                }""";
    }
}
