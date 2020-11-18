/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.data.model.cell

import com.fromfinalform.tetris.domain.model.cell.ICell

class TexturedCell(val assetId: Int, override var x: Float = 0f, override var y: Float = 0f) : ICell {

    override var id: Long = -1

    override fun clone(): TexturedCell {
        val ret = TexturedCell(assetId, x, y)
        ret.id = id
        return ret
    }
}