/*
 * Created by S.Dobranos on 19.11.20 18:14
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.dagger.module

import com.fromfinalform.tetris.data.repository.ClassicFigureTypeRepository
import com.fromfinalform.tetris.domain.repository.IFigureTypeRepository
import dagger.Binds
import dagger.Module

@Module
interface ClassicFigureModule {
    @Binds fun getFigureTypeRepository(typeRepository: ClassicFigureTypeRepository): IFigureTypeRepository
}