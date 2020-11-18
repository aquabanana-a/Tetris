/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.data.model.figure

import com.fromfinalform.tetris.domain.model.figure.FigureTypeId
import com.fromfinalform.tetris.domain.model.figure.IFigure
import com.fromfinalform.tetris.domain.model.figure.IFigureMap

data class Figure(override val typeId: FigureTypeId, override val map: IFigureMap) : IFigure {

    override var x: Float = 0f
    override var y: Float = 0f

    override fun clone(): Figure {
        val ret = Figure(typeId, map.clone())
        ret.x = x
        ret.y = y
        return ret
    }
}