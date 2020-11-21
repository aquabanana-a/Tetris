/*
 * Created by S.Dobranos on 19.11.20 15:01
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.renderer.data

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import java.nio.ShortBuffer

class GLVertices {
    companion object {
        const val POSITION_CNT_2D   = 2     // Number of Components in Vertex Position for 2D
        const val POSITION_CNT_3D   = 3     // Number of Components in Vertex Position for 3D
        const val COLOR_CNT         = 4     // Number of Components in Vertex Color
        const val TEXCOORD_CNT      = 2     // Number of Components in Vertex Texture Coords
        const val NORMAL_CNT        = 3     // Number of Components in Vertex Normal

        const val INDEX_SIZE        = java.lang.Short.SIZE / 8  // Index Byte Size (Short.SIZE = bits)
    }

    var positionCnt             = POSITION_CNT_2D
    var vertexStride            = 0     // Vertex Stride (Element Size of a Single Vertex)
    var vertexSize              = 0     // Bytesize of a Single Vertex

    var tmpBuffer: IntArray?    = null
    var vertices: IntBuffer?    = null
    var indices: ShortBuffer?   = null
    var numVertices             = 0
    var numIndices              = 0

    private var positionHandle  = -1
    private var texCoordHandle  = -1
    private var colorHandle     = -1
    private var normalsHandle   = -1

    fun hasTexCoords()  = texCoordHandle    != -1
    fun hasPositions()  = positionHandle    != -1
    fun hasColor()      = colorHandle       != -1
    fun hasNormals()    = normalsHandle     != -1

    // maxVertices - maximum vertices allowed in buffer
    // maxIndices - maximum indices allowed in buffer
    constructor(maxVertices: Int, maxIndices: Int, posHandle: Int = -1, texHandle: Int = -1, clrHandle: Int = -1, nrmlHandle: Int = -1, use3D: Boolean = false) {
        this.positionCnt    = if(use3D) POSITION_CNT_3D else POSITION_CNT_2D

        this.positionHandle = posHandle
        this.texCoordHandle = texHandle
        this.colorHandle    = clrHandle
        this.normalsHandle  = nrmlHandle

        this.vertexStride   = this.positionCnt + (if(hasColor()) COLOR_CNT else 0) + (if(hasTexCoords()) TEXCOORD_CNT else 0) + (if(hasNormals()) NORMAL_CNT else 0)
        this.vertexSize     = this.vertexStride * 4

        var buffer = ByteBuffer.allocateDirect(maxVertices * vertexSize)
        buffer.order(ByteOrder.nativeOrder())
        this.vertices = buffer.asIntBuffer()

        if (maxIndices > 0) {
            buffer = ByteBuffer.allocateDirect(maxIndices * INDEX_SIZE)
            buffer.order(ByteOrder.nativeOrder())
            this.indices = buffer.asShortBuffer()
        } else
            indices = null

        this.numVertices = 0
        this.numIndices = 0

        this.tmpBuffer = IntArray(maxVertices * vertexSize / 4)
    }

    fun setVertices(vertices: FloatArray, offset: Int, length: Int) {
        this.vertices!!.clear()
        val last = offset + length

        var j = 0
        for (i in offset until last) { tmpBuffer!![j++] = vertices[i].toBits() }  // Set Vertex as Raw Integer Bits in Buffer

        this.vertices!!.put(tmpBuffer, 0, length)
        this.vertices!!.flip()
        this.numVertices = length / this.vertexStride
    }

    fun setIndices(indices: ShortArray, offset: Int, length: Int) {
        this.indices!!.clear()
        this.indices!!.put(indices, offset, length)
        this.indices!!.flip()
        this.numIndices = length
    }

    fun bind() {
        vertices!!.position(0)
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, positionCnt, GLES20.GL_FLOAT, false, vertexSize, vertices)

        if (hasColor()) {
            vertices!!.position(positionCnt)
            GLES20.glEnableVertexAttribArray(colorHandle)
            GLES20.glVertexAttribPointer(colorHandle, COLOR_CNT, GLES20.GL_FLOAT, false, vertexSize, vertices)
        }

        if (hasTexCoords()) {
            vertices!!.position(positionCnt + (if(hasColor()) COLOR_CNT else 0))  // !NOTE: position based on whether color is also specified
            GLES20.glEnableVertexAttribArray(texCoordHandle)
            GLES20.glVertexAttribPointer(texCoordHandle, TEXCOORD_CNT, GLES20.GL_FLOAT, false, vertexSize, vertices)
        }

        if (hasNormals()) {
            vertices!!.position(positionCnt + (if(hasColor()) COLOR_CNT else 0) + (if(hasTexCoords()) TEXCOORD_CNT else 0))  // !NOTE: position based on whether color/texcoords is also specified
            GLES20.glEnableVertexAttribArray(normalsHandle)
            GLES20.glVertexAttribPointer(normalsHandle, TEXCOORD_CNT, GLES20.GL_FLOAT, false, vertexSize, vertices)
        }
    }

    fun unbind() {
        GLES20.glDisableVertexAttribArray(positionHandle)
        if (hasColor())     GLES20.glDisableVertexAttribArray(colorHandle)
        if (hasTexCoords()) GLES20.glDisableVertexAttribArray(texCoordHandle)
        if (hasNormals())   GLES20.glDisableVertexAttribArray(normalsHandle)
    }

    fun draw(primitiveType: Int, offset: Int, numVertices: Int) {
        if (indices != null) {
            indices!!.position(offset)
            GLES20.glDrawElements(primitiveType, numVertices, GLES20.GL_UNSIGNED_SHORT, indices)
            //checkGlError("glDrawElements")
        }
        else {
            GLES20.glDrawArrays(primitiveType, offset, numVertices)
            //checkGlError("glDrawArrays")
        }
    }

    fun drawFull(primitiveType: Int, offset: Int, numVertices: Int) {
        bind()
        draw(primitiveType, offset, numVertices)
        unbind()
    }

    // set vertex elements
    fun setVtxPosition(vtxIdx: Int, x: Float, y: Float) {
        val index = vtxIdx * vertexStride
        vertices!!.put(index + 0, x.toBits())
        vertices!!.put(index + 1, y.toBits())
    }
    fun setVtxPosition(vtxIdx: Int, x: Float, y: Float, z: Float) {
        val index = vtxIdx * vertexStride
        vertices!!.put(index + 0, x.toBits())
        vertices!!.put(index + 1, y.toBits())
        vertices!!.put(index + 2, z.toBits())
    }
    fun setVtxColor(vtxIdx: Int, r: Float, g: Float, b: Float, a: Float) {
        val index = (vtxIdx * vertexStride) + positionCnt
        vertices!!.put(index + 0, r.toBits())
        vertices!!.put(index + 1, g.toBits())
        vertices!!.put(index + 2, b.toBits())
        vertices!!.put(index + 3, a.toBits())
    }
    fun setVtxColor(vtxIdx: Int, r: Float, g: Float, b: Float) {
        val index = (vtxIdx * vertexStride) + positionCnt
        vertices!!.put(index + 0, r.toBits())
        vertices!!.put(index + 1, g.toBits())
        vertices!!.put(index + 2, b.toBits())
    }
    fun setVtxColor(vtxIdx: Int, a: Float) {
        val index = (vtxIdx * vertexStride) + positionCnt
        vertices!!.put(index + 3, a.toBits())
    }
    fun setVtxTexCoords(vtxIdx: Int, u: Float, v: Float) {
        val index = (vtxIdx * vertexStride) + positionCnt + (if(hasColor()) COLOR_CNT else 0)
        vertices!!.put(index + 0, u.toBits())
        vertices!!.put(index + 1, v.toBits())
    }
    fun setVtxNormal(vtxIdx: Int, x: Float, y: Float, z: Float) {
        val index = (vtxIdx * vertexStride) + positionCnt + (if(hasColor()) COLOR_CNT else 0) + (if(hasTexCoords()) TEXCOORD_CNT else 0)
        vertices!!.put(index + 0, x.toBits())
        vertices!!.put(index + 1, y.toBits())
        vertices!!.put(index + 2, z.toBits())
    }
}