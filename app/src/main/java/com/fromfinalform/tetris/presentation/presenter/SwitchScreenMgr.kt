/*
 * Created by S.Dobranos on 05.12.20 17:09
 * Copyright (c) 2020. All rights reserved.
 */

package com.fromfinalform.tetris.presentation.presenter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.fromfinalform.tetris.R
import com.fromfinalform.tetris.presentation.view.GameOverFragment
import java.lang.IllegalStateException

object SwitchScreenMgr {

    fun onBackPressed(cx: FragmentActivity): Boolean /*absorb*/ {
        val fm: FragmentManager = cx.supportFragmentManager
        var cf = getCurrentFragment(cx)
        var ret = false
        try {
            when (cf) {
                is GameOverFragment -> { onGameOverEnds(cx); ret = true }
            }
            return ret
        } finally {

        }
    }

    fun onGameOverEnds(cx: FragmentActivity) {
        closeCurrentFragment(cx)
    }

    fun switchFragment(cx: FragmentActivity, fg: Fragment) {
        val openFg = {
            val fm: FragmentManager = cx.supportFragmentManager
            val fragmentTransaction = fm.beginTransaction()

            fragmentTransaction.add(R.id.fragment_placer, fg)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()

//            var fragmentTransactionAnimated = FragmentTransactionAnimated(cx, fragmentTransaction, fm.findFragmentById(R.id.fragment_placer), fg, R.id.fragment_placer)
//            fragmentTransactionAnimated.addTransition(switchTransactionType)
//            fragmentTransactionAnimated.commit()
        }

        openFg.invoke()
    }

    fun getCurrentFragment(cx: FragmentActivity): Fragment? {
        val fm: FragmentManager = cx.supportFragmentManager
        return fm.fragments.lastOrNull()
    }

    fun closeCurrentFragment(cx: FragmentActivity, allowLostState: Boolean = true): Fragment? {
        val fm: FragmentManager = cx.supportFragmentManager
        val cf = getCurrentFragment(cx)

        try {
            if (!fm.isStateSaved)
                fm.popBackStackImmediate()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        if (cf != null)
            fm.beginTransaction().remove(cf).let {
                if (allowLostState) it.commitAllowingStateLoss()
                else it.commit()
            }
        return cf
    }

    fun setFragment(cx: FragmentActivity, fg: Fragment) {
        val fm: FragmentManager = cx.supportFragmentManager
        val fragmentTransaction = fm.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_placer, fg)
        fragmentTransaction.commit()
    }
}