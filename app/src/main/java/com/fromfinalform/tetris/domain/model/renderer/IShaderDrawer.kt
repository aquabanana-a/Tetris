/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.model.renderer

import android.graphics.RectF
import com.fromfinalform.tetris.presentation.opengl.SceneParams

interface IShaderDrawer {
    val typeId: ShaderDrawerTypeId
    val program: Int

    fun setUniforms(vararg args: Any)
    fun cleanUniforms()

    fun draw(ru: IRenderUnit, params: SceneParams, dst: RectF?, src: RectF?, angle: Float)
}