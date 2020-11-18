/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.model.renderer

import android.opengl.GLSurfaceView
import com.fromfinalform.tetris.presentation.opengl.ISize
import com.fromfinalform.tetris.presentation.opengl.RendererListener
import com.fromfinalform.tetris.presentation.opengl.SceneSizeType

interface IRenderer : GLSurfaceView.Renderer {
    val sceneSize: ISize

    val renderUnits: List<IRenderUnit>
    fun addRenderUnit(ru: IRenderUnit)
    fun getRenderUnit(id: Long): IRenderUnit?
    fun removeRenderUnit(id: Long)
    fun clearRenderUnits()

    fun start()
    fun stop()
    fun requestRender()

    fun withListener(handler: RendererListener): IRenderer
    fun withUpdater(handler: () -> Unit): IRenderer
}