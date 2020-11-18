/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.data.model.game

import com.fromfinalform.tetris.data.model.cell.EmptyCell
import com.fromfinalform.tetris.domain.model.cell.ICell
import com.fromfinalform.tetris.domain.model.figure.IFigure
import com.fromfinalform.tetris.domain.model.figure.getCellsLocations
import com.fromfinalform.tetris.domain.model.figure.height
import com.fromfinalform.tetris.domain.model.figure.width
import com.fromfinalform.tetris.domain.model.game.IGameConfig
import com.fromfinalform.tetris.domain.model.game.IGameField
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round

class ClassicGameField : IGameField {

    companion object {
        private const val CELL_ID_TO_REMOVE = -2L
    }

    private val config: IGameConfig = ClassicGameConfig()

    private val fieldMatrixLo = Any()
    private var fieldMatrix = MutableList(config.fieldHeight) { y -> MutableList<ICell>(config.fieldWidth) { x -> EmptyCell(x.toFloat(), y.toFloat()) } }

    override val isEmpty: Boolean get() { synchronized(fieldMatrixLo) {
        return fieldMatrix.all { r -> r.all { c -> c is EmptyCell } }
    } }

    override fun canBePlaced(figure: IFigure, x: Float, y: Float): Boolean {
        val ocl = figure.getCellsLocations(x, y)
        if (x < 0 || x + figure.width > config.fieldWidth)
            return false

        if (y + figure.height > config.fieldHeight)
            return false

        val hm = x - figure.x != 0f
        val vm = y - figure.y != 0f
        if (ocl.any { !canBePlaced(it, hm, vm) })
            return false

        return true
    }

    private fun canBePlaced(cell: ICell, hMovement: Boolean = false, vMovement: Boolean = false): Boolean { synchronized(fieldMatrixLo) {
        var x = ceil(cell.x).toInt()
        var y = ceil(cell.y).toInt()

        if (y < 0)
            return true

        if (fieldMatrix[y][x] !is EmptyCell)
            return false

        val yPrev = floor(cell.y).toInt()
        val cc = if(yPrev > 0f) fieldMatrix[yPrev][x] else EmptyCell()

        if (hMovement && cc !is EmptyCell && cell.y - yPrev < 0.6f)
            return false

        return true
    } }

    override fun placeToLowest(figure: IFigure): Boolean { synchronized(fieldMatrixLo) {
        var nextY = Int.MIN_VALUE
        for (y in 0..config.fieldHeight) {
            if (canBePlaced(figure, figure.x, y.toFloat()))
                nextY = y
            else
                break
        }

        if (nextY != Int.MIN_VALUE) {
            figure.y = nextY.toFloat()
            return true
        }
        return false
    } }

    override fun hold(figure: IFigure): Boolean { synchronized(fieldMatrixLo) {
        figure.x = round(figure.x)
        figure.y = round(figure.y)
        if (figure.x < 0 || figure.x + figure.width > config.fieldWidth || figure.y < 0 || figure.y + figure.height > config.fieldHeight)
            return false

        for (c in figure.getCellsLocations())
            fieldMatrix[c.y.toInt()][c.x.toInt()] = c.clone()
        return true
    } }

    override fun getFullRows(): List<Pair<Int, List<ICell>>> { synchronized(fieldMatrixLo) {
        val ret = mutableListOf<Pair<Int, List<ICell>>>()
        fieldMatrix.forEachIndexed { i, r ->
            if (!r.any { it is EmptyCell })
                ret.add(Pair(i, r.map { it.clone() }))
        }
        return ret
    } }

    override fun getNonEmptyCells(): List<ICell> { synchronized(fieldMatrixLo) {
        val ret = mutableListOf<ICell>()
        for (r in fieldMatrix)
            ret.addAll(r.filter { it !is EmptyCell })
        return ret
    } }

    override fun completeRows(rowIndexes: List<Int>) { synchronized(fieldMatrixLo) {
        rowIndexes.forEach { i ->
            for (c in 0 until config.fieldWidth)
                fieldMatrix[i][c] = EmptyCell(c.toFloat(), i.toFloat()).withId(CELL_ID_TO_REMOVE)
        }

        for (x in 0 until config.fieldWidth)
            stackColumn(x)
    } }

    private fun stackColumn(x: Int) { synchronized(fieldMatrixLo) {
        var found = false
        for (y in config.fieldHeight - 1 downTo 0)
            if (fieldMatrix[y][x].id == CELL_ID_TO_REMOVE) {
                found = true
                for (i in y-1 downTo 0) {
                    fieldMatrix[i + 1][x] = fieldMatrix[i][x]
                    fieldMatrix[i + 1][x].y++
                }
                fieldMatrix[0][x] = EmptyCell(x.toFloat(), 0f)
                break
            }

        if (found)
            stackColumn(x)
    } }

    override fun clear() { synchronized(fieldMatrixLo) {
        fieldMatrix = MutableList(config.fieldHeight) { y -> MutableList<ICell>(config.fieldWidth) { x -> EmptyCell(x.toFloat(), y.toFloat()) } }
    } }

}