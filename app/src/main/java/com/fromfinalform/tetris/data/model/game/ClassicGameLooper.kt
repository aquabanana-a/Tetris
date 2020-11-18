/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.data.model.game

import com.fromfinalform.tetris.data.model.figure.ClassicFigureBuilder
import com.fromfinalform.tetris.data.model.game.level.SegaGameLevelRepository
import com.fromfinalform.tetris.domain.model.cell.ICell
import com.fromfinalform.tetris.domain.model.figure.*
import com.fromfinalform.tetris.domain.model.game.IGameConfig
import com.fromfinalform.tetris.domain.model.game.IGameField
import com.fromfinalform.tetris.domain.model.game.IGameLooper
import com.fromfinalform.tetris.domain.model.game.IGameResults
import com.fromfinalform.tetris.domain.model.game.level.IGameLevel
import com.fromfinalform.tetris.domain.model.game.level.IGameLevelRepository
import kotlin.math.abs
import kotlin.math.floor

class ClassicGameLooper() : IGameLooper {

    private val config: IGameConfig = ClassicGameConfig()
    private val field: IGameField = ClassicGameField()
    private val levels: IGameLevelRepository = SegaGameLevelRepository().apply { initialize() }

    private var results = ClassicGameResults()

    private var figureCreatedAtY = 0f
    private var figureSoftDroppedAtY = Byte.MIN_VALUE.toFloat()
    private var figureHardDroppedAtY = Byte.MIN_VALUE.toFloat()

    private var holdDesiredTimeMs: Long = -1
    private var hardDropSpeed: Float = -1f

    private val currentLevelLo = Any()
    override var currentLevel: IGameLevel? = null; private set

    private val currentFigureLo = Any()
    override var currentFigure: IFigure? = null; private set

    private val startedLo = Any()
    override var isStarted = false; private set

    private var currentFigureChangedHandler: ((f: IFigure?, replace: Boolean) -> Unit)? = null
    override fun withOnCurrentFigureChanged(handler: (f: IFigure?, replace: Boolean) -> Unit): IGameLooper {
        this.currentFigureChangedHandler = handler
        return this
    }

    private var currentFigureHoldedHandler: ((f: IFigure, x: Float, y: Float, dx: Float, dy: Float) -> Unit)? = null
    override fun withOnCurrentFigureHolded(handler: (f: IFigure, x: Float, y: Float, dx: Float, dy: Float) -> Unit): IGameLooper {
        this.currentFigureHoldedHandler = handler
        return this
    }

    private var currentFigureLocationChangedHandler: ((f: IFigure, x: Float, y: Float, dx: Float, dy: Float) -> Unit)? = null
    override fun withOnCurrentFigureLocationChanged(handler: (f: IFigure, x: Float, y: Float, dx: Float, dy: Float) -> Unit): IGameLooper {
        this.currentFigureLocationChangedHandler = handler
        return this
    }

    private var completeRowsHandler: ((List<Pair<Int, List<ICell>>>)->Unit)? = null
    override fun withCompleteRowsListener(handler: (List<Pair<Int, List<ICell>>>) -> Unit): IGameLooper {
        this.completeRowsHandler = handler
        return this
    }

    private var relocateCellsHandler: ((List<ICell>) -> Unit)? = null
    override fun withRelocateCellsListener(handler: (List<ICell>) -> Unit): IGameLooper {
        this.relocateCellsHandler = handler
        return this
    }

    private var resultsChangedHandler: ((results: IGameResults) -> Unit)? = null
    override fun withOnResultsChanged(handler: (results: IGameResults) -> Unit): IGameLooper {
        this.resultsChangedHandler = handler
        return this
    }

    private var onStartHandler: (() -> Unit)? = null
    override fun withOnStartListener(handler: () -> Unit): IGameLooper {
        this.onStartHandler = handler
        return this
    }

    private var onStopHandler: (() -> Unit)? = null
    override fun withOnStopListener(handler: () -> Unit): IGameLooper {
        this.onStopHandler = handler
        return this
    }

    override fun onLeft() { translateImpl(-1) }
    override fun onRight() { translateImpl(+1) }

    private fun translateImpl(delta: Int) { synchronized(currentFigureLo) {
        val x = currentFigure!!.x + delta
        val y = currentFigure!!.y
        if (currentFigure == null || !field.canBePlaced(currentFigure!!, x, y))
            return

        currentFigure!!.x = x
        currentFigure!!.y = y
        currentFigureLocationChangedHandler?.invoke(currentFigure!!, x, y, delta.toFloat(), 0f)
    } }

    override fun onHardDrop() { synchronized(currentFigureLo) {
        this.hardDropSpeed = config.hardDropSpeed
        this.figureHardDroppedAtY = currentFigure?.y ?: 0f
    } }

