/*
 * Created by S.Dobranos on 18.11.20 23:26
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.renderer

import android.opengl.GLSurfaceView
import com.fromfinalform.tetris.presentation.model.renderer.unit.IRenderUnit

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