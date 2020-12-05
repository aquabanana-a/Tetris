/*
 * Created by S.Dobranos on 18.11.20 23:47
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.view

import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.fromfinalform.tetris.R
import com.fromfinalform.tetris.domain.model.game.IGameResults
import com.fromfinalform.tetris.presentation.model.opengl.EGL10ContextFactory
import com.fromfinalform.tetris.presentation.presenter.GameOverPresenter.Companion.RESULTS_DATA
import com.fromfinalform.tetris.presentation.presenter.MainActivityPresenter
import com.fromfinalform.tetris.presentation.presenter.SwitchScreenMgr
import com.fromfinalform.tetris.presentation.view.common.dp
import com.fromfinalform.tetris.presentation.view.common.setMarginTop
import com.fromfinalform.tetris.presentation.view.common.vibrate

class MainActivity : AppCompatActivity(), MainActivityPresenter.IMainActivityView {

    private lateinit var vRoot: ConstraintLayout
    private lateinit var vgCanvas: ConstraintLayout
    private lateinit var glSurface: GLSurfaceView

    private lateinit var vHGap: View
    private lateinit var btnStart: ImageButton
    private lateinit var btnStartTxt: TextView
    private lateinit var btnLeft: View
    private lateinit var btnRight: View
    private lateinit var btnRotate: View
    private lateinit var btnDrop: View
    private lateinit var tvLevel: TextView
    private lateinit var tvScore: TextView

    private var presenter: MainActivityPresenter

    init {
        presenter = MainActivityPresenter(this)
        App.presenterScope.register(presenter)
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
        vgCanvas = findViewById(R.id.vg_canvas_group)
    }

    override fun onPostCreated() {
        presenter.winOnSceneConfigured { params ->
            vRoot.post {
                val appPadding = (2 + 2).dp
                val mainFramePadding = (14 + 14).dp
                val canvasFramePadding = (5 + 5).dp

                val clp = vgCanvas.layoutParams as ConstraintLayout.LayoutParams
                clp.width = params.scaledSceneWidth.toInt() + canvasFramePadding
                clp.height = params.scaledSceneHeight.toInt() + canvasFramePadding
                vgCanvas.layoutParams = clp

                val gapW = ((resources.displayMetrics.widthPixels - (clp.width + btnStart.layoutParams.width + mainFramePadding + appPadding)) / 3f).toInt()

                val lp = vHGap.layoutParams
                lp.width = gapW
                vHGap.layoutParams = lp

                vgCanvas.setMarginTop(gapW)

                //val cs = ConstraintSet()
                //cs.clone(vgCanvas)
                //cs.setVerticalBias(R.id.gl_surface, 0.5f)
                //cs.setHorizontalBias(R.id.gl_surface, 0.5f)
                //cs.applyTo(vgCanvas)
            }
        }

        vHGap = vRoot.findViewById(R.id.v_hgap)
        btnStartTxt = vRoot.findViewById(R.id.btn_start_txt)

        presenter.withOnGameStarted { vRoot.post { btnStartTxt.text = "Stop" } }
        presenter.withOnGameStopped { vRoot.post { btnStartTxt.text = "Start" } }
        presenter.withOnResultsChanged { results ->
            vRoot.post {
                tvLevel.text = "Level: ${results.level}"
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
            vibrate(this, 10)
            presenter.moveFigureLeft()
        })

        btnRight = vRoot.findViewById(R.id.btn_right)
        btnRight.setOnTouchListener(RepeatableTouchHandler(400, 100) {
            vibrate(this, 10)
            presenter.moveFigureRight()
        })

        btnRotate = vRoot.findViewById(R.id.btn_rotate)
        btnRotate.setOnTouchListener(RepeatableTouchHandler(400, 100) {
            vibrate(this, 10)
            presenter.rotateFigure()
        })

        btnDrop = vRoot.findViewById(R.id.btn_drop)
        btnDrop.setOnTouchListener { v, me ->
            if (me.action == MotionEvent.ACTION_DOWN) {
                vibrate(this, 10)
                presenter.dropFigure()
                true
            }
            false
        }

        tvLevel = vRoot.findViewById(R.id.tv_Level)
        tvScore = vRoot.findViewById(R.id.tv_score_value)
    }

    override fun openGameOverScreen(results: IGameResults) {
        val bundle = Bundle()
        bundle.putParcelable(RESULTS_DATA, results)

        val f = GameOverFragment()
        f.arguments = bundle
        SwitchScreenMgr.switchFragment(this, f)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount >= 1 && SwitchScreenMgr.onBackPressed(this))
            return

        super.onBackPressed()
    }
}