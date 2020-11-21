/*
 * Created by S.Dobranos on 19.11.20 16:34
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.dagger

import android.content.Context
import com.fromfinalform.tetris.domain.interactor.IGameLooper
import com.fromfinalform.tetris.domain.model.game.IGameConfig
import com.fromfinalform.tetris.domain.repository.IFigureTypeRepository
import com.fromfinalform.tetris.domain.repository.IGameLevelRepository
import com.fromfinalform.tetris.presentation.dagger.module.ClassicFigureModule
import com.fromfinalform.tetris.presentation.dagger.module.ClassicGameModule
import com.fromfinalform.tetris.presentation.dagger.module.RenderModule
import com.fromfinalform.tetris.presentation.dagger.module.SegaLevelModule
import com.fromfinalform.tetris.presentation.mapper.IFigureRenderMapper
import dagger.BindsInstance
import dagger.Component
import javax.inject.Named

@Component(modules = [ClassicGameModule::class, RenderModule::class, ClassicFigureModule::class, SegaLevelModule::class])
interface GameComponent {
    fun getConfig(): IGameConfig
    fun getLooper(): IGameLooper

    fun getRenderMapper(): IFigureRenderMapper

    fun getFigureTypeRepository(): IFigureTypeRepository
    fun getLevelRepository(): IGameLevelRepository

    @Component.Builder
    interface Builder {
        fun build(): GameComponent

        @BindsInstance
        @Named("Context")
        fun withContext(context: Context): Builder
    }
}