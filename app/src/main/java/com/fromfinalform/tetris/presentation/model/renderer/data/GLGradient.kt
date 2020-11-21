/*
 * Created by S.Dobranos on 19.11.20 15:01
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.renderer.data

import java.io.Serializable

class GLGradient(
    var colorPrimary: GLColor,
    var angle: Int = 0) : Serializable { // clockwise
    var colorSecondary: GLColor? = null

    companion object {
        val TRANSPARENT = GLGradient(GLColor.TRANSPARENT)
        val BLACK       = GLGradient(GLColor.BLACK)
        val WHITE       = GLGradient(GLColor.WHITE)
    }

    constructor(colorPrimary: GLColor, colorSecondary: GLColor?, angle: Int = 0): this(colorPrimary, angle) {
        this.colorSecondary = colorSecondary
    }

    constructor(colorPrimary: Long, angle: Int = 0) : this(GLColor(colorPrimary), angle) { }
    constructor(colorPrimary: Long, colorSecondary: Long?, angle: Int = 0): this(colorPrimary, angle) {
        if(colorSecondary != null)
            this.colorSecondary = GLColor(colorSecondary)
    }

    fun clone() = GLGradient(colorPrimary.clone(), colorSecondary?.clone(), angle)

    fun isGradient() = colorSecondary != null

    override fun hashCode() = colorPrimary.hashCode() + angle.hashCode() + (colorSecondary?.hashCode() ?: 0)
    override fun equals(other: Any?): Boolean {
        var o = other as? GLGradient
        if(o != null && o === this)
            return true
        if(o?.hashCode() == hashCode())
            return true
        return super.equals(other)
    }
}