/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.model.game

import com.fromfinalform.tetris.domain.model.cell.ICell
import com.fromfinalform.tetris.domain.model.figure.IFigure

interface IGameField {

    val isEmpty: Boolean

    fun canBePlaced(figure: IFigure, x: Float = figure.x, y: Float = figure.y): Boolean
    fun placeToLowest(figure: IFigure): Boolean

    fun hold(figure: IFigure): Boolean
    fun clear()

    fun getFullRows(): List<Pair<Int/*row index*/, List<ICell>>>
    fun getNonEmptyCells(): List<ICell>
    fun completeRows(rowIndexes: List<Int>)
}