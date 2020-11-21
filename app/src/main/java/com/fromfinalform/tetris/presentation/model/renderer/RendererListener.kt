/*
 * Created by S.Dobranos on 19.11.20 0:01
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.renderer

import com.fromfinalform.tetris.presentation.model.renderer.SceneParams

interface RendererListener {
    fun onFrame(frame: Long, timeMs: Long, deltaTimeMs: Long)
    fun onStart()
    fun onFirstFrame()
    fun onStop()
    fun onCrash()

    fun onSceneConfigured(params: SceneParams)
}