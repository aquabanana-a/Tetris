/*
 * Created by S.Dobranos on 19.11.20 0:01
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.renderer

interface ISize {
    val width: Float
    val height: Float
}

class Size(override var width: Float = 0f, override var height: Float = 0f) : ISize

enum class SceneSizeType(val id: Int): ISize {

    VERTICAL_FULL_HD(1) {
        override val width get() = 1080f
        override val height get() = 1920f
    },

    HORIZONTAL_FULL_HD(2) {
        override val width get() = 1920f
        override val height get() = 1080f
    },

    VERTICAL_1080x4360(3)
    {
        override val width get() = 1080f
        override val height get() = 4360f
    };

    override val width: Float = 0f
    override val height: Float = 0f
}