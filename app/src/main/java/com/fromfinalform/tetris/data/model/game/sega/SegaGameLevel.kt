/*
 * Created by S.Dobranos on 19.11.20 14:54
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.data.model.game.sega

import com.fromfinalform.tetris.domain.model.game.IGameLevel

class SegaGameLevel(
    override var level: Int,
    override val speed: Float,
    override val ptsSingle: Long,
    override val ptsDouble: Long,
    override val ptsTriple: Long,
    override val ptsTetris: Long,
    override val ptsSoftDrop: Long,
    override val ptsPerfectClearMultiplier: Float
) : IGameLevel {

    override val ptsHardDrop: Long = 0L
    override val ptsPerfectClear: Long = 0L

    override fun getDropPoints(softCount: Int, hardCount: Int, perfectClear: Boolean, level: Int): Long {
        return ptsSoftDrop * (softCount + hardCount)
    }

    override fun getCompletePoints(rowsCount: Int, perfectClear: Boolean, level: Int): Long {
        var pts = when(rowsCount) {
            0 -> 0
            1 -> ptsSingle
            2 -> ptsDouble
            3 -> ptsTriple
            4 -> ptsTetris
            else -> ptsTetris
        }

        return (pts * if (perfectClear) ptsPerfectClearMultiplier else 1f).toLong()
    }

    override fun clone(): IGameLevel {
        return SegaGameLevel(level, speed, ptsSingle, ptsDouble, ptsTriple, ptsTetris, ptsSoftDrop, ptsPerfectClearMultiplier)
    }
}