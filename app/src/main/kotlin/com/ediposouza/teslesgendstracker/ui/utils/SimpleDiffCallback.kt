package com.ediposouza.teslesgendstracker.ui.utils

import android.support.v7.util.DiffUtil

/**
 * Created by EdipoSouza on 12/22/16.
 */
class SimpleDiffCallback<T>(val items: List<T>, val oldItems: List<T>,
                            val areItemsTheSame: (oldItem: T, newItem: T) -> Boolean) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areItemsTheSame(oldItems[oldItemPosition], items[newItemPosition])
    }

    override fun getOldListSize(): Int = oldItems.size

    override fun getNewListSize(): Int = items.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areItemsTheSame(oldItemPosition, newItemPosition)
    }

}