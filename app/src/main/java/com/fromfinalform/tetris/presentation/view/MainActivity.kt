/*
 * Created by S.Dobranos on 18.11.20 23:47
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.view

import android.opengl.GLSurfaceView
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.fromfinalform.tetris.R
import com.fromfinalform.tetris.presentation.model.repository.StaticShaderDrawerRepository
import com.fromfinalform.tetris.domain.model.game.IGameConfig
import com.fromfinalform.tetris.domain.interactor.IGameLooper
import com.fromfinalform.tetris.presentation.dagger.DaggerGameComponent
import com.fromfinalform.tetris.presentation.dagger.GameComponent
import com.fromfinalform.tetris.presentation.model.renderer.unit.IRenderUnit
import com.fromfinalform.tetris.presentation.mapper.IFigureRenderMapper
import com.fromfinalform.tetris.presentation.model.renderer.RendererListener
import com.fromfinalform.tetris.presentation.model.renderer.SceneParams
import com.fromfinalform.tetris.presentation.model.renderer.Size
import com.fromfinalform.tetris.presentation.model.renderer.unit.RenderUnit
import com.fromfinalform.tetris.presentation.model.ViewRenderer
import com.fromfinalform.tetris.presentation.model.opengl.EGL10ContextFactory
import com.fromfinalform.tetris.presentation.presenter.MainActivityPresenter

class MainActivity : AppCompatActivity(), MainActivityPresenter.IMainActivityView {
    private lateinit var vRoot: ConstraintLayout
    private lateinit var vgCanvas: ConstraintLayout
    private lateinit var vgTouch: View
    private lateinit var glSurface: GLSurfaceView

    private lateinit var btnStart: Button
    private lateinit var btnLeft: View
    private lateinit var btnRight: View
    private lateinit var btnRotate: View
    private lateinit var btnDrop: View
    private lateinit var tvLevel: TextView
    private lateinit var tvScore: TextView

    private var presenter: MainActivityPresenter

    init {
        presenter = MainActivityPresenter(this)
    }

    override fun requestRender() { vRoot.post { glSurface.requestRender() } }
    override fun setRenderMode(mode: Int) { vRoot.post { glSurface.renderMode = mode } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(presenter)

        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = 0xff1A1C29.toInt()
            window.navigationBarColor = 0xff1A1C29.toInt()
        }

        vRoot = findViewById(R.id.vg_root)
        vgCanvas = findViewById(R.id.vg_canvas)
    }

    override fun onPostCreated() {
        presenter.winOnSceneConfigured { params ->
            vRoot.post {
                val clp = vgCanvas.layoutParams as ConstraintLayout.LayoutParams
                clp.width = params.scaledSceneWidth.toInt()
                clp.height = params.scaledSceneHeight.toInt()
                vgCanvas.layoutParams = clp

                val cs = ConstraintSet()
                cs.clone(vgCanvas)
                //cs.setVerticalBias(R.id.gl_surface, 0.5f)
                cs.setHorizontalBias(R.id.gl_surface, 0.5f)
                cs.applyTo(vgCanvas)
            }
        }

        presenter.withOnGameStarted { vRoot.post { btnStart.text = "Stop" } }
        presenter.withOnGameStopped { vRoot.post { btnStart.text = "Start" } }
        presenter.withOnResultsChanged { results ->
            vRoot.post {
                tvLevel.text = "${results.level}"
                tvScore.text = "${results.points}"
            }
        }

        glSurface = findViewById(R.id.gl_surface)
        glSurface.setEGLContextFactory(EGL10ContextFactory())
        glSurface.setEGLContextClientVersion(2)
        glSurface.setRenderer(presenter.renderer)
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY)

        btnStart = vRoot.findViewById(R.id.btn_start)
        btnStart.setOnClickListener {
            glSurface.queueEvent {
                if (!presenter.isGameStarted)
                    presenter.startGame()
                else
                    presenter.stopGame()
            }
        }

        btnLeft = vRoot.findViewById(R.id.btn_left)
        btnLeft.setOnTouchListener(RepeatableTouchHandler(400, 100) {
            presenter.moveFigureLeft()
        })

        btnRight = vRoot.findViewById(R.id.btn_right)
        btnRight.setOnTouchListener(RepeatableTouchHandler(400, 100) {
            presenter.moveFigureRight()
        })

        btnRotate = vRoot.findViewById(R.id.btn_rotate)
        btnRotate.setOnTouchListener(RepeatableTouchHandler(400, 100) {
            presenter.rotateFigure()
        })

        btnDrop = vRoot.findViewById(R.id.btn_drop)
        btnDrop.setOnTouchListener { v, me ->
            if (me.action == MotionEvent.ACTION_DOWN) {
                presenter.dropFigure()
                true
            }
            false
        }

        tvLevel = vRoot.findViewById(R.id.tv_Level_value)
        tvScore = vRoot.findViewById(R.id.tv_score_value)
    }
}