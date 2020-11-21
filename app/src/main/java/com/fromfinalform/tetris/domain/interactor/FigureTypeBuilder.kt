/*
 * Created by S.Dobranos on 18.11.20 23:54
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.interactor

import com.fromfinalform.tetris.domain.model.cell.ICell
import com.fromfinalform.tetris.domain.model.figure.FigureOrientationId
import com.fromfinalform.tetris.domain.model.figure.FigureType
import com.fromfinalform.tetris.domain.model.figure.FigureTypeId
import com.fromfinalform.tetris.domain.model.figure.IFigureMap

class FigureTypeBuilder(val figureTypeId: FigureTypeId) {

    private val maps = hashMapOf<FigureOrientationId, IFigureMap>()

    fun withMap(orientationId: FigureOrientationId, vararg rows: ArrayList<ICell>) = withMap(orientationId, 0, 0, *rows)
    fun withMap(orientationId: FigureOrientationId, xOffset: Int, yOffset: Int, vararg rows: ArrayList<ICell>): FigureTypeBuilder {
        var mb = FigureMapBuilder()
            .withOrientation(orientationId)
            .withXOffset(xOffset)
            .withYOffset(yOffset)

        for(r in rows)
            mb.withRow(r)

        this.maps[orientationId] = mb.build()
        return this
    }

    fun build(): FigureType {
        val ret = FigureType(figureTypeId)
        for(k in maps.keys)
            ret.maps[k] = maps[k]!!.clone()
        return ret
    }

}