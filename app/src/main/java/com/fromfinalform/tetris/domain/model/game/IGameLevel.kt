/*
 * Created by S.Dobranos on 18.11.20 23:21
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.model.game

import com.fromfinalform.tetris.common.ICloneable

interface IGameLevel : ICloneable<IGameLevel> {
    companion object {
        const val LVL_N = Int.MAX_VALUE
    }

    var level: Int
    val speed: Float

    val ptsSingle: Long
    val ptsDouble: Long
    val ptsTriple: Long
    val ptsTetris: Long

    val ptsPerfectClear: Long
    val ptsPerfectClearMultiplier: Float

    val ptsSoftDrop: Long
    val ptsHardDrop: Long

    fun getCompletePoints(rowsCount: Int, perfectClear: Boolean, lvl: Int = level): Long
    fun getDropPoints(softCount: Int, hardCount: Int, perfectClear: Boolean, lvl: Int = level): Long
}