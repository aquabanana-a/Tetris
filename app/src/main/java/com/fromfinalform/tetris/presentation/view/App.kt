/*
 * Created by S.Dobranos on 05.12.20 22:30
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.view

import android.app.Application
import com.fromfinalform.tetris.presentation.presenter.PresenterScope

class App : Application() {

    companion object {
        val presenterScope = PresenterScope()
    }

    override fun onCreate() {
        super.onCreate()


    }
}