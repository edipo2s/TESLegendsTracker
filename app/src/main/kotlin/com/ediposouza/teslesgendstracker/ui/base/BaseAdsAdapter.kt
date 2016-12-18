package com.ediposouza.teslesgendstracker.ui.base

import android.support.annotation.LayoutRes
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.ediposouza.teslesgendstracker.R
import com.ediposouza.teslesgendstracker.inflate
import com.ediposouza.teslesgendstracker.load
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.NativeExpressAdView

/**
 * Created by ediposouza on 08/12/16.
 */
abstract class BaseAdsAdapter(val adsEachItems: Int, @LayoutRes val adsLayout: Int) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        const val ITEM_VIEW_TYPE_DEFAULT = 0
        const val ITEM_VIEW_TYPE_ADS = 1

    }

    constructor(adsEachItems: Int, layoutManager: GridLayoutManager,
                @LayoutRes adsLayout: Int) : this(adsEachItems, adsLayout) {
        onRestoreState(layoutManager)
    }

    abstract fun onCreateDefaultViewHolder(parent: ViewGroup): RecyclerView.ViewHolder
    abstract fun onBindDefaultViewHolder(holder: RecyclerView.ViewHolder?, position: Int)
    abstract fun getDefaultItemCount(): Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == ITEM_VIEW_TYPE_DEFAULT) {
            return onCreateDefaultViewHolder(parent)
        } else {
            val adsItemView = parent.inflate(adsLayout)
            val ads = adsItemView.findViewById(R.id.ads_view)
            when (ads) {
                is AdView -> ads.load()
                is NativeExpressAdView -> ads.load()
            }
            return object : RecyclerView.ViewHolder(adsItemView) {}
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val qtdAdsBefore = getAdsQtdBeforePosition(position)
        if (getItemViewType(position) == ITEM_VIEW_TYPE_DEFAULT) {
            onBindDefaultViewHolder(holder, position - qtdAdsBefore)
        }
    }

    override fun getItemCount(): Int = getDefaultItemCount() + getDefaultItemCount().div(adsEachItems)

    override fun getItemViewType(position: Int): Int {
        val qtdAdsBefore = getAdsQtdBeforePosition(position)
        val nextEachItems = adsEachItems * (qtdAdsBefore + 1) + qtdAdsBefore
        return if (position == nextEachItems) ITEM_VIEW_TYPE_ADS else ITEM_VIEW_TYPE_DEFAULT
    }

    private fun getAdsQtdBeforePosition(position: Int): Int {
        val qtdAds = position.div(adsEachItems)
        return (position - qtdAds).div(adsEachItems)
    }

    protected fun getAdsQtdBeforeDefaultPosition(position: Int): Int {
        return position.div(adsEachItems)
    }

    fun onRestoreState(layoutManager: GridLayoutManager) {
        layoutManager.apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    val itemType = getItemViewType(position)
                    return if (itemType == ITEM_VIEW_TYPE_ADS) spanCount else 1
                }
            }
        }
    }

}