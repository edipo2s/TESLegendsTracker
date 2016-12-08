package com.ediposouza.teslesgendstracker.ui.base

import android.support.annotation.LayoutRes
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.inflate
import com.ediposouza.teslesgendstracker.load
import kotlinx.android.synthetic.main.itemlist_ads.view.*

/**
 * Created by ediposouza on 08/12/16.
 */
abstract class BaseAdsAdapter(val adsEachItems: Int) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val ITEM_VIEW_TYPE_DEFAULT = 0
    val ITEM_VIEW_TYPE_ADS = 1

    @LayoutRes abstract fun onDefaultViewLayout(): Int
    abstract fun onCreateDefaultViewHolder(defaultItemView: View): RecyclerView.ViewHolder
    abstract fun onBindDefaultViewHolder(holder: RecyclerView.ViewHolder?, position: Int)
    abstract fun getDefaultItemCount(): Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_VIEW_TYPE_DEFAULT) {
            val defaultItemView = updateViewSpan(parent.inflate(onDefaultViewLayout()), false)
            return onCreateDefaultViewHolder(defaultItemView)
        } else {
            val adsItemView = parent.inflate(R.layout.itemlist_ads)
            adsItemView.ads_view.load()
            return object : RecyclerView.ViewHolder(updateViewSpan(adsItemView, true)) {}
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val qtdAdsBefore = getAdsQtdBefore(position)
        if (getItemViewType(position) == ITEM_VIEW_TYPE_DEFAULT) {
            onBindDefaultViewHolder(holder, position - qtdAdsBefore)
        }
    }

    override fun getItemCount(): Int = getDefaultItemCount() + getDefaultItemCount().div(adsEachItems)

    override fun getItemViewType(position: Int): Int {
        val qtdAdsBefore = getAdsQtdBefore(position)
        val nextEachItems = adsEachItems * (qtdAdsBefore + 1) + qtdAdsBefore
        return if (position == nextEachItems) ITEM_VIEW_TYPE_ADS else ITEM_VIEW_TYPE_DEFAULT
    }

    private fun getAdsQtdBefore(position: Int): Int {
        val qtdAds = position.div(adsEachItems)
        return (position - qtdAds).div(adsEachItems)
    }

    private fun updateViewSpan(view: View, fullSpan: Boolean): View {
        val viewLP = view.layoutParams as StaggeredGridLayoutManager.LayoutParams
        viewLP.isFullSpan = fullSpan
        view.layoutParams = viewLP
        return view
    }

}