/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests.gl_500;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL4;
import framework.Profile;
import framework.Test;

/**
 *
 * @author GBarbieri
 */
public class Gl_500_blend_op_amd extends Test {

    public static void main(String[] args) {
        Gl_500_blend_op_amd gl_500_blend_op_amd = new Gl_500_blend_op_amd();
    }

    public Gl_500_blend_op_amd() {
        super("gl-500-blend-op-amd", Profile.CORE, 4, 5);
    }

    @Override
    protected boolean begin(GL gl) {

        GL4 gl4 = (GL4) gl;

//		Viewport[texture::RGB8] = glm::ivec4(0, 0, WindowSize >> 1);
//		Viewport[texture::R] = glm::ivec4(WindowSize.x >> 1, 0, WindowSize >> 1);
//		Viewport[texture::G] = glm::ivec4(WindowSize.x >> 1, WindowSize.y >> 1, WindowSize >> 1);
//		Viewport[texture::B] = glm::ivec4(0, WindowSize.y >> 1, WindowSize >> 1);
        boolean validated = true;
        validated = validated && gl4.isExtensionAvailable("GL_AMD_blend_minmax_factor");

//		if(validated)
//			validated = initBlend();
//		if(validated)
//			validated = initProgram();
//		if(validated)
//			validated = initBuffer();
//		if(validated)
//			validated = initVertexArray();
//		if(validated)
//			validated = initTexture();
//		if(validated)
//			validated = initFramebuffer();
        return validated && checkError(gl4, "begin");
    }
}
