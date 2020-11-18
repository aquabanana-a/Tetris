/*
 * Created by S.Dobranos on 18.11.20 1:06
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.model.game.level

interface IGameLevelRepository {
    val maxLevel: Int

    fun initialize()
    operator fun get(level: Int): IGameLevel?
}