/*
 * Created by S.Dobranos on 19.11.20 14:54
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.data.model.game.classic

import com.fromfinalform.tetris.domain.model.game.IGameLevel
import com.fromfinalform.tetris.domain.model.game.IGameResults
import kotlinx.android.parcel.Parcelize

@Parcelize
class ClassicGameResults : IGameResults {

    override var level: Int = 0; private set
    override var speed: Float = 0f; private set
    override var points: Long = 0L; private set
    override var completedRowsCount: Int = 0; private set

    override fun updateBy(level: IGameLevel): IGameResults {
        this.level = level.level
        this.speed = level.speed
        return this
    }

    override fun addPoints(value: Long) {
        this.points += value
    }

    override fun addCompletedRows(value: Int) {
        this.completedRowsCount += value
    }

    override fun clone(): ClassicGameResults {
        val ret = ClassicGameResults()
        ret.level = level
        ret.speed = speed
        ret.points = points
        ret.completedRowsCount = completedRowsCount
        return ret
    }
}