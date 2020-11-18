/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.data.model.figure

import com.fromfinalform.tetris.domain.model.figure.FigureOrientationId
import com.fromfinalform.tetris.domain.model.figure.FigureTypeId
import com.fromfinalform.tetris.domain.model.figure.IFigure

interface IFigureBuilder {
    val typeId: FigureTypeId

    fun withLocation(x: Float, y: Float): IFigureBuilder
    fun withOrientation(orientationId: FigureOrientationId): IFigureBuilder

    fun build(): IFigure
}