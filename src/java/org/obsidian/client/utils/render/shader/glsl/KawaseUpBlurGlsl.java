package org.obsidian.client.utils.render.shader.glsl;

import org.obsidian.client.utils.render.shader.IShader;

public class KawaseUpBlurGlsl implements IShader {
    @Override
    public String shader() {
        return """
                #version 120

                uniform sampler2D inTexture, textureToCheck;
                uniform vec2 halfpixel, offset, resolution;
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
                        sum += texture2D(inTexture, sampleUV) * weights[i];
                    }

                    gl_FragColor = vec4(sum.rgb / 12.0, mix(1.0, texture2D(textureToCheck, gl_TexCoord[0].st).a, check));
                }""";
    }
}
