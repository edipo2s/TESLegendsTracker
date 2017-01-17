package com.ediposouza.teslesgendstracker.ui.util.firebase

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * Created by ediposouza on 04/01/17.
 */
class OnLinearLayoutItemScrolled(val itemPosition: Int, val onViewItem: () -> Unit) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        if (dy < 0) {
            return
        }
        val layoutManager = recyclerView?.layoutManager as LinearLayoutManager
        if (layoutManager.findLastVisibleItemPosition() >= itemPosition) {
            onViewItem()
        }
    }

}