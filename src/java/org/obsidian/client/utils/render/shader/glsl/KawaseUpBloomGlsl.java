package org.obsidian.client.utils.render.shader.glsl;

import org.obsidian.client.utils.render.shader.IShader;

public class KawaseUpBloomGlsl implements IShader {
    @Override
    public String shader() {
        return """
                #version 120

                uniform sampler2D inTexture, textureToCheck;
                uniform vec2 halfpixel, offset, resolution;
                uniform float divider;

                uniform int check;

                void main() {
                    vec2 uv = gl_FragCoord.xy / resolution;
                    vec4 sum = vec4(0.0);
                    vec2 offsets[8] = vec2[](
                        vec2(-2.0, 0.0),
                        vec2(-1.0, 1.0),
                        vec2(0.0, 2.0),
                        vec2(1.0, 1.0),
                        vec2(2.0, 0.0),
                        vec2(1.0, -1.0),
                        vec2(0.0, -2.0),
                        vec2(-1.0, -1.0)
                    );
                    float weights[8] = float[](1.0, 2.0, 1.0, 2.0, 1.0, 2.0, 1.0, 2.0);

                    for (int i = 0; i < 8; ++i) {
                        vec2 sampleUV = uv + offsets[i] * halfpixel * offset;
                        vec4 smp = texture2D(inTexture, sampleUV);
                        smp.rgb *= smp.a;
                        sum += smp * weights[i];
                    }
                    
                    vec4 result = sum / divider; // 12.0
                    gl_FragColor = vec4(result.rgb / max(result.a, 0.001), mix(result.a, result.a * (1.0 - texture2D(textureToCheck, gl_TexCoord[0].st).a), check));
                }""";
    }
}
