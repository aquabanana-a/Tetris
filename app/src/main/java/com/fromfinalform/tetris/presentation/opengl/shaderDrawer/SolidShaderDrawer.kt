/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.opengl.shaderDrawer

import android.graphics.PointF
import android.graphics.RectF
import android.opengl.GLES20
import com.fromfinalform.tetris.domain.model.renderer.IRenderUnit
import com.fromfinalform.tetris.domain.model.renderer.IShaderDrawer
import com.fromfinalform.tetris.domain.model.renderer.ShaderDrawerTypeId
import com.fromfinalform.tetris.presentation.opengl.GLUtils
import com.fromfinalform.tetris.presentation.opengl.GLVertices
import com.fromfinalform.tetris.presentation.opengl.SceneParams
import com.fromfinalform.tetris.presentation.rotateMesh
import io.instories.common.render.GLColor

class SolidShaderDrawer() : IShaderDrawer {

    companion object {
        private const val VERTEX_SHADER = """
            uniform mat4 uMVPMatrix;
            attribute vec4 aPosition;
            
            void main() {
                gl_Position = aPosition;
            }
            """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            uniform vec4 uColor;
            
            void main() {
                gl_FragColor = uColor;
            }
            """

        const val VERTEX_SIZE = 2// + 4
        const val VERTICES_PER_SPRITE = 4
        const val INDICES_PER_SPRITE = 6
    }

    override val typeId get() = ShaderDrawerTypeId.SOLID
    override var program = 0; private set

    private var vertices: GLVertices
    private var vertexBuffer: FloatArray
    private var bufferIndex = 0

    private var positionHandle = -1
    private var colorHandle = -1

    init {
        program = GLUtils.createShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        if (program == 0)
            throw RuntimeException("failed creating program")

        colorHandle = GLES20.glGetUniformLocation(program, "uColor")
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")

        this.vertices = GLVertices(VERTICES_PER_SPRITE, INDICES_PER_SPRITE, positionHandle, -1, -1)
        this.vertexBuffer = FloatArray(VERTICES_PER_SPRITE * VERTEX_SIZE)

        var indices = ShortArray(GradientBackShaderDrawer.INDICES_PER_SPRITE)
        var i: Short = 0
        var v: Short = 0
        while (i < indices.size) {
            indices[i + 0] = (v + 0).toShort()
            indices[i + 1] = (v + 1).toShort()
            indices[i + 2] = (v + 2).toShort()
            indices[i + 3] = (v + 2).toShort()
            indices[i + 4] = (v + 3).toShort()
            indices[i + 5] = (v + 1).toShort()

            i = (i + GradientBackShaderDrawer.INDICES_PER_SPRITE).toShort()
            v = (v + GradientBackShaderDrawer.VERTICES_PER_SPRITE).toShort()
        }
        vertices.setIndices(indices, 0, indices.size)
    }

    private fun refreshMesh(dst: RectF, params: SceneParams) {
        bufferIndex = 0
        vertexBuffer[bufferIndex++] = dst.left
        vertexBuffer[bufferIndex++] = dst.top

        vertexBuffer[bufferIndex++] = dst.left
        vertexBuffer[bufferIndex++] = dst.bottom

        vertexBuffer[bufferIndex++] = dst.right
        vertexBuffer[bufferIndex++] = dst.top

        vertexBuffer[bufferIndex++] = dst.right
        vertexBuffer[bufferIndex++] = dst.bottom

        GLES20.glUniform4f(colorHandle, color.getR(), color.getG(), color.getB(), color.getA())
    }

    var color: GLColor = GLColor.BLACK; private set

    override fun setUniforms(vararg args: Any) {
        if (args.size != 1)
            return

        this.color = (args[0] as? GLColor) ?: GLColor.BLACK
    }

    override fun cleanUniforms() {
        this.color = GLColor.BLACK
    }

    override fun draw(ru: IRenderUnit, params: SceneParams, dst: RectF?, src: RectF?, angle: Float) {
        if (dst == null)
            return

        GLES20.glUseProgram(program)

        refreshMesh(dst, params)

        val pivot = PointF((dst.left + dst.right) / 2, (dst.bottom + dst.top) / 2)
        if (angle % 360 != 0f)
            rotateMesh(vertexBuffer, 0, bufferIndex, angle, pivot, params.sceneWH, VERTEX_SIZE)

        vertices.setVertices(vertexBuffer, 0, bufferIndex)
        vertices.bind()
        vertices.draw(GLES20.GL_TRIANGLES, 0, GradientBackShaderDrawer.INDICES_PER_SPRITE)
        vertices.unbind()
    }
}