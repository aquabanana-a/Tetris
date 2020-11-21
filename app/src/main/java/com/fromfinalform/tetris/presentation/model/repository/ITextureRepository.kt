/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.repository

interface ITextureRepository {
    operator fun get(assetId: Int): Int
    operator fun get(path: String): Int
}