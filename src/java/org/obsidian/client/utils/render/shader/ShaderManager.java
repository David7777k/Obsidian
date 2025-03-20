package org.obsidian.client.utils.render.shader;


import lombok.Getter;
import net.minecraft.util.math.vector.Matrix4f;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.api.interfaces.IRender;
import org.obsidian.client.utils.file.FileUtils;
import org.obsidian.client.utils.render.shader.glsl.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@Getter
public class ShaderManager implements IRender, IMinecraft {
    private final int programID;
    public static ShaderManager fontShader;
    public static ShaderManager kawaseUpBloom;
    public static ShaderManager kawaseDownBloom;
    public static ShaderManager entityChamsShader;
    public static ShaderManager entityOutlineShader;
    public static ShaderManager roundedShader;
    public static ShaderManager roundedTextureShader;
    public static ShaderManager roundedOutlineShader;
    public static ShaderManager mainmenuShader;

    public static void loadShaders() {
        fontShader = create(new FontGlsl());
        kawaseUpBloom = create(new KawaseUpBloomGlsl());
        kawaseDownBloom = create(new KawaseDownBloomGlsl());
        entityChamsShader = create(new EntityChamsGlsl());
        entityOutlineShader = create(new EntityOutlineGlsl());
        roundedShader = create(new RoundedGlsl());
        roundedTextureShader = create(new RoundedTextureGlsl());
        roundedOutlineShader = create(new RoundedOutline());
        mainmenuShader = create(new MainMenuGlsl());
    }

    private ShaderManager(IShader fragmentShaderLoc, IShader vertexShaderLoc) {
        int program = glCreateProgram();
        int fragmentShaderID = createShader(new ByteArrayInputStream(fragmentShaderLoc.shader().getBytes()), GL_FRAGMENT_SHADER);
        GL20.glAttachShader(program, fragmentShaderID);
        int vertexShaderID = createShader(new ByteArrayInputStream(vertexShaderLoc.shader().getBytes()), GL_VERTEX_SHADER);
        GL20.glAttachShader(program, vertexShaderID);
        GL20.glLinkProgram(program);
        int status = glGetProgrami(program, GL_LINK_STATUS);
        if (status == 0) throw new IllegalStateException("Shader creation failed");
        this.programID = program;
    }

    public static ShaderManager create(IShader shader) {
        return new ShaderManager(shader, new VertexGlsl());
    }

    public static ShaderManager create(IShader fragShader, IShader vertexShader) {
        return new ShaderManager(fragShader, vertexShader);
    }


    private int createShader(InputStream inputStream, int shaderType) {
        int shader = glCreateShader(shaderType);
        glShaderSource(shader, FileUtils.readInputStream(inputStream));
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            System.out.println(glGetShaderInfoLog(shader, 4096));
            throw new IllegalStateException(String.format("Shader (%s) failed to compile", shaderType));
        }
        return shader;
    }

    public void load() {
        glUseProgram(programID);
    }

    public void unload() {
        glUseProgram(0);
    }

    public int getUniform(String name) {
        return glGetUniformLocation(programID, name);
    }

    public ShaderManager setUniformf(String name, float... args) {
        int loc = glGetUniformLocation(programID, name);
        switch (args.length) {
            case 1 -> glUniform1f(loc, args[0]);
            case 2 -> glUniform2f(loc, args[0], args[1]);
            case 3 -> glUniform3f(loc, args[0], args[1], args[2]);
            case 4 -> glUniform4f(loc, args[0], args[1], args[2], args[3]);
        }
        return this;
    }

    public ShaderManager setUniformi(String name, int... args) {
        int loc = glGetUniformLocation(programID, name);
        switch (args.length) {
            case 1 -> glUniform1i(loc, args[0]);
            case 2 -> glUniform2i(loc, args[0], args[1]);
            case 3 -> glUniform3i(loc, args[0], args[1], args[2]);
            case 4 -> glUniform4i(loc, args[0], args[1], args[2], args[3]);
        }
        return this;
    }

    public ShaderManager setMat4fv(String name, FloatBuffer matrix) {
        int loc = glGetUniformLocation(programID, name);
        glUniformMatrix4fv(loc, false, matrix);
        return this;
    }

    public ShaderManager setMat4fv(String name, float[] matrix) {
        int loc = glGetUniformLocation(programID, name);
        glUniformMatrix4fv(loc, false, matrix);
        return this;
    }

    public ShaderManager setMat4fv(String name, MatrixStack matrix) {
        setMat4fv(name, matrix.getLast().getMatrix());
        return this;
    }

    public ShaderManager setMat4fv(String name, Matrix4f matrix) {
        FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);
        matrix.write(floatBuffer);
        setMat4fv(name, floatBuffer);
        return this;
    }
}