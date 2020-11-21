/*
 * Created by S.Dobranos on 19.11.20 18:17
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.dagger.module

import com.fromfinalform.tetris.data.model.game.sega.SegaGameLevelRepository
import com.fromfinalform.tetris.domain.repository.IGameLevelRepository
import dagger.Binds
import dagger.Module

@Module
interface SegaLevelModule {
    @Binds fun getLevelRepository(levelRepository: SegaGameLevelRepository): IGameLevelRepository
}