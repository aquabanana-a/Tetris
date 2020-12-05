/*
 * Created by S.Dobranos on 05.12.20 17:22
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.presenter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.fromfinalform.tetris.domain.model.game.IGameResults
import com.fromfinalform.tetris.presentation.view.App

class GameOverPresenter(val view: IGameOverView) : LifecycleObserver {

    companion object {
        const val RESULTS_DATA = "results_data"
    }

    interface IGameOverView {
        fun setResultLevel(level: Int)
        fun setResultPoints(points: Long)

        fun getArguments(): Bundle?
    }

    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) {
        var gr: IGameResults? = view.getArguments()?.getParcelable(RESULTS_DATA)
        if (gr != null) {
            view.setResultLevel(gr.level)
            view.setResultPoints(gr.points)
        }
    }

    fun playAgain() {
        App.presenterScope.mainActivityPresenter?.startGame()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private fun onResume(owner: LifecycleOwner) {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private fun onPause(owner: LifecycleOwner) {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private fun onCreate(owner: LifecycleOwner) {

    }
}