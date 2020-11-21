/*
 * Created by S.Dobranos on 20.11.20 14:00
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.media

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

class MediaPlayerPool(context: Context, maxStreams: Int) {
    private val context: Context = context.applicationContext

    private val mediaPlayerLo = Any()
    private val mediaUsedIds = mutableListOf<Int>()
    private val mediaPlayerPool = mutableListOf<Media>().also {
        for (i in 0 until maxStreams)
            it += Media.create(context, i)  { m -> synchronized(mediaPlayerLo) { mediaUsedIds.remove(m.id) } }
    }

    private fun requestMedia(rawResId: Int): Media? { synchronized(mediaPlayerLo) {
        return if (mediaUsedIds.size < mediaPlayerPool.size) {
            val available = mediaPlayerPool.filter { !mediaUsedIds.contains(it.id) }
            val media = available.firstOrNull { it.resId == rawResId } ?: available.firstOrNull()
            if (media != null)
                mediaUsedIds.add(media.id)

            media
        }
        else null
    } }

    fun playSound(@RawRes rawResId: Int, volume: Float = 1f, onComplete: (()->Unit)? = null): MediaPlayer? {
        val media = requestMedia(rawResId) ?: return null
        return media.play(rawResId, volume, onComplete)
    }

    fun stop() { synchronized(mediaPlayerLo) {
        mediaPlayerPool.forEach {
            it.stop()
            mediaUsedIds.remove(it.id)
        }
    } }
}