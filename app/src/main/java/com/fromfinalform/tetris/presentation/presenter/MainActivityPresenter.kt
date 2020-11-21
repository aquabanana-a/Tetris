/*
 * Created by S.Dobranos on 19.11.20 15:18
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.presenter

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.opengl.GLSurfaceView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.fromfinalform.tetris.R
import com.fromfinalform.tetris.domain.interactor.IGameLooper
import com.fromfinalform.tetris.domain.model.game.IGameConfig
import com.fromfinalform.tetris.domain.model.game.IGameResults
import com.fromfinalform.tetris.presentation.dagger.DaggerGameComponent
import com.fromfinalform.tetris.presentation.dagger.GameComponent
import com.fromfinalform.tetris.presentation.mapper.IFigureRenderMapper
import com.fromfinalform.tetris.presentation.model.media.MediaPlayerPool
import com.fromfinalform.tetris.presentation.model.ViewRenderer
import com.fromfinalform.tetris.presentation.model.renderer.IRenderer
import com.fromfinalform.tetris.presentation.model.renderer.RendererListener
import com.fromfinalform.tetris.presentation.model.renderer.SceneParams
import com.fromfinalform.tetris.presentation.model.renderer.Size
import com.fromfinalform.tetris.presentation.model.renderer.unit.IRenderUnit
import com.fromfinalform.tetris.presentation.model.renderer.unit.RenderUnit
import com.fromfinalform.tetris.presentation.model.repository.StaticShaderDrawerRepository

class MainActivityPresenter(val view: IMainActivityView) : LifecycleObserver {

    interface IMainActivityView {
        fun requestRender()
        fun setRenderMode(mode: Int)
        fun onPostCreated()
    }

    private lateinit var game: GameComponent
    private lateinit var config: IGameConfig
    private lateinit var looper: IGameLooper
    private lateinit var mapper: IFigureRenderMapper

    private lateinit var glViewRenderer: ViewRenderer
    private var mediaPool: MediaPlayerPool? = null
    private var soundPool: SoundPool? = null
    private var soundDrop = 0
    private var soundComplete = 0

    val renderer: IRenderer get() = glViewRenderer
    val isGameStarted get() = looper.isStarted

    private var sceneConfiguredHandler: ((params: SceneParams) -> Unit)? = null
    fun winOnSceneConfigured(handler: (params: SceneParams)->Unit): MainActivityPresenter {
        this.sceneConfiguredHandler = handler
        return this
    }

    private var gameStartedHandler: (()->Unit)? = null
    fun withOnGameStarted(handler: ()->Unit): MainActivityPresenter {
        this.gameStartedHandler = handler
        return this
    }

    private var gameStoppedHandler: (() -> Unit)? = null
    fun withOnGameStopped(handler: () -> Unit): MainActivityPresenter {
        this.gameStoppedHandler = handler
        return this
    }

    private var resultsChangedHandler: ((results: IGameResults) -> Unit)? = null
    fun withOnResultsChanged(handler: (results: IGameResults)->Unit): MainActivityPresenter {
        this.resultsChangedHandler = handler
        return this
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume(owner: LifecycleOwner) {
        if (isGameStarted)
            playBackSound()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause(owner: LifecycleOwner) {
        mediaPool?.stop()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate(owner: LifecycleOwner) {
        game = DaggerGameComponent.builder().withContext(view as Context).build()
        config = game.getConfig()
        looper = game.getLooper()
        mapper = game.getRenderMapper()

        game.getFigureTypeRepository().initialize()
        game.getLevelRepository().initialize()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .build()

        soundComplete = soundPool!!.load(view as Context, R.raw.hit_01, 1)
        soundDrop = soundPool!!.load(view as Context, R.raw.hit_02, 1)

        glViewRenderer = ViewRenderer(Size(config.fieldWidthPx, config.fieldHeightPx))
            .withUpdater { view.requestRender() }
            .withListener(object : RendererListener {
                override fun onStart() {
                    view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY)
                }

                override fun onFirstFrame() {}
                override fun onStop() {
                    view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)
                }

                override fun onCrash() {}
                override fun onFrame(frame: Long, timeMs: Long, deltaTimeMs: Long) {
                    looper.onFrame(frame, timeMs, deltaTimeMs)
                }

                override fun onSceneConfigured(params: SceneParams) {
                    sceneConfiguredHandler?.invoke(params)

                    // strange playback speed boost if in onCreate, possible caused by framerate
                    mediaPool = MediaPlayerPool(view as Context, 1)

                    RenderUnit.shaderRepo = StaticShaderDrawerRepository().apply { initialize() }

                    var cw = config.cellSizePx * params.sx
                    var ch = config.cellSizePx * params.sy

                    glViewRenderer.clearRenderUnits()

                    var ru: RenderUnit? = null
                    looper
                        .withOnCurrentFigureChanged { f, replaced ->
                            if (replaced && ru != null)
                                glViewRenderer.removeRenderUnit(ru!!.id)

                            if (f == null)
                                return@withOnCurrentFigureChanged

                            ru = mapper.map(params, f, config)
                                .translateX(f.x * cw)
                                .translateY(f.y * ch) as RenderUnit

                            glViewRenderer.addRenderUnit(ru!!)
                        }
                        .withOnCurrentFigureLocationChanged { f, x, y, dx, dy ->
                            ru?.translateX(dx * cw)
                            ru?.translateY(dy * ch)
                        }
                        .withOnCurrentFigureHolded { f, x, y, dx, dy, hard ->
                            if (hard)
                                soundPool!!.play(soundDrop, .30f, .30f, 0, 0, 1f)

                            ru!!.translateX(dx * cw)
                            ru!!.translateY(dy * ch)

                            glViewRenderer.removeRenderUnit(ru!!.id)
                            ru!!.childs?.forEach {
                                glViewRenderer.addRenderUnit(it as IRenderUnit)
                            }
                        }
                        .withCompleteRowsListener { rows ->
                            soundPool!!.play(soundComplete, .66f, .66f, 0, 0, 1f)

                            for (r in rows)
                                for (c in r.second)
                                    glViewRenderer.removeRenderUnit(c.id)
                        }
                        .withRelocateCellsListener { cells ->
                            for (c in cells)
                                (glViewRenderer.getRenderUnit(c.id) as? RenderUnit)?.withLocation(-1f + c.x * cw, 1f - c.y * ch)
                        }
                        .withOnStartListener {
                            gameStartedHandler?.invoke()
                            glViewRenderer.clearRenderUnits()
                            glViewRenderer.start()

                            playBackSound()
                        }
                        .withOnStopListener {
                            gameStoppedHandler?.invoke()
                            glViewRenderer.stop()

                            mediaPool?.stop()
                        }
                        .withOnResultsChanged { results ->
                            resultsChangedHandler?.invoke(results)
                        }
                }
            })

        view.onPostCreated()
    }

    private fun playBackSound() {
        if(!isGameStarted)
            return

        mediaPool?.playSound(arrayListOf(R.raw.retro_funk/*, R.raw.surf*/).random(), .25f) {
            playBackSound()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private fun onDestroy(owner: LifecycleOwner) {

    }

    fun startGame() = looper.start()
    fun stopGame() = looper.stop()
    fun moveFigureLeft() = looper.onLeft()
    fun moveFigureRight() = looper.onRight()
    fun rotateFigure() = looper.onRotate()
    fun dropFigure() = looper.onHardDrop()
}