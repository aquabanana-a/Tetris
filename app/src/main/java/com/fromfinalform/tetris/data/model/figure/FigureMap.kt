/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.data.model.figure

import com.fromfinalform.tetris.domain.model.cell.ICell
import com.fromfinalform.tetris.domain.model.figure.FigureOrientationId
import com.fromfinalform.tetris.domain.model.figure.IFigureMap

data class FigureMap(override val orientationId: FigureOrientationId) : IFigureMap {

    override var xOffset = 0
    override var yOffset = 0

    override val cells: ArrayList<ArrayList<ICell>> = arrayListOf()

    fun addRow(cells: ArrayList<ICell>) {
        this.cells.add(cells)
    }

    override fun clone(): FigureMap {
        val ret = FigureMap(orientationId)
        ret.xOffset = xOffset
        ret.yOffset = yOffset
        for (r in cells)
            ret.addRow(ArrayList(r.map { c -> c.clone() }))
        return ret
    }
}