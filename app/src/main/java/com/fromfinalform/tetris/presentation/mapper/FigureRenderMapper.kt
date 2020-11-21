/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.mapper

import android.content.Context
import android.graphics.RectF
import com.fromfinalform.tetris.data.model.cell.EmptyCell
import com.fromfinalform.tetris.data.model.cell.TexturedCell
import com.fromfinalform.tetris.presentation.model.repository.TextureRepository
import com.fromfinalform.tetris.domain.model.figure.IFigure
import com.fromfinalform.tetris.domain.model.figure.height
import com.fromfinalform.tetris.domain.model.figure.width
import com.fromfinalform.tetris.domain.model.game.IGameConfig
import com.fromfinalform.tetris.presentation.model.drawer.ShaderDrawerTypeId
import com.fromfinalform.tetris.presentation.model.repository.ITextureRepository
import com.fromfinalform.tetris.presentation.model.renderer.SceneParams
import com.fromfinalform.tetris.presentation.model.renderer.unit.RenderUnit
import javax.inject.Inject

class FigureRenderMapper @Inject constructor(val context: Context) : IFigureRenderMapper {

    private var texturesRepo: ITextureRepository = TextureRepository(context) // todo: move to DI
    private var idGenerator = 0L

    override fun map(sceneParams: SceneParams, figure: IFigure, game: IGameConfig): RenderUnit {
        val cw = game.cellSizePx * sceneParams.sx
        val ch = game.cellSizePx * sceneParams.sy

        val hg = game.cellTextureContentGapHPx / game.cellSizePx
        val vg = game.cellTextureContentGapVPx / game.cellSizePx

        var ret = RenderUnit()
            .withId(++idGenerator)
            .withSize(figure.width * sceneParams.sx, figure.height * sceneParams.sy)
            as RenderUnit

        figure.map.cells.forEachIndexed { i, r ->
            r.forEachIndexed { p, c ->
                if (c !is EmptyCell) {
                    var retc = RenderUnit()
                        .withId(++idGenerator)
                        .withLocation(-1f + p * cw, 1f - i * ch)
                        .withSize(cw, ch)

                    c.id = idGenerator
                    if (c is TexturedCell)
                        retc.withShader(ShaderDrawerTypeId.FLAT)
                            .withTexture(texturesRepo[c.assetId])
                            .withSrcRect(RectF(hg, vg, 1.0f - hg, 1.0f - vg))

                    ret.addChild(retc)
                }
            }
        }

        return ret
    }

}