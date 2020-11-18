/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.model.figure

import com.fromfinalform.tetris.domain.model.cell.ICell

class FigureType(val id: FigureTypeId) {
    val maps = hashMapOf<FigureOrientationId, IFigureMap>()
}