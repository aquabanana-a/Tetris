/*
 * Created by S.Dobranos on 05.12.20 22:26
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.presenter

class PresenterScope {

    private val lo = Any()

    private var map: MainActivityPresenter? = null
    private var gop: GameOverPresenter? = null

    val mainActivityPresenter get() = synchronized(lo) { map }
    val gameOverPresenter get() = synchronized(lo) { gop }

    fun register(presenter: MainActivityPresenter): PresenterScope { synchronized(lo) {
        this.map = presenter
        return this
    } }

    fun register(presenter: GameOverPresenter): PresenterScope {
        synchronized(lo) {
            this.gop = presenter
            return this
        }
    }
}