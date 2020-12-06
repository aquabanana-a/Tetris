/*
 * Created by S.Dobranos on 05.12.20 17:13
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.fromfinalform.tetris.R
import com.fromfinalform.tetris.presentation.presenter.GameOverPresenter
import com.fromfinalform.tetris.presentation.presenter.SwitchScreenMgr
import com.fromfinalform.tetris.presentation.view.common.format

class GameOverFragment : Fragment(), GameOverPresenter.IGameOverView {

    private lateinit var vRoot: View
    private lateinit var btnClose: View
    private lateinit var btnPlayAgain: View
    private lateinit var tvLevel: TextView
    private lateinit var tvScore: TextView

    private var presenter: GameOverPresenter

    init {
        presenter = GameOverPresenter(this)
        App.presenterScope.register(presenter)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        vRoot = inflater.inflate(R.layout.fragment_game_over, container, false)
        lifecycle.addObserver(presenter)

        tvLevel = vRoot.findViewById(R.id.tv_level)
        tvScore = vRoot.findViewById(R.id.tv_score)

        btnClose = vRoot.findViewById(R.id.vg_close)
        btnClose.setOnClickListener {
            SwitchScreenMgr.onGameOverEnds(activity as FragmentActivity)
        }

        btnPlayAgain = vRoot.findViewById(R.id.vg_play)
        btnPlayAgain.setOnClickListener {
            SwitchScreenMgr.onGameOverEnds(activity as FragmentActivity)
            presenter.playAgain()
        }

        presenter.onCreateView(inflater, container, savedInstanceState)

        return vRoot
    }

    override fun setResultLevel(level: Int) {
        tvLevel.text = format(R.string.game_over_level, level)
    }

    override fun setResultPoints(points: Long) {
        tvScore.text = format(R.string.game_over_score, points)
    }
}