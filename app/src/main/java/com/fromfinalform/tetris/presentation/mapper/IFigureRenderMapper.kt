/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.mapper

import com.fromfinalform.tetris.domain.model.figure.IFigure
import com.fromfinalform.tetris.domain.model.game.IGameConfig
import com.fromfinalform.tetris.presentation.model.renderer.SceneParams
import com.fromfinalform.tetris.presentation.model.renderer.unit.RenderUnit

interface IFigureRenderMapper {
    fun map(sceneParams: SceneParams, figure: IFigure, game: IGameConfig): RenderUnit
}