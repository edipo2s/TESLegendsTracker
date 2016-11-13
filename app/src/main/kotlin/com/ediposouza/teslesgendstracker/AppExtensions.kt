package com.ediposouza.teslesgendstracker

import android.support.annotation.IntegerRes
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * Created by ediposouza on 01/11/16.
 */
fun String.toIntSafely(): Int {
    if (this.trim().isEmpty())
        return 0
    this.forEach {
        if (!it.isDigit())
            return 0
    }
    return Integer.parseInt(this)
}

fun ViewGroup.inflate(@IntegerRes resource: Int): View {
    return LayoutInflater.from(context).inflate(resource, this, false)
}

fun BottomSheetBehavior<CardView>.toogleExpanded() {
    this.state = if (this.state == BottomSheetBehavior.STATE_COLLAPSED)
        BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
}