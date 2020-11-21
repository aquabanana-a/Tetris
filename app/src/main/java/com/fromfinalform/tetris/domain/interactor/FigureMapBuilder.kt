/*
 * Created by S.Dobranos on 18.11.20 23:53
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.interactor

import com.fromfinalform.tetris.data.model.figure.FigureMap
import com.fromfinalform.tetris.domain.model.cell.ICell
import com.fromfinalform.tetris.domain.model.figure.FigureOrientationId
import com.fromfinalform.tetris.domain.model.figure.IFigureMap

class FigureMapBuilder() {

    private var orientationId = FigureOrientationId.UP
    fun withOrientation(orientationId: FigureOrientationId): FigureMapBuilder {
        this.orientationId = orientationId
        return this
    }

    private var xOffset = 0
    fun withXOffset(value: Int): FigureMapBuilder {
        this.xOffset = value
        return this
    }

    private var yOffset = 0
    fun withYOffset(value: Int): FigureMapBuilder {
        this.yOffset = value
        return this
    }

    private var figureMapRows = arrayListOf<ArrayList<ICell>>()
    fun withRow(cells: ArrayList<ICell>): FigureMapBuilder {
        this.figureMapRows.add(ArrayList(cells.map { c -> c.clone() }))
        return this
    }

    fun build(): IFigureMap {
        val ret = FigureMap(orientationId)
        ret.xOffset = xOffset
        ret.yOffset = yOffset
        for (r in figureMapRows)
            ret.addRow(ArrayList(r.map { c -> c.clone() }))

        return ret
    }
}