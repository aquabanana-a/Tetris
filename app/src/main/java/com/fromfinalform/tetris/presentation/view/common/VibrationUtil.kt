package com.fromfinalform.tetris.presentation.view.common

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.content.ContextCompat.getSystemService

@SuppressWarnings("deprecation")
fun vibrate(cx: Context, ms: Long) {
    val v = cx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    else
        v.vibrate(ms) //deprecated in API 26
}