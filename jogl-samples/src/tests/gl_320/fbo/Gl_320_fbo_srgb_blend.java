/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_320.fbo;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL2GL3.*;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import glm.glm;
import glm.mat._4.Mat4;
import framework.BufferUtils;
import framework.Profile;
import framework.Semantic;
import framework.Test;
import glm.vec._2.Vec2;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import jgli.Texture2d;

/**
 *
 * @author GBarbieri
 */
public class Gl_320_fbo_srgb_blend extends Test {

    public static void main(String[] args) {
        Gl_320_fbo_srgb_blend gl_320_fbo_srgb_blend = new Gl_320_fbo_srgb_blend();
    }

    public Gl_320_fbo_srgb_blend() {
        super("Gl-320-fbo-srgb-blend", Profile.CORE, 3, 2);
    }

    private final String SHADER_SOURCE_TEXTURE = "fbo-srgb-blend";
    private final String SHADER_SOURCE_SPLASH = "fbo-srgb-blit-blend";
    private final String SHADERS_ROOT = "src/data/gl_320/fbo";
    private final String TEXTURE_DIFFUSE = "kueken7_rgba8_srgb.dds";

    private int vertexCount = 4;
    private int vertexSize = vertexCount * glf.Vertex_v2fv2f.SIZE;
    private float[] VertexData = {
        -10.0f, -10.0f,/**/ 0.0f, 1.0f,
        +10.0f, -10.0f,/**/ 1.0f, 1.0f,
        +10.0f, +10.0f,/**/ 1.0f, 0.0f,
        -10.0f, +10.0f,/**/ 0.0f, 0.0f};

    private int elementCount = 6;
    private int elementSize = elementCount * Short.BYTES;
    private short[] elementData = {
        0, 1, 2,
        2, 3, 0};

    private class Buffer {

        public static final int VERTEX = 0;
        public static final int ELEMENT = 1;
        public static final int TRANSFORM = 2;
        public static final int MAX = 3;
    }

    private class Texture {

        public static final int DIFFUSE = 0;
        public static final int COLORBUFFER = 1;
        public static final int RENDERBUFFER = 2;
        public static final int MAX = 3;
    }

    private class Program {

        public static final int TEXTURE = 0;
        public static final int SPLASH = 1;
        public static final int MAX = 2;
    }

    private class Shader {

        public static final int VERT_TEXTURE = 0;
        public static final int FRAG_TEXTURE = 1;
        public static final int VERT_SPLASH = 2;
        public static final int FRAG_SPLASH = 3;
        public static final int MAX = 4;
    }

