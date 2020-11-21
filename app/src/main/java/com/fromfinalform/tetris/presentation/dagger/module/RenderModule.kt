/*
 * Created by S.Dobranos on 19.11.20 16:38
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.dagger.module

import android.content.Context
import com.fromfinalform.tetris.presentation.mapper.FigureRenderMapper
import com.fromfinalform.tetris.presentation.mapper.IFigureRenderMapper
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Named
import javax.inject.Singleton

@Module
interface RenderModule {

    @Binds
    fun getRenderMapper(renderMapper: FigureRenderMapper): IFigureRenderMapper
}