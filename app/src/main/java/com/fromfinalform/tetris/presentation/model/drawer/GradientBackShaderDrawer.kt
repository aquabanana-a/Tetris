/*
 * Created by S.Dobranos on 18.11.20 23:43
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.drawer

import android.graphics.PointF
import android.graphics.RectF
import android.opengl.GLES20
import com.fromfinalform.tetris.presentation.model.renderer.unit.IRenderUnit
import com.fromfinalform.tetris.common.clone
import com.fromfinalform.tetris.common.heightInv
import com.fromfinalform.tetris.presentation.model.opengl.common.GLUtils.createShaderProgram
import com.fromfinalform.tetris.presentation.model.renderer.data.GLVertices
import com.fromfinalform.tetris.presentation.model.renderer.SceneParams
import com.fromfinalform.tetris.presentation.model.common.rotateMesh
import com.fromfinalform.tetris.presentation.model.renderer.data.GLGradient
import kotlin.math.sqrt

class GradientBackShaderDrawer() : IShaderDrawer {

    companion object {
        private const val VERTEX_SHADER = """
            uniform mat4 uMVPMatrix;
            uniform mat4 uSTMatrix;
            attribute vec4 aColor;
            attribute vec4 aPosition;
            varying vec4 vColor;

            void main() {
                gl_Position = aPosition;
                vColor = aColor;
            }
            """

        private const val FRAGMENT_SHADER = """
            precision mediump float;
            varying vec4 vColor;

            void main() {
                gl_FragColor = vColor;
            }
            """

        const val VERTEX_SIZE = 2 + 4         // Vertex Size (in Components) ie. (xy + rgba)
        const val VERTICES_PER_SPRITE = 4             // Vertices Per Sprite
        const val INDICES_PER_SPRITE = 6             // Indices Per Sprite
    }

    override val typeId get() = ShaderDrawerTypeId.GRADIENT_BACK
    override var program = 0; private set

    private var vertices: GLVertices
    private var vertexBuffer: FloatArray
    private var bufferIndex = 0

    private var positionHandle = -1
    private var colorHandle = -1

    init {
        program = createShaderProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        if (program == 0)
            throw RuntimeException("failed creating program")

        colorHandle = GLES20.glGetAttribLocation(program, "aColor")
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")

        this.vertices = GLVertices(VERTICES_PER_SPRITE, INDICES_PER_SPRITE, positionHandle, -1, colorHandle)
        this.vertexBuffer = FloatArray(VERTICES_PER_SPRITE * VERTEX_SIZE)

        var indices = ShortArray(INDICES_PER_SPRITE)
        var i: Short = 0
        var v: Short = 0
        while (i < indices.size) {
            indices[i + 0] = (v + 0).toShort()
            indices[i + 1] = (v + 1).toShort()
            indices[i + 2] = (v + 2).toShort()
            indices[i + 3] = (v + 2).toShort()
            indices[i + 4] = (v + 3).toShort()
            indices[i + 5] = (v + 1).toShort()

            i = (i + INDICES_PER_SPRITE).toShort()
            v = (v + VERTICES_PER_SPRITE).toShort()
        }
        vertices.setIndices(indices, 0, indices.size)
    }

    private fun refreshMesh(dst: RectF, params: SceneParams) {
        var canvasDiag = sqrt(params.sceneWidth * params.sceneWidth + params.sceneHeight * params.sceneHeight)
        var scaleX = 0f
        var scaleY = 0f

        if (params.sceneHeight > params.sceneWidth) {
            scaleX = canvasDiag / params.sceneWidth
            scaleY = canvasDiag / params.sceneHeight
        } else {
            scaleX = canvasDiag / params.sceneHeight
            scaleY = canvasDiag / params.sceneWidth
        }

        //var dst = ru.getRectOriginDst()
        var dstScaled = RectF()
        if (dst != null) {
            var offsetX = dst.width() * (scaleX - 1) / 2
            var offsetY = dst.heightInv() * (scaleY - 1) / 2

            dstScaled = dst.clone()

            dstScaled.left -= offsetX
            dstScaled.right += offsetX

            dstScaled.top += offsetY
            dstScaled.bottom -= offsetY
        }

        val colors = Array(4) { i ->
            if (!background.isGradient())
                background.colorPrimary
            else {
                if (i % 2 == 0)
                    background.colorPrimary
                else
                    background.colorSecondary!!
            }
        }

        bufferIndex = 0
        vertexBuffer[bufferIndex++] = dstScaled.left
        vertexBuffer[bufferIndex++] = dstScaled.top
        vertexBuffer[bufferIndex++] = colors[0].getR()
        vertexBuffer[bufferIndex++] = colors[0].getG()
        vertexBuffer[bufferIndex++] = colors[0].getB()
        vertexBuffer[bufferIndex++] = colors[0].getA()

        vertexBuffer[bufferIndex++] = dstScaled.left
        vertexBuffer[bufferIndex++] = dstScaled.bottom
        vertexBuffer[bufferIndex++] = colors[1].getR()
        vertexBuffer[bufferIndex++] = colors[1].getG()
        vertexBuffer[bufferIndex++] = colors[1].getB()
        vertexBuffer[bufferIndex++] = colors[1].getA()

        vertexBuffer[bufferIndex++] = dstScaled.right
        vertexBuffer[bufferIndex++] = dstScaled.top
        vertexBuffer[bufferIndex++] = colors[2].getR()
        vertexBuffer[bufferIndex++] = colors[2].getG()
        vertexBuffer[bufferIndex++] = colors[2].getB()
        vertexBuffer[bufferIndex++] = colors[2].getA()

        vertexBuffer[bufferIndex++] = dstScaled.right
        vertexBuffer[bufferIndex++] = dstScaled.bottom
        vertexBuffer[bufferIndex++] = colors[3].getR()
        vertexBuffer[bufferIndex++] = colors[3].getG()
        vertexBuffer[bufferIndex++] = colors[3].getB()
        vertexBuffer[bufferIndex++] = colors[3].getA()
    }

    var background: GLGradient = GLGradient.TRANSPARENT; private set

    override fun setUniforms(vararg args: Any) {
        if (args.size != 1)
            return

        this.background = (args[0] as? GLGradient) ?: GLGradient.TRANSPARENT
    }

    override fun cleanUniforms() {
        this.background = GLGradient.TRANSPARENT
    }

    override fun draw(ru: IRenderUnit, params: SceneParams, dst: RectF?, src: RectF?, angle: Float) {
        if (dst == null)
            return

        GLES20.glUseProgram(program)

        refreshMesh(dst, params)

        val pivot = PointF((dst.left + dst.right) / 2, (dst.bottom + dst.top) / 2)
        val bga = background.angle + angle
        if (bga % 360 != 0f)
            rotateMesh(vertexBuffer, 0, bufferIndex, bga, pivot, params.sceneWH, VERTEX_SIZE)

        vertices.setVertices(vertexBuffer, 0, bufferIndex)
        vertices.bind()
        vertices.draw(GLES20.GL_TRIANGLES, 0, INDICES_PER_SPRITE)
        vertices.unbind()
    }
}