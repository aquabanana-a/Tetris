/*
 * Created by S.Dobranos on 18.11.20 23:47
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.common

import android.os.Handler
import android.os.Looper

class ThreadUtil {

    companion object {

        val guiThreadId: Long by lazy {
            Looper.getMainLooper().thread.id
        }

        val currentThreadId get() = Thread.currentThread().id
        fun isGuiThread(): Boolean = currentThreadId == guiThreadId

        fun runOnGuiThread(r: () -> Unit) = runOnGuiThread(Runnable(r))
        fun runOnGuiThread(r: Runnable) {
            if(isGuiThread())
                r.run()
            else
                Handler(Looper.getMainLooper()).post(r)
        }
    }
}