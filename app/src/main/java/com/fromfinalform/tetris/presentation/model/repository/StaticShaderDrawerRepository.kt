/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.repository

import com.fromfinalform.tetris.presentation.model.drawer.IShaderDrawer
import com.fromfinalform.tetris.presentation.model.drawer.ShaderDrawerTypeId
import com.fromfinalform.tetris.presentation.model.repository.IShaderDrawerRepository
import com.fromfinalform.tetris.presentation.model.drawer.GradientBackShaderDrawer
import com.fromfinalform.tetris.presentation.model.drawer.FlatShaderDrawer
import com.fromfinalform.tetris.presentation.model.drawer.SolidShaderDrawer

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