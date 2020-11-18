/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.model.cell

import com.fromfinalform.tetris.domain.model.common.ICloneable

interface ICell : ICloneable<ICell> {
    var id: Long
    var x: Float
    var y: Float
}