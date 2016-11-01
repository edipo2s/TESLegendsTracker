package com.ediposouza.teslesgendstracker

/**
 * Created by ediposouza on 01/11/16.
 */
fun String.toIntSafely(): Int {
    if (this.trim().length == 0)
        return 0
    this.forEach {
        if (!it.isDigit())
            return 0
    }
    return Integer.parseInt(this)
}