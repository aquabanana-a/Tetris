/*
 * Created by S.Dobranos on 20.11.20 14:00
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.media

import android.content.Context
import android.media.MediaPlayer

class Media(val context: Context, val id: Int) {
    var resId: Int? = null; private set
    var path: String? = null; private set

    private var player: MediaPlayer? = null
    private var autostart: Boolean = true
    private var onPoolBackHandler: ((mdeia: Media)->Unit)? = null
    private var onCompleteHandler: (()->Unit)? = null

    companion object {
        fun create(cx: Context, id: Int, onPoolBack: (media: Media)->Unit): Media {
            val ret = Media(cx, id)
            ret.onPoolBackHandler = onPoolBack
            ret.player = MediaPlayer()
            ret.player!!.setOnPreparedListener {
                if (ret.resId != null && ret.autostart)
                    ret.player!!.start()
            }
            ret.player!!.setOnCompletionListener {
                ret.onPoolBackHandler?.invoke(ret)
                ret.onCompleteHandler?.invoke()
            }

            return ret
        }
    }

    fun prepare(rawResId: Int, volume: Float): Boolean {
        if (player == null)
            return false

        if (true/*this.resId != rawResId*/) {
            val fd = context.resources.openRawResourceFd(rawResId) ?: return false
            this.resId = rawResId
            player!!.run {
                reset()
                setDataSource(fd.fileDescriptor, fd.startOffset, fd.declaredLength)
                setVolume(volume, volume)
                prepareAsync()
            }
            return false
        }

        return true
    }

    fun play(rawResId: Int, volume: Float = 1f, onComplete: (()->Unit)? = null): MediaPlayer? {
        if (player == null)
            return null

        autostart = true
        onCompleteHandler = onComplete

        if (prepare(rawResId, volume))
            player!!.start()

        return player
    }

    fun stop() {
        if(player!!.isPlaying)
            player!!.stop()
    }
}