/*
 * Created by S.Dobranos on 18.11.20 23:22
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.model.figure

import com.fromfinalform.tetris.domain.model.cell.ICell
import com.fromfinalform.tetris.common.ICloneable

interface IFigureMap : ICloneable<IFigureMap> {
    val orientationId: FigureOrientationId

    var xOffset: Int // in cells
    var yOffset: Int

    val cells: List<List<ICell>>
}

val IFigureMap.width get() = this.cells.maxOf { r -> r.size }
val IFigureMap.height get() = this.cells.size