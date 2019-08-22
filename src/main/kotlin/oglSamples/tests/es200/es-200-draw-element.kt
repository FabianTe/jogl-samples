package oglSamples.tests.es200

import glm_.BYTES
import glm_.glm
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import gln.BufferTarget.Companion.ARRAY
import gln.BufferTarget.Companion.ELEMENT_ARRAY
import gln.DataType.Companion.UNSIGNED_SHORT
import gln.cap.Caps
import gln.draw.glDrawElements
import gln.glViewport
import gln.glf.glf
import gln.identifiers.GlBuffers
import gln.identifiers.GlProgram
import gln.uniform.glUniform
import gln.vertexArray.glDisableVertexAttribArray
import gln.vertexArray.glEnableVertexAttribArray
import gln.vertexArray.glVertexAttribPointer
import kool.shortBufferOf
import oglSamples.framework.Framework
import oglSamples.framework.semantic
import oglSamples.vec2BufferOf
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.opengl.GL15C
import org.lwjgl.opengl.GL41C.*


fun main() {
    es_200_draw_elements()()
}

class es_200_draw_elements : Framework("es-200-draw-elements", Caps.Profile.ES, 2, 0) {

    val SHADER_SOURCE = "es-200/flat-color"

    val elementCount = 6
    val elementSize = elementCount * Short.BYTES
    val elementData = shortBufferOf(
            0, 1, 2,
            0, 2, 3)

    val vertexCount = 4
    val positionSize = vertexCount * Vec2.size
    val positionData = vec2BufferOf(
            Vec2(-1f, -1f),
            Vec2(+1f, -1f),
            Vec2(+1f, +1f),
            Vec2(-1f, +1f))

    val VERTEX = 0
    val ELEMENT = 1

    val buffers = GlBuffers(2)

    var program = GlProgram.NULL
    var uniformMVP = -1
    var uniformDiffuse = -1

    override fun begin(): Boolean {

        var validated = true

        val vendor = glGetString(GL_VENDOR)
        println(vendor)
        val renderer = glGetString(GL_RENDERER)
        println(renderer)
        val version = glGetString(GL_VERSION)
        println(version)
        val extensions = glGetString(GL_EXTENSIONS)
        println(extensions)

        if (validated)
            validated = initProgram()
        if (validated)
            validated = initBuffer()

        return validated
    }

    fun initProgram(): Boolean {

        var validated = true

        // Create program
        try {
            program = GlProgram.initFromPath("$SHADER_SOURCE.vert", "$SHADER_SOURCE.frag") {
                "Position".attrib = semantic.attr.POSITION
            }
        } catch (exc: Exception) {
            validated = false
        }

        // Get variables locations
        if (validated) {
            uniformMVP = program getUniformLocation "MVP"
            uniformDiffuse = program getUniformLocation "Diffuse"
        }

        // Set some variables
        if (validated)
        // Bind the program for use
            program.used {
                // Set uniform value
                glUniform(uniformDiffuse, Vec4(1f, 0.5f, 0f, 1f))
            }   // Unbind the program

        return validated && checkError("initProgram")
    }

    fun initBuffer(): Boolean {

        buffers.gen {
            VERTEX.bind(ARRAY) { data(positionData.data) }
            ELEMENT.bind(ELEMENT_ARRAY) { data(elementData) }
        }

        return checkError("initBuffer")
    }

    override fun end(): Boolean {

        buffers.delete()
        program.delete()

        return true
    }

    override fun render(): Boolean {

        // Compute the MVP (Model View Projection matrix)
        val projection = glm.perspective(glm.πf * 0.25f, window.aspect, 0.1f, 100f)
        val model = Mat4(1f)
        val mvp = projection * view * model

        // Set the display viewport
        glViewport(windowSize)

        // Clear color buffer with black
        glClearColor(0f, 0f, 0f, 1f)
        glClearDepthf(1f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // Bind program
        program.used {

            // Set the value of MVP uniform.
            glUniform(uniformMVP, mvp)

            buffers {
                VERTEX.bind(ARRAY) {
                    glVertexAttribPointer(glf.pos2)
                }
                ELEMENT bind ELEMENT_ARRAY
            }

            glEnableVertexAttribArray(glf.pos2)
            glDrawElements(elementCount, UNSIGNED_SHORT)
            glDisableVertexAttribArray(glf.pos2)

        }   // Unbind program

        return true
    }
}