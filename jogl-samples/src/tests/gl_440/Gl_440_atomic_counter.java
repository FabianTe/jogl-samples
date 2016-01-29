/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_440;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_MAP_WRITE_BIT;
import static com.jogamp.opengl.GL.GL_RGBA;
import static com.jogamp.opengl.GL.GL_TRIANGLES;
import static com.jogamp.opengl.GL.GL_TRUE;
import static com.jogamp.opengl.GL.GL_UNSIGNED_INT;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_FRAGMENT_SHADER_BIT;
import static com.jogamp.opengl.GL2ES2.GL_PROGRAM_SEPARABLE;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER;
import static com.jogamp.opengl.GL2ES2.GL_VERTEX_SHADER_BIT;
import static com.jogamp.opengl.GL2ES3.GL_ATOMIC_COUNTER_BUFFER;
import static com.jogamp.opengl.GL2ES3.GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS;
import static com.jogamp.opengl.GL2ES3.GL_MAX_FRAGMENT_ATOMIC_COUNTER_BUFFERS;
import static com.jogamp.opengl.GL2ES3.GL_MAX_GEOMETRY_ATOMIC_COUNTER_BUFFERS;
import static com.jogamp.opengl.GL2ES3.GL_MAX_TESS_CONTROL_ATOMIC_COUNTER_BUFFERS;
import static com.jogamp.opengl.GL2ES3.GL_MAX_TESS_EVALUATION_ATOMIC_COUNTER_BUFFERS;
import static com.jogamp.opengl.GL2ES3.GL_MAX_VERTEX_ATOMIC_COUNTER_BUFFERS;
import static com.jogamp.opengl.GL2ES3.GL_R8UI;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.glsl.ShaderCode;
import com.jogamp.opengl.util.glsl.ShaderProgram;
import framework.BufferUtils;
import framework.Profile;
import framework.Test;
import java.nio.IntBuffer;
import jglm.Vec2i;

/**
 *
 * @author GBarbieri
 */
public class Gl_440_atomic_counter extends Test {

    public static void main(String[] args) {
        Gl_440_atomic_counter gl_440_atomic_counter = new Gl_440_atomic_counter();
    }

    public Gl_440_atomic_counter() {
        super("gl-430-atomic-counter", Profile.CORE, 4, 4, new Vec2i(1280, 720));
    }

    private final String SHADERS_SOURCE = "atomic-counter";
    private final String SHADERS_ROOT = "src/data/gl_440";

    private enum Buffer {
        ATOMIC_COUNTER,
        MAX
    }

    private int[] pipelineName = {0}, vertexArrayName = {0}, bufferName = new int[Buffer.MAX.ordinal()];
    private int programName;

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

        boolean validated = checkExtension(gl4, "GL_ARB_clear_buffer_object");

        if (validated) {
            validated = initBuffer(gl4);
        }
        if (validated) {
            validated = initProgram(gl4);
        }
        if (validated) {
            validated = initVertexArray(gl4);
        }

