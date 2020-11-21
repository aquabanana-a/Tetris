/*
 * Created by S.Dobranos on 19.11.20 15:01
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.renderer.unit

import com.fromfinalform.tetris.presentation.model.renderer.IRenderer
import com.fromfinalform.tetris.presentation.model.renderer.SceneParams

interface IRenderUnit {
    val id: Long
    val childs: List<RenderItem>?

    fun addChild(value: RenderItem)
    fun removeChild(id: Long): RenderItem?

    fun prerender(renderer: IRenderer)
    fun render(renderer: IRenderer, params: SceneParams, timeMs: Long, deltaTimeMs: Long)
    fun postrender(renderer: IRenderer)
}