/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.model.figure

import kotlin.random.Random

enum class FigureTypeId(val id: Int) {
    NONE    (0),

    O       (11),
    I       (12),
    T       (13),
    S       (14),
    Z       (15),
    J       (16),
    L       (17);

    companion object {
        val values = values().filter { it != NONE }
        val rnd = Random(System.currentTimeMillis())

        fun getRandom(exclude: FigureTypeId? = null): FigureTypeId {
            val v = (rnd.nextInt(0, 1_000_000) / 1000f).toInt()
            var ret = when(v) {
                in 0..100   -> O
                in 100..200 -> I
                in 200..350 -> T
                in 350..560 -> S
                in 560..760 -> Z
                in 760..880 -> J
                in 880..1000 -> L
                else -> Z
            }
            if (ret == exclude)
                return getRandom(exclude)

            return ret
        }
    }
}