        return validated;
    }

    private boolean initProgram(GL4 gl4) {

        boolean validated = true;

        // Create program
        if (validated) {

            ShaderCode vertShaderCode = ShaderCode.create(gl4, GL_VERTEX_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "vert", null, true);
            ShaderCode fragShaderCode = ShaderCode.create(gl4, GL_FRAGMENT_SHADER,
                    this.getClass(), SHADERS_ROOT, null, SHADERS_SOURCE, "frag", null, true);

            ShaderProgram shaderProgram = new ShaderProgram();
            shaderProgram.init(gl4);

            programName = shaderProgram.program();

            gl4.glProgramParameteri(programName, GL_PROGRAM_SEPARABLE, GL_TRUE);

            shaderProgram.add(vertShaderCode);
            shaderProgram.add(fragShaderCode);

            shaderProgram.link(gl4, System.out);
        }

        if (validated) {

            gl4.glGenProgramPipelines(1, pipelineName, 0);
            gl4.glUseProgramStages(pipelineName[0], GL_VERTEX_SHADER_BIT | GL_FRAGMENT_SHADER_BIT, programName);
        }

        return validated & checkError(gl4, "initProgram");
    }

    private boolean initBuffer(GL4 gl4) {

        boolean validated = true;

        int[] maxVertexAtomicCounterBuffers = {0};
        int[] maxControlAtomicCounterBuffers = {0};
        int[] maxEvaluationAtomicCounterBuffers = {0};
        int[] maxGeometryAtomicCounterBuffers = {0};
        int[] maxFragmentAtomicCounterBuffers = {0};
        int[] maxCombinedAtomicCounterBuffers = {0};

        gl4.glGetIntegerv(GL_MAX_VERTEX_ATOMIC_COUNTER_BUFFERS, maxVertexAtomicCounterBuffers, 0);
        gl4.glGetIntegerv(GL_MAX_TESS_CONTROL_ATOMIC_COUNTER_BUFFERS, maxControlAtomicCounterBuffers, 0);
        gl4.glGetIntegerv(GL_MAX_TESS_EVALUATION_ATOMIC_COUNTER_BUFFERS, maxEvaluationAtomicCounterBuffers, 0);
        gl4.glGetIntegerv(GL_MAX_GEOMETRY_ATOMIC_COUNTER_BUFFERS, maxGeometryAtomicCounterBuffers, 0);
        gl4.glGetIntegerv(GL_MAX_FRAGMENT_ATOMIC_COUNTER_BUFFERS, maxFragmentAtomicCounterBuffers, 0);
        gl4.glGetIntegerv(GL_MAX_COMBINED_ATOMIC_COUNTER_BUFFERS, maxCombinedAtomicCounterBuffers, 0);

        gl4.glGenBuffers(Buffer.MAX.ordinal(), bufferName, 0);

        gl4.glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, bufferName[Buffer.ATOMIC_COUNTER.ordinal()]);
        gl4.glBufferStorage(GL_ATOMIC_COUNTER_BUFFER, Integer.BYTES, null, GL_MAP_WRITE_BIT);
        gl4.glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, 0);

        return validated;
    }

    private boolean initVertexArray(GL4 gl4) {

        boolean validated = true;

        gl4.glGenVertexArrays(1, vertexArrayName, 0);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindVertexArray(0);

        return validated;
    }

    @Override
    protected boolean render(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glBindBuffer(GL_ATOMIC_COUNTER_BUFFER, bufferName[Buffer.ATOMIC_COUNTER.ordinal()]);
        int[] data = {0};
        IntBuffer dataBuffer = GLBuffers.newDirectIntBuffer(data);
        gl4.glClearBufferSubData(GL_ATOMIC_COUNTER_BUFFER, GL_R8UI, 0, Integer.BYTES,
                GL_RGBA, GL_UNSIGNED_INT, dataBuffer);
        BufferUtils.destroyDirectBuffer(dataBuffer);

        gl4.glViewportIndexedf(0, 0, 0, windowSize.x, windowSize.y);

        gl4.glBindProgramPipeline(pipelineName[0]);
        gl4.glBindVertexArray(vertexArrayName[0]);
        gl4.glBindBufferBase(GL_ATOMIC_COUNTER_BUFFER, 0, bufferName[Buffer.ATOMIC_COUNTER.ordinal()]);

        gl4.glDrawArraysInstancedBaseInstance(GL_TRIANGLES, 0, 3, 1, 0);

        return true;
    }

    @Override
    protected boolean end(GL gl) {

        GL4 gl4 = (GL4) gl;

        gl4.glDeleteBuffers(Buffer.MAX.ordinal(), bufferName, 0);
        gl4.glDeleteProgram(programName);
        gl4.glDeleteProgramPipelines(1, pipelineName, 0);
        gl4.glDeleteVertexArrays(1, vertexArrayName, 0);

        return true;
    }
}