    private int[] programName = new int[Program.MAX], uniformDiffuse = new int[Program.MAX];
    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(Program.MAX),
            framebufferName = GLBuffers.newDirectIntBuffer(1), bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX),
            textureName = GLBuffers.newDirectIntBuffer(Texture.MAX);
    private int framebufferScale = 2, uniformTransform;
    private FloatBuffer clearColor = GLBuffers.newDirectFloatBuffer(new float[]{0.0f, 0.0f, 0.0f, 1.0f});

    @Override
    protected boolean begin(GL gl) {

        GL3 gl3 = (GL3) gl;

        boolean validated = true;

        // Explicitly convert linear pixel color to sRGB color space, as FramebufferName is a sRGB FBO
        // Shader execution is done with linear color to get correct linear algebra working.
        gl3.glEnable(GL_FRAMEBUFFER_SRGB);

        if (validated) {
            validated = initProgram(gl3);
        }
        if (validated) {
            validated = initBuffer(gl3);
        }
        if (validated) {
            validated = initVertexArray(gl3);
        }
        if (validated) {
            validated = initTexture(gl3);
        }
        if (validated) {
            validated = initFramebuffer(gl3);
        }

        return validated;
    }

    private boolean initProgram(GL3 gl3) {

        boolean validated = true;

        ShaderCode[] shaderCodes = new ShaderCode[Shader.MAX];

        if (validated) {

            shaderCodes[Shader.VERT_TEXTURE] = ShaderCode.create(gl3, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADER_SOURCE_TEXTURE, "vert", null, true);
            shaderCodes[Shader.FRAG_TEXTURE] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADER_SOURCE_TEXTURE, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            programName[Program.TEXTURE] = program.program();

            program.add(shaderCodes[Shader.VERT_TEXTURE]);
            program.add(shaderCodes[Shader.FRAG_TEXTURE]);

            program.init(gl3);

            programName[Program.TEXTURE] = program.program();

            gl3.glBindAttribLocation(programName[Program.TEXTURE], Semantic.Attr.POSITION, "position");
            gl3.glBindAttribLocation(programName[Program.TEXTURE], Semantic.Attr.TEXCOORD, "texCoord");
            gl3.glBindFragDataLocation(programName[Program.TEXTURE], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }

        if (validated) {

            shaderCodes[Shader.VERT_SPLASH] = ShaderCode.create(gl3, GL_VERTEX_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADER_SOURCE_SPLASH, "vert", null, true);
            shaderCodes[Shader.FRAG_SPLASH] = ShaderCode.create(gl3, GL_FRAGMENT_SHADER, this.getClass(), SHADERS_ROOT,
                    null, SHADER_SOURCE_SPLASH, "frag", null, true);

            ShaderProgram program = new ShaderProgram();
            programName[Program.SPLASH] = program.program();

            program.add(shaderCodes[Shader.VERT_SPLASH]);
            program.add(shaderCodes[Shader.FRAG_SPLASH]);

            program.init(gl3);

            programName[Program.SPLASH] = program.program();

            gl3.glBindFragDataLocation(programName[Program.SPLASH], Semantic.Frag.COLOR, "color");

            program.link(gl3, System.out);
        }

        if (validated) {

            uniformTransform = gl3.glGetUniformBlockIndex(programName[Program.TEXTURE], "Transform");
            uniformDiffuse[Program.TEXTURE] = gl3.glGetUniformLocation(programName[Program.TEXTURE], "diffuse");
            uniformDiffuse[Program.SPLASH] = gl3.glGetUniformLocation(programName[Program.SPLASH], "diffuse");

            gl3.glUseProgram(programName[Program.TEXTURE]);
            gl3.glUniform1i(uniformDiffuse[Program.TEXTURE], 0);
            gl3.glUniformBlockBinding(programName[Program.TEXTURE], uniformTransform, Semantic.Uniform.TRANSFORM0);

            gl3.glUseProgram(programName[Program.SPLASH]);
            gl3.glUniform1i(uniformDiffuse[Program.SPLASH], 0);
        }

        return validated & checkError(gl3, "initProgram");
    }

    private boolean initBuffer(GL3 gl3) {

        ShortBuffer elementBuffer = GLBuffers.newDirectShortBuffer(elementData);
        FloatBuffer vertexBuffer = GLBuffers.newDirectFloatBuffer(VertexData);
        IntBuffer uniformBufferOffset = GLBuffers.newDirectIntBuffer(1);

        gl3.glGenBuffers(Buffer.MAX, bufferName);

        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        gl3.glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementSize, elementBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);

        gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
        gl3.glBufferData(GL_ARRAY_BUFFER, vertexSize, vertexBuffer, GL_STATIC_DRAW);
        gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

        gl3.glGetIntegerv(GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT, uniformBufferOffset);
        int uniformBlockSize = Math.max(Mat4.SIZE, uniformBufferOffset.get(0));

        gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
        gl3.glBufferData(GL_UNIFORM_BUFFER, uniformBlockSize, null, GL_DYNAMIC_DRAW);
        gl3.glBindBuffer(GL_UNIFORM_BUFFER, 0);

        BufferUtils.destroyDirectBuffer(elementBuffer);
        BufferUtils.destroyDirectBuffer(vertexBuffer);
        BufferUtils.destroyDirectBuffer(uniformBufferOffset);

        return true;
    }

    private boolean initTexture(GL3 gl3) {

        boolean validated = true;

        try {

            jgli.Texture2d texture = new Texture2d(jgli.Load.load(TEXTURE_ROOT + "/" + TEXTURE_DIFFUSE));
            assert (!texture.empty());

            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            gl3.glGenTextures(Texture.MAX, textureName);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.DIFFUSE));
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, texture.levels() - 1);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            jgli.Gl.Format format = jgli.Gl.translate(texture.format());
            for (int level = 0; level < texture.levels(); ++level) {
                gl3.glTexImage2D(GL_TEXTURE_2D, level,
                        format.internal.value,
                        texture.dimensions(level)[0], texture.dimensions(level)[1],
                        0,
                        format.external.value, format.type.value,
                        texture.data(level));
            }

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.COLORBUFFER));
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_BASE_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAX_LEVEL, 0);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            gl3.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
            gl3.glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB8_ALPHA8, windowSize.x * framebufferScale,
                    windowSize.y * framebufferScale, 0, GL_RGBA, GL_UNSIGNED_BYTE, null);

            gl3.glPixelStorei(GL_UNPACK_ALIGNMENT, 4);

        } catch (IOException ex) {
            Logger.getLogger(Gl_320_fbo_srgb_blend.class.getName()).log(Level.SEVERE, null, ex);
        }
        return validated;
    }

    private boolean initVertexArray(GL3 gl3) {

        gl3.glGenVertexArrays(Program.MAX, vertexArrayName);
        gl3.glBindVertexArray(vertexArrayName.get(Program.TEXTURE));
        {
            gl3.glBindBuffer(GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX));
            gl3.glVertexAttribPointer(Semantic.Attr.POSITION, 2, GL_FLOAT, false, glf.Vertex_v2fv2f.SIZE, 0);
            gl3.glVertexAttribPointer(Semantic.Attr.TEXCOORD, 2, GL_FLOAT, false, glf.Vertex_v2fv2f.SIZE, Vec2.SIZE);
            gl3.glBindBuffer(GL_ARRAY_BUFFER, 0);

            gl3.glEnableVertexAttribArray(Semantic.Attr.POSITION);
            gl3.glEnableVertexAttribArray(Semantic.Attr.TEXCOORD);

            gl3.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT));
        }
        gl3.glBindVertexArray(0);

        gl3.glBindVertexArray(vertexArrayName.get(Program.SPLASH));
        gl3.glBindVertexArray(0);

        return true;
    }

    private boolean initFramebuffer(GL3 gl3) {

        gl3.glGenFramebuffers(1, framebufferName);
        gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(0));
        gl3.glFramebufferTexture(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, textureName.get(Texture.COLORBUFFER), 0);

        if (!isFramebufferComplete(gl3, framebufferName.get(0))) {
            return false;
        }
        int encodingLinear = GL_LINEAR;
        int encodingSRGB = GL_SRGB;

        gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        int[] encoding = {0};
        gl3.glGetFramebufferAttachmentParameteriv(GL_DRAW_FRAMEBUFFER, GL_BACK_LEFT,
                GL_FRAMEBUFFER_ATTACHMENT_COLOR_ENCODING, encoding, 0);

        return true;
    }

    @Override
    protected boolean render(GL gl) {

        GL3 gl3 = (GL3) gl;

        {
            gl3.glBindBuffer(GL_UNIFORM_BUFFER, bufferName.get(Buffer.TRANSFORM));
            ByteBuffer pointer = gl3.glMapBufferRange(GL_UNIFORM_BUFFER,
                    0, Mat4.SIZE, GL_MAP_WRITE_BIT | GL_MAP_INVALIDATE_BUFFER_BIT);

            Mat4 projection = glm.perspective_((float) Math.PI * 0.25f, (float) windowSize.x / windowSize.y, 0.1f, 100.0f);
            pointer.asFloatBuffer().put(projection.mul(viewMat4()).toFa_());

            // Make sure the uniform buffer is uploaded
            gl3.glUnmapBuffer(GL_UNIFORM_BUFFER);
        }

        // Render a textured quad to a sRGB framebuffer object.
        {
            gl3.glViewport(0, 0, windowSize.x * framebufferScale, windowSize.y * framebufferScale);

            gl3.glEnable(GL_BLEND);
            gl3.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

            gl3.glBindFramebuffer(GL_FRAMEBUFFER, framebufferName.get(0));
            gl3.glEnable(GL_FRAMEBUFFER_SRGB);

            gl3.glClearBufferfv(GL_COLOR, 0, clearColor);

            gl3.glUseProgram(programName[Program.TEXTURE]);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.DIFFUSE));
            gl3.glBindVertexArray(vertexArrayName.get(Program.TEXTURE));
            gl3.glBindBufferBase(GL_UNIFORM_BUFFER, Semantic.Uniform.TRANSFORM0, bufferName.get(Buffer.TRANSFORM));

            gl3.glDrawElementsInstancedBaseVertex(GL_TRIANGLES, elementCount, GL_UNSIGNED_SHORT, 0, 100, 0);

            gl3.glDisable(GL_BLEND);
        }

        // Blit the sRGB framebuffer to the default framebuffer back buffer.
        {
            gl3.glViewport(0, 0, windowSize.x, windowSize.y);

            gl3.glBindFramebuffer(GL_FRAMEBUFFER, 0);
            gl3.glDisable(GL_FRAMEBUFFER_SRGB);

            gl3.glUseProgram(programName[Program.SPLASH]);

            gl3.glActiveTexture(GL_TEXTURE0);
            gl3.glBindVertexArray(vertexArrayName.get(Program.SPLASH));
            gl3.glBindTexture(GL_TEXTURE_2D, textureName.get(Texture.COLORBUFFER));

            gl3.glDrawArraysInstanced(GL_TRIANGLES, 0, 3, 1);
        }

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL3 gl3 = (GL3) gl;

        gl3.glDeleteFramebuffers(1, framebufferName);
        gl3.glDeleteProgram(programName[Program.SPLASH]);
        gl3.glDeleteProgram(programName[Program.TEXTURE]);

        gl3.glDeleteBuffers(Buffer.MAX, bufferName);
        gl3.glDeleteTextures(Texture.MAX, textureName);
        gl3.glDeleteVertexArrays(Program.MAX, vertexArrayName);

        BufferUtils.destroyDirectBuffer(framebufferName);
        BufferUtils.destroyDirectBuffer(bufferName);
        BufferUtils.destroyDirectBuffer(textureName);
        BufferUtils.destroyDirectBuffer(vertexArrayName);

        BufferUtils.destroyDirectBuffer(clearColor);

        return true;
    }
}
