package org.obsidian.client.utils.render.shader.glsl;

import org.obsidian.client.utils.render.shader.IShader;

public class KawaseDownBloomGlsl implements IShader {
    @Override
    public String shader() {
        return """
                #version 120

                uniform sampler2D inTexture;
                uniform vec2 offset, halfpixel, resolution;

                void main() {
                    vec2 uv = gl_FragCoord.xy / resolution;
                    vec4 sum = texture2D(inTexture, gl_TexCoord[0].st);
                    sum.rgb *= sum.a;
                    sum *= 4.0;

                    vec2 offsets[4] = vec2[](
                        vec2(-1.0, 1.0),
                        vec2(1.0, 1.0),
                        vec2(1.0, -1.0),
                        vec2(-1.0, -1.0)
                    );

                    for (int i = 0; i < 4; ++i) {
                        vec2 sampleUV = uv + offsets[i] * halfpixel * offset;
                        vec4 smp = texture2D(inTexture, sampleUV);
                        smp.rgb *= smp.a;
                        sum += smp;
                    }

                    vec4 result = sum / 8.0;
                    gl_FragColor = vec4(result.rgb / max(result.a, 0.001), result.a);
                }""";
    }
}
