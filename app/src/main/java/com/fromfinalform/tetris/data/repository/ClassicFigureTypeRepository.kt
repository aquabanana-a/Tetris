/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.data.repository

import com.fromfinalform.tetris.R
import com.fromfinalform.tetris.data.model.cell.EmptyCell
import com.fromfinalform.tetris.data.model.cell.TexturedCell
import com.fromfinalform.tetris.data.model.figure.FigureTypeBuilder
import com.fromfinalform.tetris.domain.model.cell.ICell
import com.fromfinalform.tetris.domain.model.figure.FigureOrientationId
import com.fromfinalform.tetris.domain.model.figure.FigureType
import com.fromfinalform.tetris.domain.model.figure.FigureTypeId
import com.fromfinalform.tetris.domain.model.figure.IFigure
import com.fromfinalform.tetris.domain.repository.IFigureTypeRepository

class ClassicFigureTypeRepository: IFigureTypeRepository {

    // https://tetris.wiki/Tetromino#:~:text=The%20seven%20one%2Dsided%20tetrominoes,previously%20called%20tetraminoes%20around%201999.

    private val figuresByTypeId = hashMapOf<FigureTypeId, FigureType>()
    override fun get(typeId: FigureTypeId): FigureType {
        return figuresByTypeId[typeId] ?: throw IllegalArgumentException()
    }

