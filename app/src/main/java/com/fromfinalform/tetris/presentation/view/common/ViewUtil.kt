/*
 * Created by S.Dobranos on 18.11.20 23:46
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.view.common

import android.content.res.Resources
import android.view.View
import android.view.ViewGroup

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.dpf: Float
    get() = this * Resources.getSystem().displayMetrics.density

val Float.dp: Float
    get() = this * Resources.getSystem().displayMetrics.density

val Int.sp: Float
    get() = this / Resources.getSystem().displayMetrics.scaledDensity

val Float.sp: Float
    get() = this / Resources.getSystem().displayMetrics.scaledDensity

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