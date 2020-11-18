/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.model.game

import com.fromfinalform.tetris.domain.model.cell.ICell
import com.fromfinalform.tetris.domain.model.figure.IFigure
import com.fromfinalform.tetris.domain.model.game.level.IGameLevel

interface IGameLooper {
    val currentLevel: IGameLevel?
    val currentFigure: IFigure?
    val isStarted: Boolean

    fun withOnCurrentFigureChanged(handler: (f: IFigure?, replace: Boolean)->Unit): IGameLooper
    fun withOnCurrentFigureLocationChanged(handler: (f: IFigure, x: Float, y: Float, dx: Float, dy: Float)->Unit): IGameLooper
    fun withOnCurrentFigureHolded(handler: (f: IFigure, x: Float, y: Float, dx: Float, dy: Float) -> Unit): IGameLooper
    fun withCompleteRowsListener(handler: (List<Pair<Int, List<ICell>>>)->Unit): IGameLooper
    fun withRelocateCellsListener(handler: (List<ICell>)->Unit): IGameLooper

    fun withOnResultsChanged(handler: (results: IGameResults) -> Unit): IGameLooper

    fun withOnStartListener(handler: ()->Unit): IGameLooper
    fun withOnStopListener(handler: ()->Unit): IGameLooper

    fun prepareNextLevel(): IGameLevel
    fun prepareNextFigure(): IFigure

    fun onLeft()
    fun onRight()
    fun onHardDrop()
    fun onRotate()

    fun start()
    fun stop()
    fun onFrame(frame: Long, timeMs: Long, deltaTimeMs: Long)
}