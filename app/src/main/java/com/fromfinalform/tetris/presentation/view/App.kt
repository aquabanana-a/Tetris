/*
 * Created by S.Dobranos on 05.12.20 22:30
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.view

import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.fromfinalform.tetris.presentation.presenter.PresenterScope

class App : Application() {

    companion object {
        private var appContext: Context? = null
        val presenterScope = PresenterScope()

        fun getApplicationContext(): Context = appContext!!
        fun getResources(): Resources = appContext!!.resources
    }

    override fun onCreate() {
        super.onCreate()

        appContext = applicationContext
    }
}