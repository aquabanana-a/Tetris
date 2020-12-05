/*
 * Created by S.Dobranos on 18.11.20 15:03
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.fromfinalform.tetris.R

class BackgroundSoundService : Service() {

    internal var player: MediaPlayer? = null

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer.create(this, R.raw.retro_funk)
        player!!.isLooping = true
        player!!.setVolume(100f, 100f)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        player?.start()
        return START_STICKY
    }

    override fun onStart(intent: Intent, startId: Int) {
    }

    override fun onDestroy() {
        player?.stop()
        player?.release()
    }

    override fun onLowMemory() {

    }
}