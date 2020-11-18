/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.opengl

interface RendererListener {
    fun onFrame(frame: Long, timeMs: Long, deltaTimeMs: Long)
    fun onStart()
    fun onFirstFrame()
    fun onStop()
    fun onCrash()

    fun onSceneConfigured(params: SceneParams)
}