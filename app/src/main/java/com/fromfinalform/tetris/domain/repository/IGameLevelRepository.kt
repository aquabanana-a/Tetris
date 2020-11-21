/*
 * Created by S.Dobranos on 18.11.20 23:21
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.repository

import com.fromfinalform.tetris.domain.model.game.IGameLevel

interface IGameLevelRepository {
    val maxLevel: Int

    fun initialize()
    operator fun get(level: Int): IGameLevel?
}