/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.domain.repository

import com.fromfinalform.tetris.domain.model.renderer.IShaderDrawer
import com.fromfinalform.tetris.domain.model.renderer.ShaderDrawerTypeId

interface IShaderDrawerRepository {
    fun initialize()
    operator fun get(typeId: ShaderDrawerTypeId): IShaderDrawer?
}