    override fun initialize() {
        figuresByTypeId[FigureTypeId.NONE] = FigureTypeBuilder(FigureTypeId.NONE).build()

        var tc: ICell
        val ec = EmptyCell()

        tc = TexturedCell(R.drawable.y2)
        figuresByTypeId[FigureTypeId.O] = FigureTypeBuilder(FigureTypeId.O)
            .withMap(FigureOrientationId.UP, -1, 0, arrayListOf(tc.clone(), tc.clone()), arrayListOf(tc.clone(), tc.clone()))
            .withMap(FigureOrientationId.RIGHT, -1, 0,  arrayListOf(tc.clone(), tc.clone()), arrayListOf(tc.clone(), tc.clone()))
            .withMap(FigureOrientationId.DOWN, -1, 0, arrayListOf(tc.clone(), tc.clone()), arrayListOf(tc.clone(), tc.clone()))
            .withMap(FigureOrientationId.LEFT, -1, 0, arrayListOf(tc.clone(), tc.clone()), arrayListOf(tc.clone(), tc.clone()))
            .build()

        tc = TexturedCell(R.drawable.lb2)
        figuresByTypeId[FigureTypeId.I] = FigureTypeBuilder(FigureTypeId.I)
            .withMap(FigureOrientationId.UP, 1, 1, arrayListOf(tc.clone(), tc.clone(), tc.clone(), tc.clone()))
            .withMap(FigureOrientationId.RIGHT, 3, 0, arrayListOf(tc.clone()), arrayListOf(tc.clone()), arrayListOf(tc.clone()), arrayListOf(tc.clone()))
            .withMap(FigureOrientationId.DOWN, 1, 2, arrayListOf(tc.clone(), tc.clone(), tc.clone(), tc.clone()))
            .withMap(FigureOrientationId.LEFT, 2, 0, arrayListOf(tc.clone()), arrayListOf(tc.clone()), arrayListOf(tc.clone()), arrayListOf(tc.clone()))
            .build()

        tc = TexturedCell(R.drawable.p2)
        figuresByTypeId[FigureTypeId.T] = FigureTypeBuilder(FigureTypeId.T)
            .withMap(FigureOrientationId.UP, arrayListOf(ec.clone(), tc.clone()), arrayListOf(tc.clone(), tc.clone(), tc.clone()))
            .withMap(FigureOrientationId.RIGHT, 1, 0, arrayListOf(tc.clone()), arrayListOf(tc.clone(), tc.clone()), arrayListOf(tc.clone()))
            .withMap(FigureOrientationId.DOWN, 0, 1, arrayListOf(tc.clone(), tc.clone(), tc.clone()), arrayListOf(ec.clone(), tc.clone()))
            .withMap(FigureOrientationId.LEFT, arrayListOf(ec.clone(), tc.clone()), arrayListOf(tc.clone(), tc.clone()), arrayListOf(ec.clone(), tc.clone()))
            .build()

        tc = TexturedCell(R.drawable.g2)
        figuresByTypeId[FigureTypeId.S] = FigureTypeBuilder(FigureTypeId.S)
            .withMap(FigureOrientationId.UP, arrayListOf(ec.clone(), tc.clone(), tc.clone()), arrayListOf(tc.clone(), tc.clone(), ec.clone()))
            .withMap(FigureOrientationId.RIGHT, 1, 0, arrayListOf(tc.clone()), arrayListOf(tc.clone(), tc.clone()), arrayListOf(ec.clone(), tc.clone()))
            .withMap(FigureOrientationId.DOWN, 0, 1, arrayListOf(ec.clone(), tc.clone(), tc.clone()), arrayListOf(tc.clone(), tc.clone(), ec.clone()))
            .withMap(FigureOrientationId.LEFT, arrayListOf(tc.clone()), arrayListOf(tc.clone(), tc.clone()), arrayListOf(ec.clone(), tc.clone()))
            .build()

        tc = TexturedCell(R.drawable.r2)
        figuresByTypeId[FigureTypeId.Z] = FigureTypeBuilder(FigureTypeId.Z)
            .withMap(FigureOrientationId.UP, arrayListOf(tc.clone(), tc.clone()), arrayListOf(ec.clone(), tc.clone(), tc.clone()))
            .withMap(FigureOrientationId.RIGHT, 1, 0, arrayListOf(ec.clone(), tc.clone()), arrayListOf(tc.clone(), tc.clone()), arrayListOf(tc.clone(), ec.clone()))
            .withMap(FigureOrientationId.DOWN, 0, 1, arrayListOf(tc.clone(), tc.clone()), arrayListOf(ec.clone(), tc.clone(), tc.clone()))
            .withMap(FigureOrientationId.LEFT, arrayListOf(ec.clone(), tc.clone()), arrayListOf(tc.clone(), tc.clone()), arrayListOf(tc.clone(), ec.clone()))
            .build()

        tc = TexturedCell(R.drawable.b2)
        figuresByTypeId[FigureTypeId.J] = FigureTypeBuilder(FigureTypeId.J)
            .withMap(FigureOrientationId.UP, arrayListOf(tc.clone()), arrayListOf(tc.clone(), tc.clone(), tc.clone()))
            .withMap(FigureOrientationId.RIGHT, 1, 0, arrayListOf(tc.clone(), tc.clone()), arrayListOf(tc.clone()), arrayListOf(tc.clone()))
            .withMap(FigureOrientationId.DOWN, 0, 1, arrayListOf(tc.clone(), tc.clone(), tc.clone()), arrayListOf(ec.clone(), ec.clone(), tc.clone()))
            .withMap(FigureOrientationId.LEFT, arrayListOf(ec.clone(), tc.clone()), arrayListOf(ec.clone(), tc.clone()), arrayListOf(tc.clone(), tc.clone()))
            .build()

        tc = TexturedCell(R.drawable.o2)
        figuresByTypeId[FigureTypeId.L] = FigureTypeBuilder(FigureTypeId.L)
            .withMap(FigureOrientationId.UP, arrayListOf(ec.clone(), ec.clone(), tc.clone()), arrayListOf(tc.clone(), tc.clone(), tc.clone()))
            .withMap(FigureOrientationId.RIGHT, 1, 0, arrayListOf(tc.clone()), arrayListOf(tc.clone()), arrayListOf(tc.clone(), tc.clone()))
            .withMap(FigureOrientationId.DOWN, 0, 1, arrayListOf(tc.clone(), tc.clone(), tc.clone()), arrayListOf(tc.clone()))
            .withMap(FigureOrientationId.LEFT, arrayListOf(tc.clone(), tc.clone()), arrayListOf(ec.clone(), tc.clone()), arrayListOf(ec.clone(), tc.clone()))
            .build()
    }
}