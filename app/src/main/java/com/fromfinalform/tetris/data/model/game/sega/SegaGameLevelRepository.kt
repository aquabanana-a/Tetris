/*
 * Created by S.Dobranos on 19.11.20 14:54
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.data.model.game.sega

import com.fromfinalform.tetris.domain.model.game.IGameLevel
import com.fromfinalform.tetris.domain.model.game.IGameLevel.Companion.LVL_N
import com.fromfinalform.tetris.domain.repository.IGameLevelRepository
import javax.inject.Inject

class SegaGameLevelRepository : IGameLevelRepository {

    @Inject constructor() {

    }

    override val maxLevel get() = LVL_N

    private val gameLevels = hashMapOf<Int, IGameLevel>()
    override fun get(level: Int): IGameLevel? {
        return gameLevels[level] ?: gameLevels[LVL_N]
    }

    override fun initialize() {
        gameLevels[1]       = SegaGameLevel(1, 1f/800, 100, 400, 900, 2000, 1, 10f)
        gameLevels[2]       = SegaGameLevel(2, 1f/700, 200, 800, 1800, 4000, 2, 10f)
        gameLevels[3]       = SegaGameLevel(3, 1f/640, 200, 800, 1800, 4000, 2, 10f)
        gameLevels[4]       = SegaGameLevel(4, 1f/560, 300, 1200, 2700, 6000, 3, 10f)
        gameLevels[5]       = SegaGameLevel(5, 1f/450, 300, 1200, 2700, 6000, 3, 10f)
        gameLevels[6]       = SegaGameLevel(6, 1f/380, 400, 1600, 3600, 8000, 4, 10f)
        gameLevels[7]       = SegaGameLevel(7, 1f/220, 400, 1600, 3600, 8000, 4, 10f)
        gameLevels[LVL_N]   = SegaGameLevel(LVL_N, 1f/150, 500, 2000, 4500, 10000, 5, 10f)
    }
}