/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.data.repository

import com.fromfinalform.tetris.domain.model.renderer.IShaderDrawer
import com.fromfinalform.tetris.domain.model.renderer.ShaderDrawerTypeId
import com.fromfinalform.tetris.domain.repository.IShaderDrawerRepository
import com.fromfinalform.tetris.presentation.opengl.shaderDrawer.GradientBackShaderDrawer
import com.fromfinalform.tetris.presentation.opengl.shaderDrawer.FlatShaderDrawer
import com.fromfinalform.tetris.presentation.opengl.shaderDrawer.SolidShaderDrawer

class StaticShaderDrawerRepository : IShaderDrawerRepository {

    private val programsByTypeId = hashMapOf<ShaderDrawerTypeId, IShaderDrawer>()
    override fun get(typeId: ShaderDrawerTypeId): IShaderDrawer? {
        return programsByTypeId[typeId]
    }

    override fun initialize() {
        programsByTypeId[ShaderDrawerTypeId.SOLID] = SolidShaderDrawer()
        programsByTypeId[ShaderDrawerTypeId.FLAT] = FlatShaderDrawer()
        programsByTypeId[ShaderDrawerTypeId.GRADIENT_BACK] = GradientBackShaderDrawer()
    }
}