/*
 * Created by S.Dobranos on 19.11.20 15:01
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.renderer.unit

import android.graphics.Color
import android.graphics.RectF
import android.opengl.GLES20
import com.fromfinalform.tetris.common.ICloneable
import com.fromfinalform.tetris.presentation.model.drawer.ShaderDrawerTypeId
import com.fromfinalform.tetris.common.clone

open class RenderItem : ICloneable<RenderItem> {

    var id: Long = -1L;                             private set

    var x: Float = -1f;                             private set
    var y: Float = 1f;                              private set
    var width: Float = 0f;                          private set
    var height: Float = 0f;                         private set
    var rotation: Float = 0f;                       private set

    var srcRect: RectF = RectF(0f, 0f, 1f, 1f);     private set
    val dstRect: RectF get() = RectF(x, y, x + width, y - height)

    var textureId: Int? = null;                     private set
    var color: Int = Color.BLACK;                   private set
    var shaderTypeId = ShaderDrawerTypeId.NONE;     private set

    var blendSrc = GLES20.GL_ONE;                   private set
    var blendDst = GLES20.GL_ONE_MINUS_SRC_ALPHA;   private set
    var blendSrcRGB: Int? = null;                   private set
    var blendSrcAlpha: Int? = null;                 private set
    var blendDstRGB: Int? = null;                   private set
    var blendDstAlpha: Int? = null;                 private set

    var childs: List<RenderItem>? = null;           private set
    private val childsLo = Any()

    val usedBlend get() = usedBlendFactor || usedBlendSeparate
    val usedBlendFactor get() = blendSrc > 0 && blendDst > 0
    val usedBlendSeparate get() = blendSrcRGB != null && blendSrcAlpha != null && blendDstRGB != null && blendDstAlpha != null

    fun translateX(dX: Float): RenderItem { synchronized(childsLo) {
        this.x += dX
        this.childs?.forEach { c -> c.x += dX }
        return this
    } }

    fun translateXwidth(times: Int): RenderItem {
        this.translateX(times * width)
        return this
    }

    fun translateY(dY: Float): RenderItem { synchronized(childsLo) {
        this.y -= dY
        this.childs?.forEach { c -> c.y -= dY }
        return this
    } }

    fun translateYheight(times: Int): RenderItem {
        this.translateY(times * height)
        return this
    }

    fun addChild(value: RenderItem) { synchronized(childsLo) {
        if(this.childs == null)
            this.childs = ArrayList()

        (this.childs as ArrayList).add(value)
    } }

    fun removeChild(id: Long): RenderItem? { synchronized(childsLo) {
        var item = (this.childs as? ArrayList)?.first { it.id == id }
        val removed = (this.childs as? ArrayList)?.remove(item)
        return if (removed == true) item else null
    } }

    fun withId(id: Long): RenderItem {
        this.id = id
        return this
    }

    fun withLocation(x: Float, y: Float): RenderItem {
        this.x = x
        this.y = y
//        this.translateX(x + this.x)
//        this.translateY(y + this.y)
        return this
    }

    fun withRotation(angle: Float): RenderItem {
        this.rotation = angle
        return this
    }

    fun withSize(w: Float, h: Float): RenderItem {
        this.width = w
        this.height = h
        return this
    }

    fun withSrcRect(src: RectF): RenderItem {
        this.srcRect = src
        return this
    }

    fun withTexture(textureId: Int?): RenderItem {
        this.textureId = textureId
        return this
    }

    fun withColor(color: Int): RenderItem {
        this.color = color
        return this
    }

    fun withShader(shaderTypeId: ShaderDrawerTypeId): RenderItem {
        this.shaderTypeId = shaderTypeId
        return this
    }

    fun withBlendSeparate(srcRGB: Int?, dstRGB: Int?, srcAlpha: Int?, dstAlpha: Int?): RenderItem {
        this.blendSrcRGB = srcRGB
        this.blendSrcAlpha = srcAlpha
        this.blendDstRGB = dstRGB
        this.blendDstAlpha = dstAlpha
        return this
    }

    fun withBlendFactor(blendSrc: Int, blendDst: Int): RenderItem {
        this.blendSrc = blendSrc
        this.blendDst = blendDst
        return this
    }

    fun withChilds(items: List<RenderItem>?): RenderItem {
        this.childs = if(items == null) null else ArrayList(items.map { c -> c.clone() })
        return this
    }

    override fun clone(): RenderItem { synchronized(childsLo) {
        return RenderItem()
            .withLocation(x, y)
            .withRotation(rotation)
            .withSize(width, height)
            .withSrcRect(srcRect.clone())
            .withTexture(textureId)
            .withColor(color)
            .withShader(shaderTypeId)
            .withBlendFactor(blendSrc, blendDst)
            .withBlendSeparate(blendSrcRGB, blendDstRGB, blendSrcAlpha, blendDstAlpha)
            .withChilds(childs)
    } }
}