/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.model.renderer

import com.fromfinalform.tetris.presentation.opengl.SceneParams
import com.fromfinalform.tetris.presentation.opengl.renderUnit.RenderItem

interface IRenderUnit {
    val id: Long
    val childs: List<RenderItem>?

    fun addChild(value: RenderItem)
    fun removeChild(id: Long): RenderItem?

    fun prerender(renderer: IRenderer)
    fun render(renderer: IRenderer, params: SceneParams, timeMs: Long, deltaTimeMs: Long)
    fun postrender(renderer: IRenderer)
}