/*
 * Created by S.Dobranos on 19.11.20 14:57
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.repository

import com.fromfinalform.tetris.domain.model.figure.FigureType
import com.fromfinalform.tetris.domain.model.figure.FigureTypeId
import com.fromfinalform.tetris.domain.model.figure.IFigure

interface IFigureTypeRepository {
    fun initialize()
    operator fun get(typeId: FigureTypeId): FigureType
}