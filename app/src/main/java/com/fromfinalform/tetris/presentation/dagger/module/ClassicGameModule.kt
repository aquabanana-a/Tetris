/*
 * Created by S.Dobranos on 19.11.20 16:28
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.dagger.module

import com.fromfinalform.tetris.data.model.game.classic.ClassicGameConfig
import com.fromfinalform.tetris.domain.interactor.IGameLooper
import com.fromfinalform.tetris.domain.model.game.IGameConfig
import com.fromfinalform.tetris.domain.model.game.classic.ClassicGameLooper
import dagger.Binds
import dagger.Module

@Module
interface ClassicGameModule {
    @Binds fun bindConfig(config: ClassicGameConfig): IGameConfig
    @Binds fun bindLooper(looper: ClassicGameLooper): IGameLooper
}