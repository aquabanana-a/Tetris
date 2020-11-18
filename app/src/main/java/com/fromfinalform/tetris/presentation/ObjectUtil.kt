/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation

import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.toRectF
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import kotlin.math.round
import kotlin.reflect.KClass

val EPSILON = 1E-6.toFloat()

fun deepClone(`object`: Any?): Any? {
    if (`object` == null)
        return null
    return try {
        val baos = ByteArrayOutputStream()
        val oos = ObjectOutputStream(baos)
        oos.writeObject(`object`)
        val bais = ByteArrayInputStream(baos.toByteArray())
        val ois = ObjectInputStream(bais)
        ois.readObject()
    } catch (e: Exception) {
        Log.e("error", e.localizedMessage)
        null
    }
}

inline fun <reified T : Any> getValue(): T? = getValue(T::class)
fun <T : Any> getValue(clazz: KClass<T>): T? {
    clazz.constructors.forEach { con ->
        if (con.parameters.isEmpty()) {
            return con.call()
        }
    }
    return null
}

fun Float.range(from: Float, to: Float) = from + (to - from) * this

fun Float.round(decimals: Int): Float {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return (round(this * multiplier) / multiplier).toFloat()
}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>) {
    observe(lifecycleOwner, object : Observer<T> {
        override fun onChanged(t: T?) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

fun RectF.heightInv() = this.top - this.bottom
fun RectF.clone() = RectF(this)

fun RectF?.isSame(value: RectF?) = this == value || (this != null && value != null &&
                                                    this.left == value.left &&
                                                    this.top == value.top &&
                                                    this.right == value.right &&
                                                    this.bottom == value.bottom)

fun RectF?.isAlmostSame(value: RectF?): Boolean {
    if(this == value) return true //if one==two or both == null
    if((this == null) xor (value == null)) return false // if one of them == null
    //else there are no nothing nullable

    val dl = this!!.left - value!!.left
    if(dl * dl > EPSILON) return false

    val dr = this.right - value.right
    if(dr * dr > EPSILON) return false

    val dt = this.top - value.top
    if(dt * dt > EPSILON) return false

    val db = this.bottom - value.bottom
    if(db * db > EPSILON) return false

    return true
}

fun Point.clone() = Point(this.x, this.y)
fun PointF.clone() = PointF(this.x, this.y)

fun RectF?.toRect() = if (this != null) Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt()) else null
fun Rect?.toRectF() = if(this != null) RectF(this) else null

fun RectF?.mulHorizontal(value: Int) = mulHorizontal(value.toFloat())
fun RectF?.mulHorizontal(value: Float): RectF? {
    if (this == null)
        return null
    return RectF(left, top * value, right, bottom * value)
}

fun RectF?.mulVertical(value: Int) = mulVertical(value.toFloat())
fun RectF?.mulVertical(value: Float): RectF? {
    if (this == null)
        return null
    return RectF(left * value, top, right * value, bottom)
}

fun RectF?.mul(value: Int) = mul(value.toFloat())
fun RectF?.mul(value: Float): RectF? {
    if (this == null)
        return null
    return RectF(left * value, top * value, right * value, bottom * value)
}

fun RectF?.mul(value: Point) = mul(PointF(value.x.toFloat(), value.y.toFloat()))
fun RectF?.mul(value: PointF): RectF? {
    return mulHorizontal(value.x)
        .mulVertical(value.y)
}

fun Rect?.mulHorizontal(value: Int) = mulHorizontal(value.toFloat())
fun Rect?.mulHorizontal(value: Float): Rect? {
    if (this == null)
        return null
    return Rect(left, (top * value).toInt(), right, (bottom * value).toInt())
}

fun Rect?.mulVertical(value: Int) = mulVertical(value.toFloat())
fun Rect?.mulVertical(value: Float): Rect? {
    if (this == null)
        return null
    return Rect((left * value).toInt(), top, (right * value).toInt(), bottom)
}

fun Rect?.mul(value: Int) = mul(value.toFloat())
fun Rect?.mul(value: Float): Rect? {
    if (this == null)
        return null
    return Rect((left * value).toInt(), (top * value).toInt(), (right * value).toInt(), (bottom * value).toInt())
}

fun Rect?.mul(value: Point) = mul(PointF(value.x.toFloat(), value.y.toFloat()))
fun Rect?.mul(value: PointF): Rect? {
    return mulHorizontal(value.x)
        .mulVertical(value.y)
}

fun FloatArray.toPoints(): ArrayList<PointF> {
    val ret = ArrayList<PointF>()
    var i = 0
    while(i < this.size) {
        ret.add(PointF(this[i], this[i+1]))
        i += 2
    }
    return ret
}

fun <T1, T2> ArrayList<Pair<T1, T2>>?.toHashMap(skipNullValues: Boolean = false): HashMap<T1, T2> {
    var ret = HashMap<T1, T2>()
    if (this == null)
        return ret

    this!!.forEach {
        if (!skipNullValues || skipNullValues && it.second != null)
            ret[it.first] = it.second
    }
    return ret
}

fun ArrayList<PointF>.toBuffer(): FloatArray {
    val ret = FloatArray(size * 2)
    var i = 0
    while(i < size) {
        ret[i * 2] = this[i].x
        ret[i * 2 + 1] = this[i].y
        i++
    }
    return ret
}

//fun string(resId: Int): String = getResources().getString(resId)
//fun format(resId: Int, vararg args: Any?) = String.format(string(resId), *args)

fun <T : RecyclerView> T.removeItemDecorations() {
    while (itemDecorationCount > 0) {
        removeItemDecorationAt(0)
    }
}

fun Any?.equalsAny(vararg params: Any?): Boolean {
    for(p in params)
        if(this == p)
            return true
    return false
}

fun Any.equalsAll(vararg params: Any): Boolean {
    for (p in params)
        if (this != p)
            return false
    return true
}

fun View.setMarginLeft(value: Int) {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(value, params.topMargin, params.rightMargin, params.bottomMargin)
    layoutParams = params
}

fun View.setMarginRight(value: Int) {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(params.leftMargin, params.topMargin, value, params.bottomMargin)
    layoutParams = params
}

fun View.setMarginTop(value: Int) {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(params.leftMargin, value, params.rightMargin, params.bottomMargin)
    layoutParams = params
}

fun View.setMarginBottom(value: Int) {
    val params = layoutParams as ViewGroup.MarginLayoutParams
    params.setMargins(params.leftMargin, params.topMargin, params.rightMargin, value)
    layoutParams = params
}
