/*
 * Created by S.Dobranos on 19.11.20 15:01
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.renderer.data

import java.io.Serializable
import kotlin.random.Random

class GLColor(): Serializable {
    companion object {
        val TRANSPARENT = GLColor(0x00000000)
        val BLACK       = GLColor(0xFF000000)
        val RED         = GLColor(0xFFFF0000)
        val GREEN       = GLColor(0xFF00FF00)
        val BLUE        = GLColor(0xFF0000FF)
        val WHITE       = GLColor(0xFFFFFFFF)

        fun getRandom() = GLColor(0xFF000000 + Random.nextInt(0xFFFFFF))
    }

    private var r = 0f
    private var g = 0f
    private var b = 0f
    private var a = 0f
    private var m = 1f

//    fun getRmarker() = if(r * m == -2f) r * m else (r * m).coerceIn(0f, 1f)

    fun setA(value: Float) { a = value.coerceIn(0f, 1f) }

    fun getR() = (r * m).coerceIn(0f, 1f)
    fun getG() = (g * m).coerceIn(0f, 1f)
    fun getB() = (b * m).coerceIn(0f, 1f)
    fun getA() = (a * m).coerceIn(0f, 1f)
    fun getM() = m

    fun getRi() = (getR() * 255).toInt()
    fun getGi() = (getG() * 255).toInt()
    fun getBi() = (getB() * 255).toInt()
    fun getAi() = (getA() * 255).toInt()

    constructor(color: Int) : this(color.toLong())
    constructor(color: Long) : this() {
        this.a = ((color shr 24) and 0xFF) / 255f
        this.r = ((color shr 16) and 0xFF) / 255f
        this.g = ((color shr 8) and 0xFF) / 255f
        this.b = ((color shr 0) and 0xFF) / 255f
    }

    constructor(a: Float, r: Float, g: Float, b: Float, m: Float = 1f) : this() {
        this.a = a
        this.r = r
        this.g = g
        this.b = b
        this.m = m
    }

    fun mul(value: Float): GLColor {
        this.m = value
        return this
    }

    fun mulBy(value: Float): GLColor {
        this.m *= value
        return this
    }

    fun set(value: GLColor) {
        this.a = value.a.coerceIn(0f, 1f)
        this.r = value.r.coerceIn(0f, 1f)
        this.g = value.g.coerceIn(0f, 1f)
        this.b = value.b.coerceIn(0f, 1f)
        this.m = value.m
    }

    fun clone(): GLColor = GLColor(a, r, g, b, m)

    fun toInt() = ((getAi() and 0xFF) shl 24) or ((getRi() and 0xFF) shl 16) or ((getGi() and 0xFF) shl 8) or ((getBi() and 0xFF) shl 0)
    fun toHexStr() = String.format("#%08X", (0xFFFFFFFF and toInt().toLong()))

    override fun hashCode() = toInt()
    override fun equals(other: Any?): Boolean {
        var o = other as? GLColor
        if(o != null && o === this)
            return true
        if(o?.hashCode() == hashCode())
            return true
        return super.equals(other)
    }
}