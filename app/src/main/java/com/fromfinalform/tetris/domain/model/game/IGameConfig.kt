/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.model.game

interface IGameConfig {
    val cellSizePx: Float // width/height in pixels
    val cellTextureContentGapHPx: Float
    val cellTextureContentGapVPx: Float

    val hardDropSpeed: Float
    val softDropSpeed: Float

    val fieldWidth: Int // in cells
    val fieldHeight: Int

    val fieldWidthPx: Float
    val fieldHeightPx: Float

    val completeRowsToNextLevel: Int

    val waitForUserBeforeHoldMs: Int
}