/*
 * Created by S.Dobranos on 18.11.20 23:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.drawer

import android.graphics.RectF
import com.fromfinalform.tetris.presentation.model.renderer.unit.IRenderUnit
import com.fromfinalform.tetris.presentation.model.renderer.SceneParams

interface IShaderDrawer {
    val typeId: ShaderDrawerTypeId
    val program: Int

    fun setUniforms(vararg args: Any)
    fun cleanUniforms()

    fun draw(ru: IRenderUnit, params: SceneParams, dst: RectF?, src: RectF?, angle: Float)
}