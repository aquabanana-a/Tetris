/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.data.model.figure

import com.fromfinalform.tetris.data.repository.ClassicFigureTypeRepository
import com.fromfinalform.tetris.domain.model.figure.FigureOrientationId
import com.fromfinalform.tetris.domain.model.figure.FigureTypeId
import com.fromfinalform.tetris.domain.model.figure.IFigure
import com.fromfinalform.tetris.domain.repository.IFigureTypeRepository

class ClassicFigureBuilder(override val typeId: FigureTypeId): IFigureBuilder {

    private val repo: IFigureTypeRepository = ClassicFigureTypeRepository().apply { initialize() }

    private var x = 0f
    private var y = 0f
    override fun withLocation(x: Float, y: Float): IFigureBuilder {
        this.x = x
        this.y = y
        return this
    }

    private var orientationId = FigureOrientationId.UP
    override fun withOrientation(orientationId: FigureOrientationId): IFigureBuilder {
        this.orientationId = orientationId
        return this
    }

    override fun build(): IFigure {
        val t = repo[typeId]
        val m = t.maps[orientationId]!!
        var ret = Figure(typeId, m)
        ret.x = x + m.xOffset
        ret.y = y + m.yOffset
        return ret
    }
}