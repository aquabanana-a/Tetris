/*
 * Created by S.Dobranos on 18.11.20 2:40
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.model.game

import com.fromfinalform.tetris.common.ICloneable

interface IGameResults : ICloneable<IGameResults> {
    val level: Int
    val speed: Float
    val points: Long
    val completedRowsCount: Int

    fun updateBy(level: IGameLevel): IGameResults

    fun addPoints(value: Long)
    fun addCompletedRows(value: Int)
}