    override fun onRotate() { synchronized(currentFigureLo) {
        if (currentFigure == null)
            return

        val normalizedX = currentFigure!!.x - currentFigure!!.map.xOffset
        val normalizedY = currentFigure!!.y - currentFigure!!.map.yOffset

        var newOrientationId = when (currentFigure!!.map.orientationId) {
            FigureOrientationId.UP      -> FigureOrientationId.RIGHT
            FigureOrientationId.RIGHT   -> FigureOrientationId.DOWN
            FigureOrientationId.DOWN    -> FigureOrientationId.LEFT
            FigureOrientationId.LEFT    -> FigureOrientationId.UP
        }

        var newFigure = ClassicFigureBuilder(currentFigure!!.typeId)
            .withOrientation(newOrientationId)
            .withLocation(normalizedX, normalizedY)
            .build()

        if (newFigure.x < 0) newFigure.x = 0f
        if (newFigure.x + newFigure.width >= config.fieldWidth) newFigure.x = config.fieldWidth.toFloat() - 1 - newFigure.width

        if (!field.canBePlaced(newFigure))
            return

        currentFigure = newFigure
        currentFigureChangedHandler?.invoke(currentFigure, true)
    } }

    private fun gotoNextFigure() { synchronized(currentFigureLo) {
        currentFigure = prepareNextFigure()
        currentFigureChangedHandler?.invoke(currentFigure, false)
    } }

    private fun gotoNextLevel() { synchronized(currentLevelLo) {
        currentLevel = prepareNextLevel()
        results.updateBy(currentLevel!!)
        resultsChangedHandler?.invoke(results.clone())
    } }

    override fun prepareNextLevel(): IGameLevel {
        var nextLevel = (currentLevel?.level ?: 0) + 1
        if (nextLevel > levels.maxLevel)
            nextLevel = levels.maxLevel

        val lvl = levels[nextLevel]!!
        lvl.level = nextLevel
        return lvl
    }

    override fun prepareNextFigure(): IFigure {
        hardDropSpeed = -1f
        val f = ClassicFigureBuilder(FigureTypeId.getRandom(currentFigure?.typeId)).build()
        f.x = floor(config.fieldWidth / f.width.toFloat()) + f.map.xOffset
        f.y = -(f.height.toFloat() + f.map.yOffset)

        figureCreatedAtY = f.y
        figureSoftDroppedAtY = Byte.MIN_VALUE.toFloat()
        figureHardDroppedAtY = Byte.MIN_VALUE.toFloat()

        return f
    }

    override fun start() { synchronized(startedLo) {
        if (isStarted)
            return

        isStarted = true
        field.clear()
        results = ClassicGameResults()

        onStartHandler?.invoke()

        gotoNextLevel()
        gotoNextFigure()
    } }

    override fun stop() { synchronized(startedLo) { synchronized(currentFigureLo) { synchronized(currentLevelLo) {
        if (!isStarted)
            return

        isStarted = false
        currentLevel = null
        currentFigure = null

        onStopHandler?.invoke()
    } } } }

    override fun onFrame(frame: Long, timeMs: Long, deltaTimeMs: Long) { synchronized(startedLo) { synchronized(currentFigureLo) { synchronized(currentLevelLo) {
        if (!isStarted || currentFigure == null)
            return

        val xPrev = currentFigure!!.x
        val yPrev = currentFigure!!.y

        var dy = deltaTimeMs * if(hardDropSpeed > 0) hardDropSpeed else currentLevel!!.speed

        if (holdDesiredTimeMs < 0 && field.canBePlaced(currentFigure!!, currentFigure!!.x, currentFigure!!.y + dy)) {
            currentFigure!!.y += dy
            currentFigureLocationChangedHandler?.invoke(currentFigure!!, currentFigure!!.x, currentFigure!!.y, 0f, dy)
        }
        else {
            if (field.placeToLowest(currentFigure!!)) {
                currentFigureLocationChangedHandler?.invoke(currentFigure!!, currentFigure!!.x, currentFigure!!.y, currentFigure!!.x - xPrev, currentFigure!!.y - yPrev)
            }

            if (holdDesiredTimeMs < 0) {
                holdDesiredTimeMs = timeMs + config.waitForUserBeforeHoldMs
            }
            else {
                if (timeMs >= holdDesiredTimeMs) {
                    holdDesiredTimeMs = -1

                    if (field.hold(currentFigure!!)) {
                        currentFigureHoldedHandler?.invoke(currentFigure!!, currentFigure!!.x, currentFigure!!.y, currentFigure!!.x - xPrev, currentFigure!!.y - yPrev)

                        var fr = field.getFullRows()
                        if (fr.isNotEmpty()) {
                            completeRowsHandler?.invoke(fr)
                            field.completeRows(fr.map { it.first })
                            relocateCellsHandler?.invoke(field.getNonEmptyCells())

                            results.addCompletedRows(fr.size)
                            results.addPoints(currentLevel!!.getCompletePoints(fr.size, field.isEmpty))
                            resultsChangedHandler?.invoke(results.clone())
                        }

                        val hardRows = if (figureHardDroppedAtY < figureCreatedAtY) 0f else abs(currentFigure!!.y - figureHardDroppedAtY)
                        val softRows = if (figureSoftDroppedAtY < figureCreatedAtY) 0f else abs(currentFigure!!.y - figureSoftDroppedAtY)
                        val allRows = currentFigure!!.y - figureCreatedAtY
                        results.addPoints(currentLevel!!.getDropPoints(softRows.toInt(), hardRows.toInt(), field.isEmpty))
                        resultsChangedHandler?.invoke(results.clone())

                        if (currentLevel!!.level != levels.maxLevel && results.completedRowsCount > currentLevel!!.level * config.completeRowsToNextLevel)
                            gotoNextLevel()

                        gotoNextFigure()
                    } else {
                        stop()
                    }
                }
                else {}
            }
        }

    } } } }
}