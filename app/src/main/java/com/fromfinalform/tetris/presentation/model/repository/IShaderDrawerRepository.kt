/*
 * Created by S.Dobranos on 17.11.20 15:25
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.model.repository

import com.fromfinalform.tetris.presentation.model.drawer.IShaderDrawer
import com.fromfinalform.tetris.presentation.model.drawer.ShaderDrawerTypeId

interface IShaderDrawerRepository {
    fun initialize()
    operator fun get(typeId: ShaderDrawerTypeId): IShaderDrawer?
}