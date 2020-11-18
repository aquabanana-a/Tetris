/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.model.figure

import com.fromfinalform.tetris.data.model.cell.EmptyCell
import com.fromfinalform.tetris.domain.model.cell.ICell
import com.fromfinalform.tetris.domain.model.common.ICloneable

interface IFigure : ICloneable<IFigure> {
    val typeId: FigureTypeId
    val map: IFigureMap
    var x: Float // in cells
    var y: Float
}

val IFigure.width get() = this.map.width
val IFigure.height get() = this.map.height

fun IFigure.getCellsLocations(refX: Float = this.x, refY: Float = this.y, normalized: Boolean = false): List<ICell> {
    val ret = arrayListOf<ICell>()
    this.map.cells.forEachIndexed { y, r ->
        r.forEachIndexed { x, c ->
            if (c !is EmptyCell) {
                val rc = c.clone()
                rc.x = refX + x + if(normalized) this.map.xOffset else 0
                rc.y = refY + y + if(normalized) this.map.yOffset else 0
                ret.add(rc)
            }
        }
    }

    return ret
}