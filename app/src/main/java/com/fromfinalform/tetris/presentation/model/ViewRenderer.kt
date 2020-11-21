/*
 * Created by S.Dobranos on 19.11.20 14:51
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model

import android.opengl.GLES20.glViewport
import android.util.Log
import com.fromfinalform.tetris.presentation.model.renderer.unit.IRenderUnit
import com.fromfinalform.tetris.presentation.model.renderer.IRenderer
import com.fromfinalform.tetris.presentation.model.opengl.common.GLUtils
import com.fromfinalform.tetris.presentation.model.renderer.ISize
import com.fromfinalform.tetris.presentation.model.renderer.RendererListener
import com.fromfinalform.tetris.presentation.model.renderer.SceneParams
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.max
import kotlin.math.min

class ViewRenderer(override var sceneSize: ISize) : IRenderer {

    private val renderUnitsLo = Any()
    private var renderUnitsImpl = hashMapOf<Long, IRenderUnit>()
    private var renderUnitsSorted = arrayListOf<IRenderUnit>()
    override val renderUnits get() = renderUnitsSorted
    override fun addRenderUnit(ru: IRenderUnit) { synchronized(renderUnitsLo) {
        renderUnitsImpl[ru.id] = ru
        renderUnitsSorted.add(ru)
    } }
    override fun getRenderUnit(id: Long): IRenderUnit? { synchronized(renderUnitsLo) {
        var ru = renderUnitsImpl[id]
        if (ru == null)
            for (v in renderUnitsImpl.values) {
                ru = v.childs?.first { c -> c.id == id } as? IRenderUnit?
                if (ru != null)
                    return ru
            }
        return ru
    } }
    override fun removeRenderUnit(id: Long) { synchronized(renderUnitsLo) {
        var ru = renderUnitsImpl.remove(id)
        if (ru == null) {
            var parentRU: IRenderUnit? = null
            for (v in renderUnitsImpl.values)
                if (v.childs?.any { c -> c.id == id } == true)
                    parentRU = v
            ru = parentRU?.removeChild(id) as? IRenderUnit
        }
        renderUnitsSorted.remove(ru)
    } }
    override fun clearRenderUnits() { synchronized(renderUnitsLo) {
        renderUnitsImpl.clear()
        renderUnitsSorted.clear()
    } }

    private var frames = 0L
    private var startTime = 0L
    private var lastFrameTimeMs = 0L
    private var bgARGB = floatArrayOf(1f, .5f, .5f, 1f)

    private var handler: RendererListener? = null
    private var updater: (()-> Unit)? = null

    private val sceneParams = SceneParams(sceneSize, 1f, 1f, 1f)

    private var isStarted = false
    private var isStopRequested = false

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

    }

    override fun onSurfaceChanged(gl: GL10?, w: Int, h: Int) {
        configureScene(w, h)
        glViewport(0, 0, w, h)

        handler?.onSceneConfigured(sceneParams)
    }

    override fun onDrawFrame(gl: GL10?) { try {
        ++frames

        val nowTimeMs = System.currentTimeMillis()
        val deltaTimeMs = if (lastFrameTimeMs <= 0) 0 else max(0, nowTimeMs - lastFrameTimeMs)
        val renderTimeMs = nowTimeMs - startTime
        GLUtils.clear(bgARGB)

        if (isStarted) {
            if (isStopRequested) {
                stopImpl()
                handler?.onStop()
            }
            else {
                handler?.onFrame(frames, renderTimeMs, deltaTimeMs)
            }
        }

        synchronized(renderUnitsLo) {
            for (ru in renderUnits)
                ru.render(this, sceneParams, renderTimeMs, deltaTimeMs)
        }

        lastFrameTimeMs = nowTimeMs

        } catch (e: Exception) {
            Log.v("!", "crash");
            handler?.onCrash()
            stop()
        }
    }

    private fun configureScene(canvasWidth: Int, canvasHeight: Int) {
        var scale = min(canvasWidth / sceneSize.width, canvasHeight / sceneSize.height)
        sceneParams.update(sceneSize, scale, sceneSize.width * scale, sceneSize.height * scale)
    }

    override fun requestRender() {
        updater?.invoke()
    }

    override fun start() {
        startTime = System.currentTimeMillis()
        lastFrameTimeMs = 0
        frames = 0
        isStopRequested = false
        isStarted = true

        handler?.onStart()

        synchronized(renderUnitsLo) {
            for (ru in renderUnits)
                ru.prerender(this)
        }
    }

    override fun stop() {
        if (isStarted)
            isStopRequested = true
    }

    private fun stopImpl() {
        isStarted = false
        isStopRequested = false
        frames = 0

        synchronized(renderUnitsLo) {
            for (ru in renderUnits)
                ru.postrender(this)
        }
    }

    override fun withListener(handler: RendererListener): ViewRenderer {
        this.handler = handler
        return this
    }

    override fun withUpdater(handler: ()->Unit): ViewRenderer {
        this.updater = handler
        return this
    }
}