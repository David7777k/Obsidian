package org.obsidian.client.utils.render.shader.glsl;


import org.obsidian.client.utils.render.shader.IShader;

public class FontGlsl implements IShader {

    @Override
    public String shader() {
        return """
                #version 120

                uniform sampler2D image;
                uniform vec2 textureSize;
                uniform float range;
                uniform vec2 edgeStrength;
                uniform float thickness;
                uniform bool outline;
                uniform float outlineThickness;
                uniform vec4 outlineColor;
                                
                varying vec4 VertexColor;

                float median(float red, float green, float blue) {
                  return max(min(red, green), min(max(red, green), blue));
                }

                void main() {
                    vec4 texColor = texture2D(image, gl_TexCoord[0].st);

                    float dx = dFdx(gl_TexCoord[0].x) * textureSize.x;
                    float dy = dFdy(gl_TexCoord[0].y) * textureSize.y;
                    float toPixels = range * inversesqrt(dx * dx + dy * dy);

                    float sigDist = median(texColor.r, texColor.g, texColor.b) - 0.5 + thickness;

                    float alpha = smoothstep(edgeStrength.x, edgeStrength.y, sigDist * toPixels);
                    if (outline) {
                        float outlineAlpha = smoothstep(edgeStrength.x, edgeStrength.y, (sigDist + outlineThickness) * toPixels) - alpha;
                        float finalAlpha = alpha * VertexColor.a + outlineAlpha * outlineColor.a;

                        gl_FragColor = vec4(mix(outlineColor.rgb, VertexColor.rgb, alpha), finalAlpha);
                        return;
                    }
                    gl_FragColor = vec4(VertexColor.rgb, VertexColor.a * alpha);
                }""";
    }
}
