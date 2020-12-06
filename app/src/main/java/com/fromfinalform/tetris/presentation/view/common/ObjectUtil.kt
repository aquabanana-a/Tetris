package com.fromfinalform.tetris.presentation.view.common

import com.fromfinalform.tetris.presentation.view.App.Companion.getResources

const val FORMAT_UNIFORM_COMMON_USE = true
const val FORMAT_UNIFORM_SYMBOL = "[p]"

fun string(resId: Int): String = getResources().getString(resId)

fun formatRegular(value: Int, vararg args: Any?) = formatRegular(string(value), *args)
fun formatRegular(value: String, vararg args: Any?) = String.format(value, *args)

fun formatSymbol(value: Int, vararg args: Any?, symbol: String = FORMAT_UNIFORM_SYMBOL) = formatSymbol(string(value), *args, symbol)
fun formatSymbol(value: String, vararg args: Any?, symbol: String = FORMAT_UNIFORM_SYMBOL): String {
    var ret = value
    for(i in args.indices)
        ret = ret.replaceFirst(symbol, args[i].toString())
    return ret
}

fun format(value: Int, vararg args: Any?) = format(string(value), *args)
fun format(value: String, vararg args: Any?) =
    if(FORMAT_UNIFORM_COMMON_USE)
        formatSymbol(value, *args)
    else
        formatRegular(value, *args)