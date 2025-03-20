package org.obsidian.client.utils.render.shader.glsl;

import org.obsidian.client.utils.render.shader.IShader;

public class EntityChamsGlsl implements IShader {
    @Override
    public String shader() {
        return """
                #version 120
                                        
                uniform vec2 location, rectSize;
                uniform sampler2D tex;
                uniform vec4 color;
                                        
                void main() {
                    vec2 coords = (gl_FragCoord.xy - location) / rectSize;
                    float texColorAlpha = texture2D(tex, gl_TexCoord[0].st).a;
                    gl_FragColor = vec4(color.rgb, texColorAlpha);
                }""";
    }
}
