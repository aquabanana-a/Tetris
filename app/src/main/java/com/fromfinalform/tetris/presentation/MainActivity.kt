/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation

import android.content.Intent
import android.media.MediaPlayer
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
import com.fromfinalform.tetris.data.model.game.ClassicGameConfig
import com.fromfinalform.tetris.data.model.game.ClassicGameLooper
import com.fromfinalform.tetris.data.model.game.level.SegaGameLevelRepository
import com.fromfinalform.tetris.data.repository.ClassicFigureTypeRepository
import com.fromfinalform.tetris.data.repository.StaticShaderDrawerRepository
import com.fromfinalform.tetris.domain.model.game.IGameConfig
import com.fromfinalform.tetris.domain.model.game.IGameLooper
import com.fromfinalform.tetris.domain.model.renderer.IRenderUnit
import com.fromfinalform.tetris.presentation.mapper.FigureRenderMapper
import com.fromfinalform.tetris.presentation.opengl.*
import com.fromfinalform.tetris.presentation.opengl.renderUnit.RenderUnit

class MainActivity : AppCompatActivity() {
    private lateinit var vRoot: ConstraintLayout
    private lateinit var vgCanvas: ConstraintLayout
    private lateinit var vgTouch: View
    private lateinit var glSurface: GLSurfaceView
    private lateinit var glViewRenderer: ViewRenderer
    private lateinit var btnStart: Button
    private lateinit var btnLeft: View
    private lateinit var btnRight: View
    private lateinit var btnRotate: View
    private lateinit var btnDrop: View
    private lateinit var tvLevel: TextView
    private lateinit var tvScore: TextView

    private var player: MediaPlayer? = null

    override fun onPause() {
        super.onPause()
        player?.pause()
    }

    override fun onResume() {
        super.onResume()
        player?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.stop()
        player?.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        player = MediaPlayer.create(this@MainActivity, R.raw.retro_funk)
        player!!.isLooping = true
        player!!.setVolume(.33f, .33f)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = 0xff1A1C29.toInt()
            window.navigationBarColor = 0xff1A1C29.toInt()
        }

        vRoot = findViewById(R.id.vg_root)
        vgCanvas = findViewById(R.id.vg_canvas)

        val game: IGameConfig = ClassicGameConfig()
        val looper: IGameLooper = ClassicGameLooper()

        glViewRenderer = ViewRenderer(Size(game.fieldWidthPx, game.fieldHeightPx))
            .withUpdater { glSurface.requestRender() }
            .withListener(object : RendererListener {
                override fun onStart() {
                    glSurface.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                }
                override fun onFirstFrame() { }
                override fun onStop() {
                    glSurface.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                }
                override fun onCrash() { }
                override fun onFrame(frame: Long, timeMs: Long, deltaTimeMs: Long) { looper.onFrame(frame, timeMs, deltaTimeMs) }
                override fun onSceneConfigured(params: SceneParams) { vRoot.post {
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

                    RenderUnit.shaderRepo = StaticShaderDrawerRepository().apply { initialize() }

                    var fm = FigureRenderMapper(this@MainActivity)
                    var gc = ClassicGameConfig()
                    var fr = ClassicFigureTypeRepository().apply { initialize() }
                    var lr = SegaGameLevelRepository().apply { initialize() }

                    var cw = gc.cellSizePx * params.sx
                    var ch = gc.cellSizePx * params.sy

                    glViewRenderer.clearRenderUnits()

                    var ru: RenderUnit? = null
                    looper.withOnCurrentFigureChanged { f, replaced ->
                            if (replaced && ru != null)
                                glViewRenderer.removeRenderUnit(ru!!.id)

                            if (f == null)
                                return@withOnCurrentFigureChanged

                            ru = fm.map(params, f, gc)
                                .translateX(f.x * cw)
                                .translateY(f.y * ch) as RenderUnit

                            glViewRenderer.addRenderUnit(ru!!)
                        }
                        .withOnCurrentFigureLocationChanged { f, x, y, dx, dy ->
                            ru?.translateX(dx * cw)
                            ru?.translateY(dy * ch)
                        }
                        .withOnCurrentFigureHolded { f, x, y, dx, dy ->
                            ru!!.translateX(dx * cw)
                            ru!!.translateY(dy * ch)

                            glViewRenderer.removeRenderUnit(ru!!.id)
                            ru!!.childs?.forEach {
                                glViewRenderer.addRenderUnit(it as IRenderUnit)
                            }
                        }
                        .withCompleteRowsListener { rows ->
                            for (r in rows)
                                for (c in r.second)
                                    glViewRenderer.removeRenderUnit(c.id)
                        }
                        .withRelocateCellsListener { cells ->
                            for (c in cells)
                                (glViewRenderer.getRenderUnit(c.id) as? RenderUnit)?.withLocation(-1f + c.x * cw, 1f - c.y * ch)
                        }
                        .withOnStartListener {
                            vRoot.post { btnStart.text = "Stop" }
                            glViewRenderer.clearRenderUnits()
                            glViewRenderer.start()

                            player?.start()
                         }
                        .withOnStopListener {
                            vRoot.post { btnStart.text = "Start" }
                            glViewRenderer.stop()

                            player?.stop()
                        }
                        .withOnResultsChanged { results -> vRoot.post {
                            tvLevel.text = "${results.level}"
                            tvScore.text = "${results.points}"
                        } }
                }
            })

        glSurface = findViewById(R.id.gl_surface)
        glSurface.setEGLContextFactory(EGL10ContextFactory())
        glSurface.setEGLContextClientVersion(2)
        glSurface.setRenderer(glViewRenderer)
        glSurface.renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY

        btnStart = vRoot.findViewById(R.id.btn_start)
        btnStart.setOnClickListener {
            glSurface.queueEvent {
                if (!looper.isStarted)
                    looper.start()
                else
                    looper.stop()
            }
        }

        btnLeft = vRoot.findViewById(R.id.btn_left)
        btnLeft.setOnTouchListener(RepeatableTouchHandler(400, 100) {
            looper.onLeft()
        })

        btnRight = vRoot.findViewById(R.id.btn_right)
        btnRight.setOnTouchListener(RepeatableTouchHandler(400, 100) {
            looper.onRight()
        })

        btnRotate = vRoot.findViewById(R.id.btn_rotate)
        btnRotate.setOnTouchListener(RepeatableTouchHandler(400, 100) {
            looper.onRotate()
        })

        btnDrop = vRoot.findViewById(R.id.btn_drop)
        btnDrop.setOnTouchListener { v, me ->
            if (me.action == MotionEvent.ACTION_DOWN) {
                looper.onHardDrop()
                true
            }
            false
        }

        tvLevel = vRoot.findViewById(R.id.tv_Level_value)
        tvScore = vRoot.findViewById(R.id.tv_score_value)
    }
}