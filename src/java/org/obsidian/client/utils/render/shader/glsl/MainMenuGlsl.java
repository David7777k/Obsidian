package org.obsidian.client.utils.render.shader.glsl;

import org.obsidian.client.utils.render.shader.IShader;

public class MainMenuGlsl implements IShader {
    @Override
    public String shader() {
        return """
                #version 120
                                
                #ifdef GL_ES
                precision mediump float;
                #endif
                                
                uniform float time;
                uniform float alpha;
                uniform vec4 color;
                uniform vec2 resolution;
                                
                vec2 dir = vec2(-0.7,0.3);
                                
                                
                float value_noise(in vec2 uv)
                {
                    const float k = 257.0;
                    vec4 l  = vec4(floor(uv),fract(uv));
                    float u = l.x + l.y * k;
                    vec4 v  = vec4(u, u+1.0,u+k, u+k+1.0);
                    v       = fract(fract(v*1.23456789)*9.18273645*v);
                    l.zw    = l.zw*l.zw*(3.0-2.0*l.zw);
                    l.x     = mix(v.x, v.y, l.z);
                    l.y     = mix(v.z, v.w, l.z);
                    return    mix(l.x, l.y, l.w);
                }
                                
                mat2 rmat(float t)
                {
                	float c = cos(t);
                	float s = sin(t);
                	return mat2(c, s, -s, c);	
                }
                                
                float fbm(float a, float f, vec2 uv, const int it)
                {
                    float n = 0.0;
                                
                    vec2 p = dir * time;
                    mat2 rm = rmat(0.3);
                	
                    for(int i = 0; i < 32; i++)
                    {
                        if(i<it)
                        {
                            n += value_noise(uv*f+p)*a;
                            a *= 0.5;
                            f *= 2.0;
                	    p *= rm;
                        }
                        else
                        {
                            break;
                        }
                    }
                    return n;
                }
                                
                vec2 center_and_correct_aspect_ratio(vec2 uv)
                {
                	uv = uv * 2.0 - 1.0;
                	uv.x *= resolution.x/resolution.y;
                	return uv;
                }
                                
                void main( void ) {
                                
                	vec2 uv 		= gl_FragCoord.xy / resolution.xy;
                	uv 			= center_and_correct_aspect_ratio(uv);
                	
                	float lacunarity 	= 3.0; //roughness, kinda
                	float amplitude  	= 0.5; //maximum brightness per step
                	const int iterations	= 8;
                	
                	float noise 		= fbm(amplitude, lacunarity, uv, iterations); //this is a really simple perlin-esque noise function
                	float falloff 		= length(uv);
                                
                	float radius = 1.2, border = 0.5;
                	falloff = 1.0-smoothstep(radius,radius+border, falloff);
                	
                	float cloud		= clamp(pow(noise,  1.5 + max(falloff, noise)) * (1.0 - falloff), 0.0, 1.0);
                	gl_FragColor.rgb	= vec3(color.r,color.g,color.b)*cloud;
                	gl_FragColor.a = cloud * alpha;
                }""";
    }
}
